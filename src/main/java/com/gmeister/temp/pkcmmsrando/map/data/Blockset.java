package com.gmeister.temp.pkcmmsrando.map.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Blockset
{
	
	private ArrayList<Block> blocks;
	
	public Blockset()
	{
		this.blocks = new ArrayList<>();
	}

	public ArrayList<Block> getBlocks()
	{ return this.blocks; }
	
	public static Blockset importBlockset(File f, ArrayList<Constant> collisionConstants) throws FileNotFoundException, IOException
	{
		Blockset blockset = new Blockset();
		Pattern whitespace = Pattern.compile("\\s*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern comma = Pattern.compile(",", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern comments = Pattern.compile(";.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern tilecoll = Pattern.compile("\\s*tilecoll\\s*", Pattern.DOTALL);
		
		try (BufferedReader reader = new BufferedReader(new FileReader(f)))
		{
			while (reader.ready())
			{
				String line = reader.readLine();
				String code = comments.matcher(line).replaceFirst("");
				
				if (tilecoll.matcher(code).find())
				{
					code = tilecoll.matcher(code).replaceFirst("");
					String[] args = comma.split(code, 4);
					if (args.length != 4) throw new IllegalArgumentException("The line \"" + line + "\" is not a valid collision instantiation");
					
					for (int i = 0; i < args.length; i++) args[i] = whitespace.matcher(args[i]).replaceAll("");
					
					byte[] values = new byte[args.length];
					for (int i = 0; i < args.length; i++)
					{
						args[i] = whitespace.matcher(args[i]).replaceAll("");
						if (args[i].startsWith("$")) values[i] = (byte) Integer.parseInt(args[i].substring(1), 16);
						else try
						{
							values[i] = (byte) Integer.parseInt(args[i]);
						}
						catch (NumberFormatException e)
						{
							String coll = "COLL_" + args[i];
							for (int j = 0; j < collisionConstants.size(); j++)
							{
								Constant constant = collisionConstants.get(j);
								if (constant.getName().equals(coll)) values[i] = constant.getValue();
							}
						}
					}
					
					blockset.getBlocks().add(new Block(values[0], values[1], values[2], values[3]));
				}
				/*
				 * Let's regex this boi into a readable format
				 * look for "tilecoll\s"
				 * if you find it, remove it and process everything after
				 * remove whitespace
				 * split into an array by commas
				 * if it's numbers, read numbers directly
				 * if it isn't, assume it is a collision constant and find it's value - (make sure you import collision constants too)
				 * hex values in RGBDS formatting are written by prefixing a number with a $
				 * https://github.com/pret/pokecrystal/blob/7a42f1790ae1e9e357593879bd38c6596dcb03da/constants/collision_constants.asm
				 */
			}
		}
		
		return blockset;
	}
	
}