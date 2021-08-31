package com.gmeister.temp.pkcmmsrando.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionConstant;
import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.Flag;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks;
import com.gmeister.temp.pkcmmsrando.map.data.MapConnection;
import com.gmeister.temp.pkcmmsrando.map.data.MapConnection.Cardinal;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;

public class DisassemblyReader
{
	
	private File dir;
	
	private Pattern commentPattern;
	private Pattern trailingWhitespacePattern;
	private Pattern commaSeparatorPattern;
	private Pattern includePattern;
	private Pattern incbinPattern;
	
	public DisassemblyReader(File dir) throws IOException, FileNotFoundException
	{
		this.dir = dir.getCanonicalFile();
		if (!dir.exists()) throw new FileNotFoundException(dir.getAbsolutePath() + " does not exist");
		
		this.commentPattern = Pattern.compile(";.*");
		this.trailingWhitespacePattern = Pattern.compile("\\s+$");
		this.commaSeparatorPattern = Pattern.compile("\\s*,\\s*");
		this.includePattern = Pattern.compile("^\\s*INCLUDE\\s+");
		this.incbinPattern = Pattern.compile("^\\s*INCBIN\\s+");
	}
	
	public ArrayList<CollisionConstant> readCollisionConstants() throws FileNotFoundException, IOException
	{
		ArrayList<CollisionConstant> output = new ArrayList<>();
		
		ArrayList<Constant> constants = this.importConstants(dir.toPath().resolve("constants/collision_constants.asm").toFile());
		for (Constant constant : constants) output.add(new CollisionConstant(constant));
		
		return output;
	}
	
	public ArrayList<Flag> readEngineFlags() throws FileNotFoundException, IOException
	{
		ArrayList<Flag> output = new ArrayList<>();
		
		ArrayList<Constant> constants = this.importConstants(dir.toPath().resolve("constants/engine_flags.asm").toFile());
		for (Constant constant : constants) if (!constant.getName().equals("NUM_ENGINE_FLAGS")) output.add(new Flag(constant));
		
		return output;
	}
	
	public ArrayList<Flag> readEventFlags() throws FileNotFoundException, IOException
	{
		ArrayList<Flag> output = new ArrayList<>();
		
		ArrayList<Constant> constants = this.importConstants(dir.toPath().resolve("constants/event_flags.asm").toFile());
		for (Constant constant : constants) if (!constant.getName().equals("NUM_EVENTS")) output.add(new Flag(constant));
		
		return output;
	}
	
	public ArrayList<TileSet> readTileSets(ArrayList<CollisionConstant> collisionConstants) throws FileNotFoundException, IOException
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
		File dataScriptFile = this.dir.toPath().resolve("data/tilesets.asm").toFile();
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
		File gfxScriptFile = this.dir.toPath().resolve("gfx/tilesets.asm").toFile();
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
					File file = this.dir.toPath().resolve(filePath).toFile();
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
		
		//Create a mapping of map file names to map constant names
		Pattern mapAttributesPattern = Pattern.compile("\\tmap_attributes\\s+");
		ArrayList<Map> maps = new ArrayList<>();
		
		File mapAttributesFile = dir.toPath().resolve("data/maps/attributes.asm").toFile();
		ArrayList<String> mapAttributesScript = this.readScript(mapAttributesFile);
		HashMap<String, Map> mapsByName = new HashMap<>();
		HashMap<String, Map> mapsByConstName = new HashMap<>();
		
		for (String line : mapAttributesScript) if (mapAttributesPattern.matcher(line).find())
		{
			String backup = line;
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			line = mapAttributesPattern.matcher(line).replaceFirst("");
			String[] args = this.commaSeparatorPattern.split(line);
			
			Map map = new Map();
			if (args.length != 4) throw new IOException("map_attributes did not contain 4 arguments: \"" + backup + "\"");
			map.setName(args[0]);
			map.setConstName(args[1]);
			
			maps.add(map);
			mapsByName.put(map.getName(), map);
			mapsByConstName.put(map.getConstName(), map);
		}

