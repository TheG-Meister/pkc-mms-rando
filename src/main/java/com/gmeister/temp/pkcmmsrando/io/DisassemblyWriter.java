package com.gmeister.temp.pkcmmsrando.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;

public class DisassemblyWriter
{
	
	private File dir;
	
	public DisassemblyWriter(File dir) throws IOException
	{
		super();
		this.dir = dir.getCanonicalFile();
		if (!this.dir.exists()) this.dir.mkdirs();
		if (this.dir.listFiles().length != 0) throw new IllegalArgumentException("The provided directory was not empty");
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
		File outputFile = this.getSubDir("maps/").toPath().resolve(blocks.getName() + ".blk").toFile();
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
		File file = this.getSubDir("maps/").toPath().resolve(map.getName() + ".asm").toFile();
		this.writeScript(map.getScript(), file);
	}
	
	public void writeMusicPointers(ArrayList<String> script) throws IOException
	{
		File file = this.getSubDir("audio/").toPath().resolve("music_pointers.asm").toFile();
		this.writeScript(script, file);
	}
	
	public void writeSFXPointers(ArrayList<String> script) throws IOException
	{
		File file = this.getSubDir("audio/").toPath().resolve("sfx_pointers.asm").toFile();
		this.writeScript(script, file);
	}
	
	public void writeOverworldSpritePointers(List<String> script) throws IOException
	{
		File file = this.getSubDir("data/sprites/").toPath().resolve("sprites.asm").toFile();
		this.writeScript(script, file);
	}
	
	private void writeScript(List<String> script, File file) throws IOException
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
	
	private File getSubDir(String path)
	{
		File subDir = this.dir.toPath().resolve(path).toFile();
		if (!subDir.exists()) subDir.mkdirs();
		return subDir;
	}
	
}
