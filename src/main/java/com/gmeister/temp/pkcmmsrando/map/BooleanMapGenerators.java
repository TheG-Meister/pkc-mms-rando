package com.gmeister.temp.pkcmmsrando.map;

import java.util.ArrayList;
import java.util.Random;

import com.gmeister.temp.maps.BooleanMap;
import com.gmeister.temp.maps.ReferencePoint;
import com.gmeister.temp.pkcmmsrando.map.data.Block;

public class BooleanMapGenerators
{
	
	/*
	 * In principle, a general generator for a single map:
	 * Can place different types of block
	 * Uses a "next" algorithm to get the next coordinate and Block
	 * Likely requires a tileset as input
	 *
	 * More general generators may:
	 * Generate only a single type of tile
	 * Use probability based selection methods for coordinates (blocks too?)
	 * Draw from a probability "map"
	 * For blob, change origin of construction
	 * Specific shape generators - circle, rectangle, torus, ellipse, square, line, curve, arc
	 *
	 * Types of basic generator:
	 * Path generator links 2 points
	 * Shape generator makes a specific shape
	 * Scatter generator makes random shapes
	 *
	 * Can move these towards pokecrystal-style generators:
	 * Structures - hill, lake, river, path, forest, grass, building, mountain, boulder puzzle, ice puzzle
	 * Areas - town, route, cave, national park, lake of rage, tower, pokecentre indoor, house indoor
	 */
	
	/**
	 * Generates a blob shape by adding coordinates to an existing structure.
	 *
	 * @param size
	 * @param random
	 * @return
	 */
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
					candidateProb.add(Math.pow(10, -r));
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
		}
		
		return blob;
	}
	
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
	public static BooleanMap makeSARWBetween(ReferencePoint start, ReferencePoint end, BooleanMap map, Random random)
	{
		BooleanMap used = new BooleanMap(map.getXCapacity(), map.getYCapacity(), false);
		ReferencePoint current = start;
		ReferencePoint[] adjacentOffsets = {new ReferencePoint(1, 0), new ReferencePoint(-1, 0),
				new ReferencePoint(0, 1), new ReferencePoint(0, -1)};
		
		while (current.getX() != end.getX() || current.getY() != end.getY())
		{
			used.setAt(current.getX(), current.getY(), true);
			
			//collecting candidates
			ArrayList<ReferencePoint> candidates = new ArrayList<>();
			for (int i = 0; i < adjacentOffsets.length; i++)
			{
				ReferencePoint r = current.add(adjacentOffsets[i]);
				if (used.isWithinBoundsAt(r.getX(), r.getY())
						&& (!used.getAt(r.getX(), r.getY()) || (r.getX() == end.getX() && r.getY() == end.getY())))
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
									&& !used.getAt(offset.getX(), offset.getY()))
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
		}
		
		used.setAt(current.getX(), current.getY(), true);
		return used;
		
	}
	
	public static BooleanMap makeCircle(double diameter, boolean centredOnCoord)
	{ return BooleanMapGenerators.makeEllipse(diameter, diameter, centredOnCoord); }
	
	public static BooleanMap makeEllipse(double xDiameter, double yDiameter, boolean centredOnCoord)
	{
		int xCapacity = (int) Math.round(xDiameter);
		int yCapacity = (int) Math.round(yDiameter);
		if ((xCapacity % 2 == 1 && !centredOnCoord) || (xCapacity % 2 == 0 && centredOnCoord)) xCapacity++;
		if ((yCapacity % 2 == 1 && !centredOnCoord) || (yCapacity % 2 == 0 && centredOnCoord)) yCapacity++;
		double xCentre = (double) (xCapacity - 1) / 2;
		double yCentre = (double) (yCapacity - 1) / 2;
		double xRadSq = xCapacity * xCapacity / 4;
		double yRadSq = yCapacity * yCapacity / 4;
		
		BooleanMap output = new BooleanMap(xCapacity, yCapacity, false);
		
		for (int y = 0; y < output.getYCapacity(); y++)
		{
			double y2 = y - yCentre;
			for (int x = 0; x < output.getXCapacity(); x++)
			{
				double x2 = x - xCentre;
				output.setAt(x, y, (x2 * x2) / (xRadSq) + (y2 * y2) / (yRadSq) <= 1);
			}
		}
		
		return output;
	}
	
	public static BooleanMap makeSquare(int capacity)
	{ return BooleanMapGenerators.makeRectangle(capacity, capacity); }
	
	public static BooleanMap makeRectangle(int xCapacity, int yCapacity)
	{ return new BooleanMap(xCapacity, yCapacity, true); }
	
}