		Pattern connectionPattern = Pattern.compile("\\tconnection\\s+");
		Map currentMap = null;
		for (String line : mapAttributesScript)
		{
			String backup = line;
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			
			if (mapAttributesPattern.matcher(line).find())
			{
				line = mapAttributesPattern.matcher(line).replaceFirst("");
				String[] args = this.commaSeparatorPattern.split(line);
				if (args.length != 4) throw new IOException("map_attributes did not contain 4 arguments: \"" + backup + "\"");
				
				currentMap = mapsByConstName.get(args[1]);
			}
			else if (currentMap != null && connectionPattern.matcher(line).find())
			{
				line = connectionPattern.matcher(line).replaceFirst("");
				String[] args = this.commaSeparatorPattern.split(line);
				if (args.length != 4) throw new IOException("connection did not contain 4 arguments: \"" + backup + "\"");
				
				for (Cardinal cardinal : Cardinal.values()) if (cardinal.name().equals(args[0].toUpperCase()))
				{
					Map connection = mapsByConstName.get(args[2]);
					if (connection == null) throw new IOException("Could not find a map with const name " + args[2] + ": \"" + backup + "\"");
					currentMap.getConnections().put(cardinal, new MapConnection(connection, Integer.parseInt(args[3])));
				}
			}
		}
		
		//Get map sizes from constants/map_constants.asm
		Pattern mapConstPattern = Pattern.compile("^\\tmap_const\\s+");
		File mapConstantsFile = dir.toPath().resolve("constants/map_constants.asm").toFile();
		ArrayList<String> mapConstantsScript = this.readScript(mapConstantsFile);
		for (String line : mapConstantsScript) if (mapConstPattern.matcher(line).find())
		{
			String backup = line;
			line = this.commentPattern.matcher(line).replaceFirst("");
			line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
			line = mapConstPattern.matcher(line).replaceFirst("");
			String[] args = this.commaSeparatorPattern.split(line);
			
			Map map = mapsByConstName.get(args[0]);
			if (map == null) throw new IOException("Could not find a map with name " + args[0] + ": \"" + backup + "\"");
			map.setXCapacity(Integer.parseInt(args[1]));
			map.setYCapacity(Integer.parseInt(args[2]));
		}
		
		ArrayList<Constant> tileSetConstants = this.importConstants(this.dir.toPath().resolve("constants/tileset_constants.asm").toFile());
		
		Pattern mapPattern = Pattern.compile("^\\tmap\\s+");
		File mapDataFile = this.dir.toPath().resolve("data/maps/maps.asm").toFile();
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
		File blocksScriptFile = this.dir.toPath().resolve("data/maps/blocks.asm").toFile();
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
				File file = this.dir.toPath().resolve(filePath).toFile();
				if (!file.exists()) throw new FileNotFoundException("Could not find file " + file.getAbsolutePath());
				byte[] blockIndices = Files.readAllBytes(file.toPath());
				for (Map map : currentLabels) if (!map.getTileSet().equals(currentLabels.get(0).getTileSet())) throw new IOException("Maps using the same blocks use different tile sets");
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
		
		File mapsFolderIn = dir.toPath().resolve("maps/").toFile();
		File[] mapsFiles = mapsFolderIn.listFiles();
		for (File file : mapsFiles) if (file.getName().endsWith(".asm"))
		{
			String mapName = file.getName().replaceAll(".asm", "");
			Map map = mapsByName.get(mapName);
			if (map == null) throw new IOException("Could not find a map with name " + mapName);
			map.setScript(this.readScript(file));
		}
		
		Pattern warpEventPattern = Pattern.compile("\\twarp_event\\s+");
		for (Map map : maps) if (map.getScript() != null) for (String line : map.getScript()) if (warpEventPattern.matcher(line).find()) map.getWarps().add(new Warp());
		
		for (Map map : maps)
		{
			int count = 0;
			if (map.getScript() != null) for (String line : map.getScript()) if (warpEventPattern.matcher(line).find())
			{
				String backup = line;
				line = this.commentPattern.matcher(line).replaceFirst("");
				line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
				line = warpEventPattern.matcher(line).replaceFirst("");
				String[] args = this.commaSeparatorPattern.split(line);
				
				Warp warp = map.getWarps().get(count);
				warp.setX(Integer.parseInt(args[0]));
				warp.setY(Integer.parseInt(args[1]));
				warp.setMap(map);
				int destinationIndex = Integer.parseInt(args[3]) - 1;

				Map mapTo = mapsByConstName.get(args[2]);
				if (mapTo == null) throw new IOException("Could not find a map with name " + args[2] + ": \"" + backup + "\"");
				if (destinationIndex >= mapTo.getWarps().size()) throw new IllegalStateException();
				if (destinationIndex >= 0) warp.setDestination(mapTo.getWarps().get(destinationIndex));
				
				count++;
			}
		}
		
