package com.gmeister.temp.pkcmmsrando.map.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Tile;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;

public class TileSetImporter
{
	
	public static TileSet importTileSet(String name, File metadata) throws FileNotFoundException, IOException
	{
		//check if files exist yada yada yada
		
		TileSet output = new TileSet(name);
		Pattern tab = Pattern.compile("\t");
		
		try (BufferedReader reader = new BufferedReader(new FileReader(metadata)))
		{
			if (!reader.ready()) throw new IOException("File contains no data");
			ArrayList<String> headers = new ArrayList<>(Arrays.asList(tab.split(reader.readLine())));
			
			while (reader.ready())
			{
				Tile tile = new Tile();
				String[] tileData = tab.split(reader.readLine());
				if (tileData.length < headers.size()) throw new IOException("Not all lines have the same length");
				
				tile.setName(tileData[headers.indexOf("Name")]);
				tile.setBuilding(!tileData[headers.indexOf("Building")].equals("0"));
				int id = Integer.parseInt(tileData[headers.indexOf("Tile ID Decimal")]);
				while (id < output.getTiles().size() - 1) output.getTiles().add(null);
				output.getTiles().set(id, tile);
			}
		}
		
		return output;
	}
	
}