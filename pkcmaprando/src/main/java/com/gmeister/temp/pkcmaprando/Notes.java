package com.gmeister.temp.pkcmaprando;

import java.util.ArrayList;
import java.util.Random;

import com.gmeister.temp.maps.BooleanMap;
import com.gmeister.temp.maps.ReferencePoint;

public class Notes
{
	
	public static void main(String... args)
	{
		Block[] blocks = new Block[16];
		for (byte i = 0; i < blocks.length; i++)
			blocks[i] = new Block((byte) (i & 0b1), (byte) ((i >>> 1) & 0b1), (byte) ((i >>> 2) & 0b1), (byte) ((i >>> 3) & 0b1));
		
		Block[][] map = new Block[8][32];
		
		ReferencePoint start = new ReferencePoint(0, 3);
		ReferencePoint end = new ReferencePoint(31, 4);
		map[start.getY()][start.getX()] = blocks[0];
		map[end.getY()][end.getX()] = blocks[0];
		
		/*
		 * process:
		 * Take two starting blocks coords
		 * Start at one of them
		 * If touching a wall, pick a direction that connects to the other side
		 * else, pick a random, unfilled direction
		 * fill it with block 0
		 * repeat until you pick the end block
		 * fill all other blocks with block 15
		 * 
		 * catch:
		 * picking randomly from all free blocks every time will net us with a sprawly path with a lot of dead ends
		 * placing a random block can still cut us off
		 * Try a random walk
		 * Might be easier to allow editing
		 * 
		 */
		
		BooleanMap used = new BooleanMap(map[0].length, map.length, false);
		
		Random random = new Random();
		ReferencePoint current = start;
		ReferencePoint[] adjacentOffsets = {new ReferencePoint(1, 0), new ReferencePoint(-1, 0), new ReferencePoint(0, 1), new ReferencePoint(0, -1)};
		while(current.getX() != end.getX() || current.getY() != end.getY())
		{
			ArrayList<ReferencePoint> candidates = new ArrayList<>();
			for (int i = 0; i < adjacentOffsets.length; i++)
			{
				ReferencePoint r = current.add(adjacentOffsets[i]);
				if (used.isWithinBoundsAt(r.getX(), r.getY()) && (map[r.getY()][r.getX()] == null || (r.getX() == end.getX() && r.getY() == end.getY()))) candidates.add(r);
			}
			
			ReferencePoint next;
			if (candidates.size() == 2)
			{
				ReferencePoint candidate1 = candidates.get(0);
				ReferencePoint candidate2 = candidates.get(1);
				BooleanMap freeFromEnd = new BooleanMap(used.getXCapacity(), used.getYCapacity(), false);
				BooleanMap toCheck = new BooleanMap(used.getXCapacity(), used.getYCapacity(), false);
				BooleanMap checked = new BooleanMap(used.getXCapacity(), used.getYCapacity(), false);
				freeFromEnd.setAt(end.getX(), end.getY(), true);
				toCheck.setAt(end.getX(), end.getY(), true);
				boolean b;
				
				do
				{
					b = false;
					for (int y = 0; y < toCheck.getYCapacity(); y++)
						for (int x = 0; x < toCheck.getXCapacity(); x++) if (toCheck.getAt(x, y))
					{
						b = true;
						ReferencePoint r = new ReferencePoint(x, y);
						for (int i = 0; i < adjacentOffsets.length; i++)
						{
							ReferencePoint offset = r.add(adjacentOffsets[i]);
							if (toCheck.isWithinBoundsAt(offset.getX(), offset.getY()) && !checked.getAt(offset.getX(), offset.getY()) && map[offset.getY()][offset.getX()] == null)
							{
								freeFromEnd.setAt(offset.getX(), offset.getY(), true);
								toCheck.setAt(offset.getX(), offset.getY(), true);
							}
						}
						toCheck.setAt(x, y, false);
						checked.setAt(x, y, true);
					}
				}
				while (b && !(freeFromEnd.getAt(candidate1.getX(), candidate1.getY()) && freeFromEnd.getAt(candidate2.getX(), candidate2.getY())));
				
				if (!freeFromEnd.getAt(candidate1.getX(), candidate1.getY())) candidates.remove(candidate1);
				if (!freeFromEnd.getAt(candidate2.getX(), candidate2.getY())) candidates.remove(candidate2);
				if (candidates.size() == 0) new IllegalStateException("Fatal logic error - neither candidate can reach the end. Working coordinate " + current.getX() + ", " + current.getY()).printStackTrace();
			}
			
			if (candidates.size() == 0)
			{
				new IllegalStateException("Fatal logic error - no candidate map coordinates were found. Working coordinate " + current.getX() + ", " + current.getY()).printStackTrace();
				break;
			}
			else if (candidates.size() == 1) next = candidates.get(0);
			else next = candidates.get(random.nextInt(candidates.size()));
			
			map[next.getY()][next.getX()] = blocks[0];
			current = next;
		}
		
		BooleanMap collision = new BooleanMap(map[0].length * 2, map.length * 2, false);
		for (int y = 0; y < map.length; y++)
		{
			for (int x = 0; x < map[y].length; x++)
			{
				if (map[y][x] == null) map[y][x] = blocks[15];
				collision.setAt(x * 2, y * 2, map[y][x].getCollision()[0][0] > 0);
				collision.setAt(x * 2 + 1, y * 2, map[y][x].getCollision()[0][1] > 0);
				collision.setAt(x * 2, y * 2 + 1, map[y][x].getCollision()[1][0] > 0);
				collision.setAt(x * 2 + 1, y * 2 + 1, map[y][x].getCollision()[1][1] > 0);
			}
		}
		
		System.out.println(collision.toCleanGridString());
		
	}
	
}