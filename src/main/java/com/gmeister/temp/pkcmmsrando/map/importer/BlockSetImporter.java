package com.gmeister.temp.pkcmmsrando.map.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;

public class BlockSetImporter
{
	
	public static BlockSet importBlockset(File collisionFile, ArrayList<Constant> collisionConstants, File tileFile,
			TileSet tileSet) throws FileNotFoundException, IOException
	{
		BlockSet blockSet = new BlockSet();
		Pattern whitespace = Pattern.compile("\\s*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern comma = Pattern.compile(",", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern comments = Pattern.compile(";.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern tilecoll = Pattern.compile("\\s*tilecoll\\s*", Pattern.DOTALL);
		
		try (BufferedReader collisionReader = new BufferedReader(new FileReader(collisionFile)))
		{
			while (collisionReader.ready())
			{
				String line = collisionReader.readLine();
				String code = comments.matcher(line).replaceFirst("");
				
				if (tilecoll.matcher(code).find())
				{
					code = tilecoll.matcher(code).replaceFirst("");
					String[] args = comma.split(code, 4);
					if (args.length != 4) throw new IllegalArgumentException(
							"The line \"" + line + "\" is not a valid collision instantiation");
					
					for (int i = 0; i < args.length; i++) args[i] = whitespace.matcher(args[i]).replaceAll("");
					
					Block block = new Block();
					
					for (int y = 0, i = 0; y < 2; y++) for (int x = 0; x < 2; x++, i++)
					{
						args[i] = whitespace.matcher(args[i]).replaceAll("");
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
					
					blockSet.getBlocks().add(block);
				}
				/*
				 * hex values in RGBDS formatting are written by prefixing a number with a $
				 * https://github.com/pret/pokecrystal/blob/7a42f1790ae1e9e357593879bd38c6596dcb03da/constants/collision_constants.asm
				 */
			}
		}
		
		if (tileFile != null && tileSet != null)
		{
			byte[] tiles = Files.readAllBytes(tileFile.toPath());
			int blockNum = Math.floorDiv(tiles.length, 16);
			for (int i = 0; i < blockNum; i++)
			{
				Block block = blockSet.getBlocks().get(i);
				for (int y = 0, j = 0; y < 4; y++)
					for (int x = 0; x < 4; x++, j++) block.getTiles()[y][x] = tileSet.getTiles().get(j);
			}
		}
		
		blockSet.updateCollGroups();
		return blockSet;
	}
	
}
