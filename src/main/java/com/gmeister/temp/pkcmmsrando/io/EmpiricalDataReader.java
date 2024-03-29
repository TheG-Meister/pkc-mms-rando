package com.gmeister.temp.pkcmmsrando.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.gmeister.temp.pkcmmsrando.map.data.CollisionConstant;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionPermission;
import com.gmeister.temp.pkcmmsrando.map.data.Direction;
import com.gmeister.temp.pkcmmsrando.map.data.Flag;
import com.gmeister.temp.pkcmmsrando.map.data.PlayerMovementAction;

/**
 * Reads select empirical data from files within this project. <br>
 * <br>
 * Reads data which has been inferred from gameplay or analysis of dissassembly
 * files, and stored in files generated by developers. This is particularly
 * effective for code or gameplay functions which are simple to interpret
 * externally. This analysis is often difficult or impossible to perform by
 * reading a disassembly or ROM.
 *
 * @author The_G_Meister
 *
 */
public class EmpiricalDataReader
{
	/**
	 * The directory to read files from.
	 */
	private File dir;
	
	public EmpiricalDataReader(File dir)
	{
		super();
		this.dir = dir;
	}
	
	public File getDir()
	{ return this.dir; }
	
	public void setDir(File dir)
	{ this.dir = dir; }
	
	public ArrayList<CollisionPermission> readCollisionPermissions(ArrayList<Flag> flags) throws URISyntaxException, FileNotFoundException, IOException
	{
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("collision-permissions.tsv"))
		{
			if (stream == null) throw new FileNotFoundException("Could not find collision-permissions.tsv");
			
			ArrayList<CollisionPermission> perms = new ArrayList<>();
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")))
			{
				if (!reader.ready()) throw new IOException("The file could not be read or was empty");
				ArrayList<String> headers = new ArrayList<>(Arrays.asList(reader.readLine().split("\t")));
				
				while (reader.ready())
				{
					String line = reader.readLine();
					String[] args = line.split("\t");
					
					CollisionPermission perm = new CollisionPermission();
					perm.setName(args[headers.indexOf("name")]);
					perm.setAllowed(!args[headers.indexOf("allowed")].equals("0"));
					
					if (args.length - 1 >= headers.indexOf("action")) 
					{
						String actionName = args[headers.indexOf("action")];
						if (!actionName.isEmpty())
						{
							boolean found = false;
							for (PlayerMovementAction action : PlayerMovementAction.values()) if (action.name().equals(actionName))
							{
								perm.setAction(action);
								found = true;
								break;
							}
							if (!found) throw new IOException("Could not find a PlayerMovementAction for the name " + actionName);
						}
					}
					
					if (args.length - 1 >= headers.indexOf("flags"))
					{
						ArrayList<String> flagNames = new ArrayList<>(Arrays.asList(args[headers.indexOf("flags")].split(",")));
						for (Flag flag : flags) if (flagNames.contains(flag.getName()))
						{
							flagNames.remove(flag.getName());
							perm.getFlags().add(flag);
						}
						
						if (!flagNames.isEmpty()) throw new IllegalArgumentException("Could not find a flag for the name \"" + flagNames.get(0) + "\"");
					}
					
					perms.add(perm);
				}
			}
			
			return perms;
		}
	}
	
	public ArrayList<CollisionConstant> readCollisionConstants(ArrayList<CollisionPermission> perms) throws FileNotFoundException, IOException, URISyntaxException
	{
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("collision-constants.tsv"))
		{
			if (stream == null) throw new FileNotFoundException("Could not find collision-constants.tsv");
			
			ArrayList<CollisionConstant> constants = new ArrayList<>();
			ArrayList<Direction> directions = new ArrayList<>(Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
			ArrayList<String> onPerms = new ArrayList<>(Arrays.asList("on up", "on down", "on left", "on right"));
			ArrayList<String> offPerms = new ArrayList<>(Arrays.asList("off up", "off down", "off left", "off right"));
			boolean[] steps = {true, false};
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")))
			{
				if (!reader.ready()) throw new IOException("The file could not be read or was empty");
				ArrayList<String> headers = new ArrayList<>(Arrays.asList(reader.readLine().split("\t")));
				
				while (reader.ready())
				{
					String line = reader.readLine();
					String[] args = line.split("\t");
					
					CollisionConstant constant = new CollisionConstant();
					constant.setName(args[headers.indexOf("name")]);
					constant.setValue(Integer.parseInt(args[headers.indexOf("hex")], 16));
					
					for (int i = 0; i < directions.size(); i++) if (headers.indexOf(onPerms.get(i)) < args.length && headers.indexOf(offPerms.get(i)) < args.length)
					{
						String[] permNames = {args[headers.indexOf(onPerms.get(i))], args[headers.indexOf(offPerms.get(i))]};
						for (int j = 0; j < 2; j++) for (CollisionPermission perm : perms) if (perm.getName().equals(permNames[j]))
						{
							constant.setPermissionsForStep(directions.get(i), steps[j], perm);
							break;
						}
					}
					
					constants.add(constant);
				}
			}
			
			return constants;
		}
	}
	
	public ArrayList<String[]> readVanillaMapGroups() throws IOException, URISyntaxException
	{
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("vanilla-map-groups.tsv"))
		{
			if (stream == null) throw new FileNotFoundException("Could not find vanilla-map-groups.tsv");
			
			ArrayList<String[]> groups = new ArrayList<>();
			ArrayList<String> lines = new ArrayList<>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")))
			{
				lines = new ArrayList<>(reader.lines().collect(Collectors.toList()));
			}
			for (String line : lines) groups.add(line.split("\t"));
			return groups;
		}
	}
	
}
