package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.Random;

import com.gmeister.temp.maps.BooleanMap;
import com.gmeister.temp.maps.ReferencePoint;

public class Map
{
	
	//from data/maps/maps.asm
	//name (data/maps/attributes.asm)
	//tileset (a TILESET_* constant)
	//environment (TOWN, ROUTE, INDOOR, CAVE, ENVIRONMENT_5, GATE, or DUNGEON)
	//location: a LANDMARK_* constant
	//music: a MUSIC_* constant
	//phone service flag: TRUE to prevent phone calls
	//time of day: a PALETTE_* constant
	//fishing group: a FISHGROUP_* constant
	
	private int xCapacity;
	private int yCapacity;
	private Block[][] blocks;
	
	public Map(int xCapacity, int yCapacity)
	{
		this.xCapacity = xCapacity;
		this.yCapacity = yCapacity;
		this.blocks = new Block[yCapacity][xCapacity];
	}
	
	public int getXCapacity()
	{ return this.xCapacity; }
	
	public int getYCapacity()
	{ return this.yCapacity; }
	
	public Block[][] getBlocks()
	{ return this.blocks; }
	
	/**
	 * Builds a self-avoiding random walk between two {@link ReferencePoint}s by
	 * filling null elements in this map's {@link Block}s with a path block. Not all
	 * paths are equally likely, as the path is built one block at a time using the
	 * previous built coordinate.
	 *
	 * @param start  the point which is built from
	 * @param end    the point which is built to
	 * @param path   the path block to fill with
	 * @param random a random, used for direction selection
	 */
	public void fillNullsWithSARWBetween(ReferencePoint start, ReferencePoint end, Block path, Random random)
	{
		this.blocks[end.getY()][end.getX()] = path;
		BooleanMap used = new BooleanMap(this.blocks[0].length, this.blocks.length, false);
		ReferencePoint current = start;
		ReferencePoint[] adjacentOffsets = {new ReferencePoint(1, 0), new ReferencePoint(-1, 0),
				new ReferencePoint(0, 1), new ReferencePoint(0, -1)};
		
		int iterations = 0;
		while (current.getX() != end.getX() || current.getY() != end.getY())
		{
			this.blocks[current.getY()][current.getX()] = path;
			
			//collecting candidates
			ArrayList<ReferencePoint> candidates = new ArrayList<>();
			for (int i = 0; i < adjacentOffsets.length; i++)
			{
				ReferencePoint r = current.add(adjacentOffsets[i]);
				if (used.isWithinBoundsAt(r.getX(), r.getY()) && (this.blocks[r.getY()][r.getX()] == null
						|| (r.getX() == end.getX() && r.getY() == end.getY())))
					candidates.add(r);
			}
			
			if (candidates.size() == 0)
				throw new IllegalStateException("Fatal logic error - no candidate map coordinates were found");
			else if (candidates.size() > 1)
			{
				BooleanMap freeFromEnd = new BooleanMap(used.getXCapacity(), used.getYCapacity(), false);
				BooleanMap toCheck = new BooleanMap(used.getXCapacity(), used.getYCapacity(), false);
				BooleanMap checked = new BooleanMap(used.getXCapacity(), used.getYCapacity(), false);
				freeFromEnd.setAt(end.getX(), end.getY(), true);
				toCheck.setAt(end.getX(), end.getY(), true);
				boolean buildingMap;
				boolean allFound;
				
				int iterations2 = 0;
				do
				{
					buildingMap = false;
					for (int y = 0; y < toCheck.getYCapacity(); y++)
						for (int x = 0; x < toCheck.getXCapacity(); x++) if (toCheck.getAt(x, y))
					{
						buildingMap = true;
						ReferencePoint r = new ReferencePoint(x, y);
						for (int i = 0; i < adjacentOffsets.length; i++)
						{
							ReferencePoint offset = r.add(adjacentOffsets[i]);
							if (toCheck.isWithinBoundsAt(offset.getX(), offset.getY())
									&& !checked.getAt(offset.getX(), offset.getY())
									&& this.blocks[offset.getY()][offset.getX()] == null)
							{
								freeFromEnd.setAt(offset.getX(), offset.getY(), true);
								toCheck.setAt(offset.getX(), offset.getY(), true);
							}
						}
						toCheck.setAt(x, y, false);
						checked.setAt(x, y, true);
					}
					
					allFound = true;
					for (int i = 0; i < candidates.size(); i++)
						allFound = allFound && freeFromEnd.getAt(candidates.get(i).getX(), candidates.get(i).getY());
					
					if (++iterations2 > 10000)
						throw new IllegalStateException("Fatal logic error - inner loop could not exit");
				}
				while (buildingMap && !allFound);
				
				for (int i = 0; i < candidates.size(); i++)
					if (!freeFromEnd.getAt(candidates.get(i).getX(), candidates.get(i).getY()))
				{
					candidates.remove(i);
					i--;
				}
				
				if (candidates.size() == 0)
					throw new IllegalStateException("Fatal logic error - no candidate can reach the end");
			}
			
			if (candidates.size() == 1) current = candidates.get(0);
			else current = candidates.get(random.nextInt(candidates.size()));
			
			if (++iterations > 10000) throw new IllegalStateException("Fatal logic error - inner loop could not exit");
		}
		
		this.blocks[current.getY()][current.getX()] = path;
		
	}
	
