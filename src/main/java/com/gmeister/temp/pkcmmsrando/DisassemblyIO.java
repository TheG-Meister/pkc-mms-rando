package com.gmeister.temp.pkcmmsrando;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks;
import com.gmeister.temp.pkcmmsrando.map.data.Tile;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;

public class DisassemblyIO
{
	
	private File inputFolder;
	private File outputFolder;
	
	private Pattern commentPattern;
	private Pattern trailingWhitespacePattern;
	private Pattern commaSeparatorPattern;
	private Pattern includePattern;
	private Pattern incbinPattern;
	
	public DisassemblyIO(File inputFolder, File outputFolder) throws IOException
	{
		this.inputFolder = inputFolder.getCanonicalFile();
		if (!inputFolder.exists()) throw new FileNotFoundException(inputFolder.getAbsolutePath() + " does not exist");
		
		this.outputFolder = outputFolder.getCanonicalFile();
		this.outputFolder.mkdirs();
		
		this.commentPattern = Pattern.compile(";.*");
		this.trailingWhitespacePattern = Pattern.compile("\\s+$");
		this.commaSeparatorPattern = Pattern.compile("\\s*,\\s*");
		this.includePattern = Pattern.compile("^\\s*INCLUDE\\s+");
		this.incbinPattern = Pattern.compile("^\\s*INCBIN\\s+");
	}
	
	public ArrayList<Constant> readCollisionConstants() throws FileNotFoundException, IOException
	{
		return this.importConstants(inputFolder.toPath().resolve("constants/collision_constants.asm").toFile());
	}
	
	public ArrayList<TileSet> readTileSets(ArrayList<Constant> collisionConstants) throws FileNotFoundException, IOException
	{
		ArrayList<TileSet> tileSets = new ArrayList<>();
		
		//https://github.com/pret/pokecrystal/blob/master/data/tilesets.asm - defines lots of tileset pointers
		//https://github.com/pret/pokecrystal/blob/master/gfx/tilesets.asm - contains the files pointed to by a lot of the tileset pointers
		/*
		 * Each map has a:
		 * GFX pointer, to tile graphics
		 * Meta pointer, to metatiles (aka Blocks) - CAN READ, BUT HAVEN'T IMPLEMENTED YET
		 * Coll pointer, to metatile collision - DONE
		 * Anim pointer, to tile animations
		 * PalMap pointer, to tile palettes
		 * 
		 * Read in all names from data/tilesets.asm
		 * Follow the meta and coll pointers to gfx/tilesets.asm and import the relevant files.
		 * 
		 */
		
		Pattern tilesetMacroPattern = Pattern.compile("^\\ttileset\\s*");
		File dataScriptFile = this.inputFolder.toPath().resolve("data/tilesets.asm").toFile();
		ArrayList<String> dataScript = this.readScript(dataScriptFile);
		for (String line : dataScript)
		{
			Matcher tilesetMacroMatcher = tilesetMacroPattern.matcher(line);
			if (tilesetMacroMatcher.find())
			{
				line = tilesetMacroMatcher.replaceAll("");
				line = this.commentPattern.matcher(line).replaceAll("");
				line = this.trailingWhitespacePattern.matcher(line).replaceAll("");
				tileSets.add(new TileSet(line));
			}
		}
		
		Pattern collPointerPattern = Pattern.compile("Coll::$");
		Pattern metaPointerPattern = Pattern.compile("Meta::$");
		File gfxScriptFile = this.inputFolder.toPath().resolve("gfx/tilesets.asm").toFile();
		ArrayList<String> gfxScript = this.readScript(gfxScriptFile);
		String mode = null;
		ArrayList<TileSet> currentPointers = new ArrayList<>();
		for (String line : gfxScript)
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			
			Matcher collPointerMatcher = collPointerPattern.matcher(line);
			Matcher metaPointerMatcher = metaPointerPattern.matcher(line);
			Matcher includeMatcher = this.includePattern.matcher(line);
			Matcher incbinMatcher = this.incbinPattern.matcher(line);
			
			if (collPointerMatcher.find())
			{
				if (mode != null && mode != "coll") throw new IllegalStateException("Different data types point to the same file type in " + gfxScriptFile.getAbsolutePath());
				mode = "coll";
				String tileSetName = collPointerMatcher.replaceFirst("");
				for (TileSet tileSet : tileSets) if (tileSet.getName().equals(tileSetName)) currentPointers.add(tileSet);
			}
			else if (metaPointerMatcher.find())
			{
				if (mode != null && mode != "meta") throw new IllegalStateException("Different data types point to the same file type in " + gfxScriptFile.getAbsolutePath());
				mode = "meta";
				String tileSetName = metaPointerMatcher.replaceFirst("");
				for (TileSet tileSet : tileSets) if (tileSet.getName().equals(tileSetName)) currentPointers.add(tileSet);
			}
			else if (includeMatcher.find())
			{
				if (mode != null && mode.equals("coll"))
				{
					String filePath = includeMatcher.replaceFirst("");
					filePath = filePath.replace("\"", "");
					File file = this.inputFolder.toPath().resolve(filePath).toFile();
					if (!file.exists()) throw new IllegalStateException("Could not find file " + file.getAbsolutePath());
					ArrayList<String> collisionScript = this.readScript(file);
					BlockSet blockSet = new BlockSet();
					blockSet.setName(file.getName().replace("_collision.asm", ""));
					ArrayList<Block> blocks = this.readBlockCollision(collisionScript, collisionConstants);
					blockSet.setBlocks(blocks);
					for (TileSet tileSet : currentPointers) tileSet.setBlockSet(blockSet);
				}
				currentPointers.clear();
				mode = null;
			}
			else if (incbinMatcher.find())
			{
				if (mode != null && mode.equals("meta"))
				{
					
				}
				mode = null;
			}
		}
		
