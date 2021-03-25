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
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;
import com.gmeister.temp.pkcmmsrando.map.importer.BlockSetImporter;
import com.gmeister.temp.pkcmmsrando.map.importer.ConstantImporter;

public class DisassemblyIO
{
	
	private File inputFolder;
	private File outputFolder;
	
	public DisassemblyIO(File inputFolder, File outputFolder) throws IOException
	{
		this.inputFolder = inputFolder.getCanonicalFile();
		if (!inputFolder.exists()) throw new FileNotFoundException(inputFolder.getAbsolutePath() + " does not exist");
		
		this.outputFolder = outputFolder.getCanonicalFile();
		this.outputFolder.mkdirs();
	}
	
	public ArrayList<Constant> importCollisionConstants() throws FileNotFoundException, IOException
	{
		return ConstantImporter.importConstants(inputFolder.toPath().resolve("constants/collision_constants.asm").toFile());
	}
	
	public ArrayList<BlockSet> importBlockSets(ArrayList<Constant> collisionConstants) throws FileNotFoundException, IOException
	{
		ArrayList<BlockSet> blockSets = new ArrayList<>();
		File[] blockSetFiles = inputFolder.toPath().resolve("data/tilesets/").toFile().listFiles();
		for (File file : blockSetFiles) if (file.getName().endsWith("_collision.asm"))
		{
			BlockSet blockSet = BlockSetImporter.importBlockset(file, collisionConstants, null, null);
			blockSet.setName(file.getName().replace("_collision.asm", ""));
			blockSets.add(blockSet);
		}
		return blockSets;
	}
	
