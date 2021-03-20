package com.gmeister.temp.pkcmmsrando.map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
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
	
	public void fillAllRoutesWithGrass() throws IOException
	{
		outputFolder.mkdirs();
		
		File constantsFile = inputFolder.toPath().resolve("constants/collision_constants.asm").toFile();
		ArrayList<Constant> collisionConstants = ConstantImporter.importConstants(constantsFile);
		
		ArrayList<BlockSet> blockSets = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<Block>>> blockGroupss = new ArrayList<>();
		File blockSetFolder = inputFolder.toPath().resolve("data/tilesets/").toFile();
		File[] blockSetFiles = blockSetFolder.listFiles();
		for (File file : blockSetFiles) if (file.getName().endsWith("_collision.asm"))
		{
			BlockSet blockSet = BlockSetImporter.importBlockset(file, collisionConstants, null, null);
			blockSet.setName(file.getName().replace("_collision.asm", ""));
			blockSets.add(blockSet);
			ArrayList<ArrayList<Block>> blockGroups = new ArrayList<>();
			blockGroupss.add(blockGroups);
			
			for (Block block : blockSet.getBlocks()) if (blockSet.getBlocks().indexOf(block) != 0)
			{
				ArrayList<Block> blockGroup = null;
				for (ArrayList<Block> group : blockGroups)
				{
					boolean same = true;
					Block tester = group.get(0);
					blockTesting:
					for (int y = 0; y < 2; y++) for (int x = 0; x < 2; x++)
						if (tester.getCollision()[y][x].getValue() != block.getCollision()[y][x].getValue())
					{
						same = false;
						break blockTesting;
					}
					if (same)
					{
						blockGroup = group;
						break;
					}
				}
				
				if (blockGroup == null)
				{
					blockGroup = new ArrayList<>();
					blockGroups.add(blockGroup);
				}
				blockGroup.add(block);
			}
		}
		
		File mapMeta = Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/pkc-mms-rando/metadata/map-data.tsv").toFile();
		HashMap<String, String> mapTileSets = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(mapMeta)))
		{
			while (reader.ready())
			{
				String[] fields = reader.readLine().split("\t");
				mapTileSets.put(fields[0], fields[1].toLowerCase().replace("tileset_", ""));
			}
		}
		
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
					
					Map map = new Map(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
					map.setName(args[0]);
					maps.add(map);
				}
			}
		}
		
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
		
		Random random = new Random();
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
			
			//data/maps/blocks.asm
			
			if (blockSet != null) for (int i = 0; i < b.length; i++) if (b[i] != 0)
			{
				Block block = blockSet.getBlocks().get(b[i]);
				ArrayList<Block> blockGroup = null;
				for (ArrayList<Block> group : blockGroups) if (group.contains(block))
				{
					blockGroup = group;
					break;
				}
				b[i] = (byte) blockSet.getBlocks().indexOf(blockGroup.get(random.nextInt(blockGroup.size())));
			}
			//else System.out.println(file.getName().replace(".blk", ""));
			
			File outputFile = mapsFolderOut.toPath().resolve(file.getName()).toFile();
			try (FileOutputStream stream = new FileOutputStream(outputFile))
			{
				stream.write(b);
			}
		}
		
		Pattern objectEventPattern = Pattern.compile("\\tobject_event\\s+");
		Pattern numberPattern = Pattern.compile("\\d+");
		
		for (File file : mapsFiles) if (file.getName().endsWith(".asm"))
		{
			String mapConst = mapNameToConst.get(file.getName().replaceAll(".asm", ""));
			for (Map map : maps) if (map.getName().equals(mapConst))
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("UTF-8"))))
				{
					File outFile = mapsFolderOut.toPath().resolve(file.getName()).toFile();
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, Charset.forName("UTF-8"))))
					{
						while (reader.ready())
						{
							String line = reader.readLine();
//							if (objectEventPattern.matcher(line).find())
//							{
//								String argsLine = commentsPattern.matcher(line).replaceFirst("");
//								argsLine = mapConstPattern.matcher(line).replaceFirst("");
//								String[] args = commaWhitespacePattern.split(argsLine);
//								
//								if (args[9].equals("OBJECTTYPE_TRAINER"))
//								{
//									int newX = random.nextInt(map.getXCapacity());
//									int newY = random.nextInt(map.getYCapacity());
//									
//									StringBuffer buffer = new StringBuffer();
//									Matcher numbers = numberPattern.matcher(line);
//									numbers.find();
//									numbers.appendReplacement(buffer, String.valueOf(newX));
//									numbers.find();
//									numbers.appendReplacement(buffer, String.valueOf(newY));
//									numbers.appendTail(buffer);
//									
//									line = buffer.toString();
//								}
//							}
							
							
							
							writer.write(line);
							writer.newLine();
							writer.flush();
						}
					}
				}
				break;
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
	
}