	public static BooleanMap makeBlob(int size, Random random)
	{
		int radius = (int) Math.round(Math.sqrt(size / Math.PI));
		ReferencePoint centre = new ReferencePoint(radius + 2, radius + 2);
		ReferencePoint[] adjacentOffsets = {new ReferencePoint(1, 0), new ReferencePoint(-1, 0),
				new ReferencePoint(0, 1), new ReferencePoint(0, -1)};
		BooleanMap blob = new BooleanMap(2 * radius + 5, 2 * radius + 5, false);
		BooleanMap elected = new BooleanMap(2 * radius + 5, 2 * radius + 5, false);
		
		ArrayList<ReferencePoint> candidates = new ArrayList<>();
		ArrayList<Double> candidateProb = new ArrayList<>();
		
		candidates.add(centre);
		elected.setAt(centre.getX(), centre.getY(), true);
		
		for (; size > 0; size--)
		{
			double totalProb = 0d;
			for (int i = 0; i < candidates.size(); i++)
			{
				if (i >= candidateProb.size())
				{
					ReferencePoint candidate = candidates.get(i);
					int dx = candidate.getX() - centre.getX();
					int dy = candidate.getY() - centre.getY();
					double r = Math.sqrt(dx * dx + dy * dy);
					if (r < 0.1) r = 0.1;
					candidateProb.add(1 / (r * r));
				}
				totalProb += candidateProb.get(i);
			}
			
			double nextDouble = random.nextDouble() * totalProb;
			
			for (int i = 0; i < candidates.size(); i++)
			{
				nextDouble -= candidateProb.get(i);
				if (nextDouble <= 0)
				{
					ReferencePoint next = candidates.get(i);
					blob.setAt(next.getX(), next.getY(), true);
					
					candidates.remove(i);
					candidateProb.remove(i);
					
					for (int j = 0; j < adjacentOffsets.length; j++)
					{
						ReferencePoint candidate = next.add(adjacentOffsets[j]);
						if (blob.isWithinMapAt(candidate.getX(), candidate.getY())
								&& !blob.getAt(candidate.getX(), candidate.getY())
								&& !elected.getAt(candidate.getX(), candidate.getY()))
						{
							candidates.add(candidate);
							elected.setAt(candidate.getX(), candidate.getY(), true);
						}
					}
					break;
				}
			}
			
			//System.out.println(blob.toCleanGridString());
			//System.out.println();
		}
		
		return blob;
	}
	
	public static void debugPathGenerator(int xCapacity, int yCapacity, ReferencePoint start, ReferencePoint end,
			Block path, Block filler, long seed)
	{
		Map map = new Map(xCapacity, yCapacity);
		
		try
		{
			map.fillNullsWithSARWBetween(start, end, path, new Random(seed));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			System.out.println("Error stats:");
			System.out.println("Seed: " + seed);
			BooleanMap collision2 = new BooleanMap(map.getXCapacity() * 2, map.getYCapacity() * 2, false);
			for (int y = 0; y < map.getYCapacity(); y++) for (int x = 0; x < map.getXCapacity(); x++)
			{
				if (map.getBlocks()[y][x] == null) map.getBlocks()[y][x] = filler;
				for (int y2 = 0; y2 < 2; y2++) for (int x2 = 0; x2 < 2; x2++)
					collision2.setAt(x * 2 + x2, y * 2 + y2, map.getBlocks()[y][x].getCollision()[y2][x2] > 0);
			}
			
			System.out.println(collision2.toCleanGridString());
			System.out.println();
		}
	}
	
	public static void main(String... args)
	{
		/*Random r = new Random(0);
		ReferencePoint start = new ReferencePoint(0, 3);
		ReferencePoint end = new ReferencePoint(31, 4);
		Block[] sampleBlocks = Block.makeSampleBlockset();
		
		for (int i = 0; i < 10000; i++)
		{
			if (i % 100 == 0) System.out.println("Running iteration " + i + "...");
			Map.debugPathGenerator(32, 8, start, end, sampleBlocks[0], sampleBlocks[15], r.nextLong());
		}*/
		
		Random r = new Random();
		System.out.println(Map.makeBlob(50, r).toCleanGridString());
	}
	
}