	public ArrayList<Map> importMaps(ArrayList<BlockSet> blockSets) throws IOException
	{
		/*
		 * Plan:
		 * -Read in the map constants file for sizes
		 * -Read in each block file for MapBlocks objects
		 * -Read in each map script, and translate warps
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
		
		//Get map sizes from constants/map_constants.asm
		Pattern commentsPattern = Pattern.compile("\\s*;.*");
		Pattern mapConstPattern = Pattern.compile("\\tmap_const\\s+");
		Pattern commaWhitespacePattern = Pattern.compile("\\s*,\\s*");
		ArrayList<Map> maps = new ArrayList<>();
		File mapConstantsFile = inputFolder.toPath().resolve("constants/map_constants.asm").toFile();
		try (BufferedReader reader = new BufferedReader(new FileReader(mapConstantsFile)))
		{
			while (reader.ready())
			{
				String line = reader.readLine();
				if (mapConstPattern.matcher(line).find())
				{
					line = commentsPattern.matcher(line).replaceFirst("");
					line = mapConstPattern.matcher(line).replaceFirst("");
					String[] args = commaWhitespacePattern.split(line);
					
					Map map = new Map();
					map.setXCapacity(Integer.parseInt(args[1]));
					map.setXCapacity(Integer.parseInt(args[2]));
					map.setName(args[0]);
					maps.add(map);
				}
			}
		}
		
		//Create a mapping of map file names to map constant names
		Pattern mapAttributesPattern = Pattern.compile("\\tmap_attributes\\s+");
		HashMap<String, String> mapNameToConst = new HashMap<>();
		File mapAttributesFile = inputFolder.toPath().resolve("data/maps/attributes.asm").toFile();
		try (BufferedReader reader = new BufferedReader(new FileReader(mapAttributesFile)))
		{
			while (reader.ready())
			{
				String line = reader.readLine();
				if (mapAttributesPattern.matcher(line).find())
				{
					line = commentsPattern.matcher(line).replaceFirst("");
					line = mapAttributesPattern.matcher(line).replaceFirst("");
					String[] args = commaWhitespacePattern.split(line);
					mapNameToConst.put(args[0], args[1]);
				}
			}
		}
		
		File mapsFolderIn = inputFolder.toPath().resolve("maps/").toFile();
		File mapsFolderOut = outputFolder.toPath().resolve("maps/").toFile();
		mapsFolderOut.mkdirs();
		File[] mapsFiles = mapsFolderIn.listFiles();
		for (File file : mapsFiles) if (file.getName().endsWith(".blk"))
		{
			byte[] b = Files.readAllBytes(file.toPath());
			String blockSetName = mapTileSets.get(file.getName().replace(".blk", ""));
			BlockSet blockSet = null;
			ArrayList<ArrayList<Block>> blockGroups = null;
			for (int i = 0; i < blockSets.size(); i++)
			{
				//System.out.println(blockSetName + " == " + blockSets.get(i).getName());
				if (blockSets.get(i).getName().equals(blockSetName))
				{
					blockSet = blockSets.get(i);
					blockGroups = blockGroupss.get(i);
					break;
				}
			}
			
			if (blockSet != null)
			{
				//read map blocks to the map object
			}
			
			//data/maps/blocks.asm
		}
		
		Pattern warpEventPattern = Pattern.compile("\\twarp_event\\s+");
		for (File file : mapsFiles) if (file.getName().endsWith(".asm"))
		{
			String mapConst = mapNameToConst.get(file.getName().replaceAll(".asm", ""));
			for (Map map : maps) if (map.getName().equals(mapConst))
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("UTF-8"))))
				{
					ArrayList<String> script = new ArrayList<>();
					while (reader.ready()) script.add(reader.readLine());
					map.setScript(script);
				}
				break;
			}
		}
		
		for (Map map : maps) if (map.getScript() != null) for (String line : map.getScript()) if (warpEventPattern.matcher(line).find())
		{
			String argsLine = commentsPattern.matcher(line).replaceFirst("");
			argsLine = warpEventPattern.matcher(argsLine).replaceFirst("");
			String[] args = commaWhitespacePattern.split(argsLine);
			
			Warp warp = new Warp();
			warp.setX(Integer.parseInt(args[0]));
			warp.setY(Integer.parseInt(args[1]));
			warp.setDestinationIndex(Integer.parseInt(args[3]));
			for (Map mapTo : maps) if (mapTo.getName().equals(args[2]))
			{
				warp.setMapTo(mapTo);
				break;
			}
			if (warp.getMapTo() != null)
			{
				map.getWarps().add(warp);
			}
		}
		
		return maps;
	}
	
	public void writeMapBlocks(MapBlocks blocks, BlockSet blockSet) throws IOException
	{
		File outputFile = this.outputFolder.toPath().resolve("maps/" + blocks.getName() + ".blk").toFile();
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
		File outputFile = this.getOutputFolder("maps/").toPath().resolve(map.getName() + ".asm").toFile();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile)))
		{
			for (String line : map.getScript())
			{
				writer.write(line);
				writer.newLine();
			}
			writer.flush();
		}
	}
	
	public void shuffleWarps(ArrayList<Map> maps, Random random)
	{
		Pattern warpEventPattern = Pattern.compile("\\twarp_event\\s+");
		
		ArrayList<Warp> warps = new ArrayList<>();
		for (Map map : maps) warps.addAll(map.getWarps());
		Collections.shuffle(warps, random);
		for (Map map : maps) for (int i = 0; i < map.getWarps().size(); i++) 
		{
			Warp oldWarp = map.getWarps().get(i); 
			Warp newWarp = warps.remove(0);
			oldWarp.setMapTo(newWarp.getMapTo());
			oldWarp.setDestinationIndex(newWarp.getDestinationIndex());
		}
		
		for (Map map : maps)
		{
			int count = 0;
			for (int i = 0; i < map.getScript().size(); i++)
			{
				String line = map.getScript().get(i);
				
				if (warpEventPattern.matcher(line).find())
				{
					Warp warp = map.getWarps().get(count);
					StringBuilder builder = new StringBuilder();
					builder.append("\twarp_event ");
					builder.append(warp.getX()).append(", ");
					builder.append(warp.getY()).append(", ");
					builder.append(warp.getMapTo().getName()).append(", ");
					builder.append(warp.getDestinationIndex());
					
					map.getScript().set(i, builder.toString());
					count++;
				}
			}
		}
		
		//Advanced warp code in development
//		HashMap<Warp, ArrayList<Warp>> warpLinks = new HashMap<>();
//		for (Map map : maps) for (Warp warp : map.getWarps())
//		{
//			Warp dest = warp.getMapTo().getWarps().get(warp.getDestinationIndex());
//			if (!warpLinks.containsKey(dest)) warpLinks.put(dest, new ArrayList<>());
//			warpLinks.get(dest).add(warp);
//		}
//		
//		ArrayList<Integer> newOrder = new ArrayList<>();
//		for (int i = 0; i < warpLinks.keySet().size(); i++) newOrder.add(i);
//		Collections.shuffle(newOrder);
//		ArrayList<Warp> warps = new ArrayList<>(warpLinks.keySet());
//		HashMap<Map, ArrayList<Warp>> newWarps = new HashMap<>();
//		for (int i = 0; i < warps.size(); i++)
//		{
//			Warp warpFrom = warps.get(i);
//			Warp newWarpFrom = warps.get(newOrder.get(i));
//			//Change the destination of all warps in warpLinks.get(warp)
//		}
	}
	
	public void randomiseTrainerLocation(Map map, Random random)
	{
		Pattern commentsPattern = Pattern.compile("\\s*;.*");
		Pattern mapConstPattern = Pattern.compile("\\tmap_const\\s+");
		Pattern commaWhitespacePattern = Pattern.compile("\\s*,\\s*");
		Pattern objectEventPattern = Pattern.compile("\\tobject_event\\s+");
		Pattern numberPattern = Pattern.compile("\\d+");
		
		for (int i = 0; i < map.getScript().size(); i++)
		{
			String line = map.getScript().get(i);
			if (objectEventPattern.matcher(line).find())
			{
				String argsLine = commentsPattern.matcher(line).replaceFirst("");
				argsLine = mapConstPattern.matcher(line).replaceFirst("");
				String[] args = commaWhitespacePattern.split(argsLine);
				
				if (args[9].equals("OBJECTTYPE_TRAINER"))
				{
					int newX = random.nextInt(map.getXCapacity());
					int newY = random.nextInt(map.getYCapacity());
					
					StringBuffer buffer = new StringBuffer();
					Matcher numbers = numberPattern.matcher(line);
					numbers.find();
					numbers.appendReplacement(buffer, String.valueOf(newX));
					numbers.find();
					numbers.appendReplacement(buffer, String.valueOf(newY));
					numbers.appendTail(buffer);
					
					map.getScript().set(i, buffer.toString());
				}
			}
		}
	}
	
	private void importShuffleAndWrite(File in, File out, int startIndex) throws IOException
	{
		ArrayList<String> unchanged = new ArrayList<>();
		ArrayList<String> shuffle = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(in)))
		{
			int i = 0;
			while (reader.ready())
			{
				String line = reader.readLine();
				if (i >= startIndex) shuffle.add(line);
				else unchanged.add(line);
				i++;
			}
		}
		
		Collections.shuffle(shuffle);
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(out)))
		{
			for (int i = 0; i < unchanged.size(); i++)
			{
				writer.write(unchanged.get(i));
				writer.newLine();
			}
			for (int i = 0; i < shuffle.size(); i++)
			{
				writer.write(shuffle.get(i));
				writer.newLine();
			}
			writer.flush();
		}
	}
	
	public void shuffleMusicPointers() throws FileNotFoundException, IOException
	{
		File outAudioFolder = outputFolder.toPath().resolve("audio/").toFile();
		outAudioFolder.mkdirs();
		
		File musicFile = inputFolder.toPath().resolve("audio/music_pointers.asm").toFile();
		if (!musicFile.exists()) throw new FileNotFoundException(musicFile.getAbsolutePath() + " could not be found.");
		
		this.importShuffleAndWrite(musicFile, outAudioFolder.toPath().resolve(musicFile.getName()).toFile(), 5);
	}
	
	public void shuffleSFXPointers() throws FileNotFoundException, IOException
	{
		File outAudioFolder = outputFolder.toPath().resolve("audio/").toFile();
		outAudioFolder.mkdirs();
		
		File sfxFile = inputFolder.toPath().resolve("audio/sfx_pointers.asm").toFile();
		if (!sfxFile.exists()) throw new FileNotFoundException(sfxFile.getAbsolutePath() + " could not be found.");
		
		this.importShuffleAndWrite(sfxFile, outAudioFolder.toPath().resolve(sfxFile.getName()).toFile(), 2);
	}
	
	public File getOutputFolder(String path)
	{
		File outputFolder = this.outputFolder.toPath().resolve(path).toFile();
		if (!outputFolder.exists()) outputFolder.mkdirs();
		return outputFolder;
	}
	
}