package com.gmeister.temp.pkcmmsrando.map.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Constant
{
	
	private String name;
	private byte value;
	
	public Constant(String name, byte value)
	{
		super();
		this.name = name;
		this.value = value;
	}

	public String getName()
	{ return this.name; }

	public void setName(String name)
	{ this.name = name; }

	public byte getValue()
	{ return this.value; }

	public void setValue(byte value)
	{ this.value = value; }
	
	/**
	 * Imports {@linkplain Constant}s from a {@linkplain File}.
	 * 
	 * @param f the file to read from
	 * @return an {@linkplain ArrayList} of {@linkplain Constant}s in the order they are imported
	 * @throws FileNotFoundException when the input file canot be found
	 * @throws IOException when an IO exception occurs
	 */
	public static ArrayList<Constant> importConstants(File f) throws FileNotFoundException, IOException
	{
		ArrayList<Constant> constants = new ArrayList<>();
		Pattern whitespace = Pattern.compile("\s*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern comments = Pattern.compile(";.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern equ = Pattern.compile("\s*EQU\s*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		
		try (BufferedReader reader = new BufferedReader(new FileReader(f)))
		{
			while (reader.ready())
			{
				String line = reader.readLine();
				String code = comments.matcher(line).replaceFirst("");
				
				if (equ.matcher(code).find())
				{
					String[] args = equ.split(code, 2);
					if (args.length != 2) throw new IllegalArgumentException("The line \"" + line + "\" does not compile");
					
					for (int i = 0; i < args.length; i++) args[i] = whitespace.matcher(args[i]).replaceAll("");
					
					byte value = args[1].startsWith("$") ? (byte) Integer.parseInt(args[1].substring(1), 16) : (byte) Integer.parseInt(args[1]);
					constants.add(new Constant(args[0], value));
				}
			}
		}
		
		return constants;
	}
	
}