		return tileSets;
	}
	
	public ArrayList<Map> readMaps(ArrayList<TileSet> tileSets) throws IOException
	{
		/*
		 * Plan:
		 * -Read in the map attributes file for tilesets (and therefore blocksets)
		 * -Read in the map constants file for sizes
		 * -Read in each block file for MapBlocks objects
		 * -Read in each map script, and translate warps
		 * 
		 * in RGBDS, CamelCase usually means a pointer and SNAKE_CASE means a constant
		 * 
		 * https://github.com/pret/pokecrystal/blob/master/data/maps/maps.asm - has tables of a lot of map data (one map pointer to lots of constants)
		 * https://github.com/pret/pokecrystal/blob/master/constants/tileset_constants.asm - has the numbers corresponding to the tileset constants
		 * https://github.com/pret/pokecrystal/blob/master/data/tilesets.asm - defines lots of tileset pointers
		 * https://github.com/pret/pokecrystal/blob/master/gfx/tilesets.asm - contains the files pointed to by a lot of the tileset pointers
		 * 
		 * To get from a tileset constant to a blockset the full process is
		 * -Get the value of the constant
		 * -Get the pointer with the same index
		 * -Find the pointer's blocks
		 */
		
		/*File mapMeta = Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/pkc-mms-rando/metadata/map-data.tsv").toFile();
		HashMap<String, String> mapTileSets = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(mapMeta)))
		{
			while (reader.ready())
			{
				String[] fields = reader.readLine().split("\t");
				mapTileSets.put(fields[0], fields[1].toLowerCase().replace("tileset_", ""));
			}
		}*/
		
		//Create a mapping of map file names to map constant names
		Pattern mapAttributesPattern = Pattern.compile("\\tmap_attributes\\s+");
		ArrayList<Map> maps = new ArrayList<>();
		File mapAttributesFile = inputFolder.toPath().resolve("data/maps/attributes.asm").toFile();
		ArrayList<String> mapAttributesScript = this.readScript(mapAttributesFile);
		for (String line : mapAttributesScript) if (mapAttributesPattern.matcher(line).find())
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			line = mapAttributesPattern.matcher(line).replaceFirst("");
			String[] args = this.commaSeparatorPattern.split(line);
			Map map = new Map();
			map.setName(args[0]);
			map.setConstName(args[1]);
			maps.add(map);
		}
		
		//Get map sizes from constants/map_constants.asm
		Pattern mapConstPattern = Pattern.compile("^\\tmap_const\\s+");
		File mapConstantsFile = inputFolder.toPath().resolve("constants/map_constants.asm").toFile();
		ArrayList<String> mapConstantsScript = this.readScript(mapConstantsFile);
		for (String line : mapConstantsScript) if (mapConstPattern.matcher(line).find())
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			line = mapConstPattern.matcher(line).replaceFirst("");
			String[] args = this.commaSeparatorPattern.split(line);
			
			for (Map map : maps) if (args[0].equals(map.getConstName()))
			{
				map.setXCapacity(Integer.parseInt(args[1]));
				map.setYCapacity(Integer.parseInt(args[2]));
			}
		}
		
		ArrayList<Constant> tileSetConstants = this.importConstants(this.inputFolder.toPath().resolve("constants/tileset_constants.asm").toFile());
		
		Pattern mapPattern = Pattern.compile("^\\tmap\\s+");
		File mapDataFile = this.inputFolder.toPath().resolve("data/maps/maps.asm").toFile();
		ArrayList<String> mapDataScript = this.readScript(mapDataFile);
		for (String line : mapDataScript) if (mapPattern.matcher(line).find())
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			line = mapPattern.matcher(line).replaceFirst("");
			String[] args = this.commaSeparatorPattern.split(line);
			
			for (Map map : maps) if (map.getName().equals(args[0]))
			{
				String tileSetConstName = args[1];
				for (int i = 0; i < tileSetConstants.size(); i++)
				{
					Constant tileSetConstant = tileSetConstants.get(i);
					if (tileSetConstant.getName().equals(tileSetConstName))
					{
						map.setTileSet(tileSets.get(tileSetConstant.getValue()));
						break;
					}
				}
				break;
			}
		}
		
		Pattern blocksLabelPattern = Pattern.compile("_Blocks:");
		File blocksScriptFile = this.inputFolder.toPath().resolve("data/maps/blocks.asm").toFile();
		ArrayList<String> blocksScript = this.readScript(blocksScriptFile);
		ArrayList<Map> currentLabels = new ArrayList<>();
		for (String line : blocksScript)
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			
			Matcher blocksLabelMatcher = blocksLabelPattern.matcher(line);
			Matcher incbinMatcher = this.incbinPattern.matcher(line);
			
			if (blocksLabelMatcher.find())
			{
				String mapName = blocksLabelMatcher.replaceFirst("");
				for (Map map : maps) if (map.getName().equals(mapName)) currentLabels.add(map);
			}
			else if (incbinMatcher.find())
			{
				if (currentLabels.size() == 0) continue;
				String filePath = incbinMatcher.replaceFirst("");
				filePath = filePath.replace("\"", "");
				File file = this.inputFolder.toPath().resolve(filePath).toFile();
				if (!file.exists()) throw new IllegalStateException("Could not find file " + file.getAbsolutePath());
				byte[] blockIndices = Files.readAllBytes(file.toPath());
				for (Map map : currentLabels) if (!map.getTileSet().equals(currentLabels.get(0).getTileSet())) throw new IllegalStateException("Maps using the same blocks use different tile sets");
				ArrayList<Block> blockSetBlocks = currentLabels.get(0).getTileSet().getBlockSet().getBlocks();
				MapBlocks mapBlocksObject = new MapBlocks();
				mapBlocksObject.setXCapacity(currentLabels.get(0).getXCapacity());
				mapBlocksObject.setYCapacity(currentLabels.get(0).getYCapacity());
				Block[] mapBlocks = new Block[blockIndices.length];
				for (int i = 0; i < blockIndices.length; i++) mapBlocks[i] = blockSetBlocks.get(blockIndices[i]);
				mapBlocksObject.setBlocks(mapBlocks);
				mapBlocksObject.setName(file.getName().replace(".blk", ""));
				for (Map map : currentLabels) map.setBlocks(mapBlocksObject);
				currentLabels.clear();
			}
		}
		
		File mapsFolderIn = inputFolder.toPath().resolve("maps/").toFile();
		File[] mapsFiles = mapsFolderIn.listFiles();
		for (File file : mapsFiles) if (file.getName().endsWith(".asm"))
		{
			String mapName = file.getName().replaceAll(".asm", "");
			for (Map map : maps) if (map.getName().equals(mapName))
			{
				map.setScript(this.readScript(file));
				break;
			}
		}
		
		Pattern warpEventPattern = Pattern.compile("\\twarp_event\\s+");
		for (Map map : maps) if (map.getScript() != null) for (String line : map.getScript()) if (warpEventPattern.matcher(line).find()) map.getWarps().add(new Warp());
		
		for (Map map : maps)
		{
			int count = 0;
			if (map.getScript() != null) for (String line : map.getScript()) if (warpEventPattern.matcher(line).find())
			{
				line = this.commentPattern.matcher(line).replaceFirst("");
				line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
				line = warpEventPattern.matcher(line).replaceFirst("");
				String[] args = this.commaSeparatorPattern.split(line);
				
				Warp warp = map.getWarps().get(count);
				warp.setX(Integer.parseInt(args[0]));
				warp.setY(Integer.parseInt(args[1]));
				int destinationIndex = Integer.parseInt(args[3]);
				
				for (Map mapTo : maps) if (mapTo.getConstName().equals(args[2]))
				{
					warp.setMapTo(mapTo);
					if (destinationIndex < mapTo.getWarps().size() && destinationIndex >= 0) warp.setDestination(mapTo.getWarps().get(destinationIndex));
				}
				
				count++;
			}
		}
		
		return maps;
	}
	
	public ArrayList<String> readMapConstants(ArrayList<Map> maps) throws FileNotFoundException, IOException
	{
		Pattern mapAttributesPattern = Pattern.compile("\\tmap_attributes\\s+");
		ArrayList<String> mapConsts = new ArrayList<>();
		File mapAttributesFile = inputFolder.toPath().resolve("data/maps/attributes.asm").toFile();
		ArrayList<String> mapAttributesScript = this.readScript(mapAttributesFile);
		for (int i = 0; i < maps.size(); i++) mapConsts.add(null);
		
		for (String line : mapAttributesScript) if (mapAttributesPattern.matcher(line).find())
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			line = mapAttributesPattern.matcher(line).replaceFirst("");
			String[] args = this.commaSeparatorPattern.split(line);
			for (Map map : maps) if (map.getName().equals(args[0])) mapConsts.set(maps.indexOf(map), args[1]);
		}
		
		return mapConsts;
	}
	
	public void writeAllMapBlocks(ArrayList<Map> maps) throws IOException
	{
		ArrayList<MapBlocks> mapBlockss = new ArrayList<>();
		ArrayList<TileSet> tileSets = new ArrayList<>();
		for (Map map : maps) if (!mapBlockss.contains(map.getBlocks()))
		{
			mapBlockss.add(map.getBlocks());
			tileSets.add(map.getTileSet());
		}
		
		for (int i = 0; i < mapBlockss.size(); i++) this.writeMapBlocks(mapBlockss.get(i), tileSets.get(i).getBlockSet());
	}
	
	public void writeMapBlocks(MapBlocks blocks, BlockSet blockSet) throws IOException
	{
		File outputFile = this.getOutputFolder("maps/").toPath().resolve(blocks.getName() + ".blk").toFile();
		byte[] outputArray = new byte[blocks.getBlocks().length];
		for (int i = 0; i < blocks.getBlocks().length; i++)
		{
			if (!blockSet.getBlocks().contains(blocks.getBlocks()[i])) throw new IllegalArgumentException("This blockset does not contain the map block at index " + i);
			outputArray[i] = (byte) (blockSet.getBlocks().indexOf(blocks.getBlocks()[i]) & 0xFF);
		}
		Files.write(outputFile.toPath(), outputArray);
	}
	
	public void writeMapScript(Map map) throws IOException
	{
		File file = this.getOutputFolder("maps/").toPath().resolve(map.getName() + ".asm").toFile();
		this.writeScript(map.getScript(), file);
	}
	
	public ArrayList<String> readMusicPointers() throws FileNotFoundException, IOException
	{
		File file = inputFolder.toPath().resolve("audio/music_pointers.asm").toFile();
		if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath() + " could not be found.");
		return this.readScript(file);
	}
	
	public void writeMusicPointers(ArrayList<String> script) throws IOException
	{
		File file = this.getOutputFolder("audio/").toPath().resolve("music_pointers.asm").toFile();
		this.writeScript(script, file);
	}
	
	public ArrayList<String> readSFXPointers() throws FileNotFoundException, IOException
	{
		File file = inputFolder.toPath().resolve("audio/sfx_pointers.asm").toFile();
		if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath() + " could not be found.");
		return this.readScript(file);
	}
	
	public void writeSFXPointers(ArrayList<String> script) throws IOException
	{
		File file = this.getOutputFolder("audio/").toPath().resolve("sfx_pointers.asm").toFile();
		this.writeScript(script, file);
	}
	
	private ArrayList<String> readScript(File file) throws FileNotFoundException, IOException
	{
		ArrayList<String> script = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("UTF-8"))))
		{
			while (reader.ready()) script.add(reader.readLine());
		}
		
		return script;
	}
	
	private void writeScript(ArrayList<String> script, File file) throws IOException
	{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, Charset.forName("UTF-8"))))
		{
			for (String line : script)
			{
				writer.write(line);
				writer.newLine();
			}
			writer.flush();
		}
	}
	
	private File getOutputFolder(String path)
	{
		File outputFolder = this.outputFolder.toPath().resolve(path).toFile();
		if (!outputFolder.exists()) outputFolder.mkdirs();
		return outputFolder;
	}
	
	/**
	 * Imports {@linkplain Constant}s from a {@linkplain File}.
	 * 
	 * @param f the file to read from
	 * @return an {@linkplain ArrayList} of {@linkplain Constant}s in the order they
	 *         are imported
	 * @throws FileNotFoundException when the input file canot be found
	 * @throws IOException           when an IO exception occurs
	 */
	private ArrayList<Constant> importConstants(File f) throws FileNotFoundException, IOException
	{
		ArrayList<Constant> constants = new ArrayList<>();
		Pattern equPattern = Pattern.compile("\\s+EQU\\s+");
		Pattern constdefPattern = Pattern.compile("^\\tconst_def\\s+");
		Pattern constPattern = Pattern.compile("^\\tconst\\s+");
		
		int count = -1;
		try (BufferedReader reader = new BufferedReader(new FileReader(f)))
		{
			while (reader.ready())
			{
				String line = reader.readLine();
				line = this.commentPattern.matcher(line).replaceFirst("");
				line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
				
				if (equPattern.matcher(line).find())
				{
					String[] args = equPattern.split(line, 2);
					if (args.length != 2) continue; //throw new IllegalArgumentException("The line \"" + line + "\" does not compile");
					
					byte value = args[1].startsWith("$") ? (byte) Integer.parseInt(args[1].substring(1), 16)
							: (byte) Integer.parseInt(args[1]);
					constants.add(new Constant(args[0], value));
				}
				else if (constdefPattern.matcher(line).find())
				{
					line = constdefPattern.matcher(line).replaceFirst("");
					if (line.equals("")) count = 0;
					else count = Integer.parseInt(line);
				}
				else if (constPattern.matcher(line).find())
				{
					if (count == -1) throw new IllegalStateException("");
					line = constPattern.matcher(line).replaceFirst("");
					constants.add(new Constant(line, (byte) (count & 0xFF)));
					count++;
				}
			}
		}
		
		return constants;
	}
	
	private ArrayList<Block> readBlockCollision(List<String> collisionScript, List<Constant> collisionConstants) throws FileNotFoundException, IOException
	{
		ArrayList<Block> blocks = new ArrayList<>();
		Pattern tilecoll = Pattern.compile("\\s*tilecoll\\s*", Pattern.DOTALL);
		
		for (String line : collisionScript)
		{
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			
			if (tilecoll.matcher(line).find())
			{
				line = tilecoll.matcher(line).replaceFirst("");
				String[] args = this.commaSeparatorPattern.split(line);
				if (args.length != 4) throw new IllegalArgumentException(
						"The line \"" + line + "\" is not a valid collision instantiation");
				
				Block block = new Block();
				
				for (int y = 0, i = 0; y < 2; y++) for (int x = 0; x < 2; x++, i++)
				{
					if (args[i].startsWith("$")) block.getCollision()[y][x] = new Constant(null,
							(byte) Integer.parseInt(args[i].substring(1), 16));
					else try
					{
						block.getCollision()[y][x] = new Constant(null, (byte) (byte) Integer.parseInt(args[i]));
					}
					catch (NumberFormatException e)
					{
						String coll = "COLL_" + args[i];
						for (int j = 0; j < collisionConstants.size(); j++)
						{
							Constant constant = collisionConstants.get(j);
							if (constant.getName().equals(coll))
							{
								block.getCollision()[y][x] = constant;
								break;
							}
						}
					}
				}
				
				blocks.add(block);
			}
		}
		
		return blocks;
	}
	
	private void readBlockTiles(ArrayList<Block> blocks, File tileFile, ArrayList<Tile> tiles) throws IOException
	{
		byte[] tileIndices = Files.readAllBytes(tileFile.toPath());
		int blockNum = Math.floorDiv(tileIndices.length, 16);
		for (int i = 0; i < blockNum; i++)
		{
			Block block = blocks.get(i);
			for (int y = 0, j = 0; y < 4; y++)
				for (int x = 0; x < 4; x++, j++) block.getTiles()[y][x] = tiles.get(j);
		}
	}
	
}