		return maps;
	}
	
	public ArrayList<String> readMapConstants(ArrayList<Map> maps) throws FileNotFoundException, IOException
	{
		Pattern mapAttributesPattern = Pattern.compile("\\tmap_attributes\\s+");
		ArrayList<String> mapConsts = new ArrayList<>();
		File mapAttributesFile = dir.toPath().resolve("data/maps/attributes.asm").toFile();
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
	
	public ArrayList<String> readMusicPointers() throws FileNotFoundException, IOException
	{
		File file = dir.toPath().resolve("audio/music_pointers.asm").toFile();
		if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath() + " could not be found.");
		return this.readScript(file);
	}
	
	public ArrayList<Constant> readMusicConstants() throws IOException
	{
		File file = dir.toPath().resolve("constants/music_constants.asm").toFile();
		if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath() + " could not be found.");
		ArrayList<Constant> constants = this.importConstants(file);
		for (Constant constant : constants) if (!constant.getName().startsWith("MUSIC_")) constants.remove(constant);
		return constants;
	}
	
	public ArrayList<String> readSFXPointers() throws FileNotFoundException, IOException
	{
		File file = dir.toPath().resolve("audio/sfx_pointers.asm").toFile();
		if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath() + " could not be found.");
		return this.readScript(file);
	}
	
	public ArrayList<String> readScript(File file) throws FileNotFoundException, IOException
	{
		ArrayList<String> script = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("UTF-8"))))
		{
			while (reader.ready()) script.add(reader.readLine());
		}
		
		return script;
	}
	
	/**
	 * Imports Constants from a File.
	 * 
	 * @param f the file to read from
	 * @return an ArrayList of Constants in the order they
	 *         are imported
	 * @throws FileNotFoundException if the input file cannot be found
	 * @throws IOException           if an IO exception occurs
	 */
	private ArrayList<Constant> importConstants(File f) throws FileNotFoundException, IOException
	{
		ArrayList<Constant> constants = new ArrayList<>();
		Pattern equPattern = Pattern.compile("\\s+EQU\\s+");
		Pattern constdefPattern = Pattern.compile("^\\tconst_def\\s*");
		Pattern constPattern = Pattern.compile("^\\tconst\\s+");
		Pattern constskipPattern = Pattern.compile("^\\tconst_skip\\s*");
		Pattern constnextPattern = Pattern.compile("^\\tconst_next\\s+");
		
		int count = -1;
		int step = 1;
		try (BufferedReader reader = new BufferedReader(new FileReader(f)))
		{
			while (reader.ready())
			{
				String line = reader.readLine();
				String backup = line;
				line = this.commentPattern.matcher(line).replaceFirst("");
				line = this.trailingWhitespacePattern.matcher(line).replaceFirst("");
				
				if (equPattern.matcher(line).find())
				{
					String[] args = equPattern.split(line, 2);
					if (args.length != 2) continue; //throw new IllegalArgumentException("The line \"" + line + "\" does not compile");
					
					int value;
					if (args[1].startsWith("$")) value = Integer.parseInt(args[1].substring(1), 16);
					else if (args[1].equals("const_value")) value = count;
					else value = Integer.parseInt(args[1]);
					
					constants.add(new Constant(args[0], value));
				}
				else if (constdefPattern.matcher(line).find())
				{
					line = constdefPattern.matcher(line).replaceFirst("");
					if (line.equals("")) count = 0;
					else if (this.commaSeparatorPattern.matcher(line).find())
					{
						String[] args = this.commaSeparatorPattern.split(line);
						if (args.length > 2) throw new IOException("Too many arguments provided to const_def: " + backup);
						count = Integer.parseInt(args[0]);
						step = Integer.parseInt(args[1]);
					}
					else count = Integer.parseInt(line);
				}
				else if (constPattern.matcher(line).find())
				{
					if (count == -1) throw new IllegalStateException();
					line = constPattern.matcher(line).replaceFirst("");
					constants.add(new Constant(line, count));
					count += step;
				}
				else if (constskipPattern.matcher(line).find()) count += step;
				else if (constnextPattern.matcher(line).find())
				{
					line = constnextPattern.matcher(line).replaceFirst("");
					int newCount = Integer.parseInt(line);
					if (newCount < count) throw new IOException("const value cannot decrease: " + backup);
					count = newCount;
				}
			}
		}
		
		return constants;
	}
	
	private ArrayList<Block> readBlockCollision(List<String> collisionScript, List<CollisionConstant> collisionConstants) throws FileNotFoundException, IOException
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
					if (args[i].startsWith("$")) block.getCollision()[y][x] = new CollisionConstant(null,
							(byte) Integer.parseInt(args[i].substring(1), 16));
					else try
					{
						block.getCollision()[y][x] = new CollisionConstant(null, (byte) (byte) Integer.parseInt(args[i]));
					}
					catch (NumberFormatException e)
					{
						String coll = "COLL_" + args[i];
						for (int j = 0; j < collisionConstants.size(); j++)
						{
							CollisionConstant constant = collisionConstants.get(j);
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
	
}