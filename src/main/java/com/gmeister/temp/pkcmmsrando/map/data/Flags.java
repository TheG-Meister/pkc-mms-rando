package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Flags
{
	
	//This algorithm likely completes in O(t^2) time, and that could probably improve if necessary
	/**
	 * Simplifies a collection of sets of flags which all represent the same movement. This is done by removing sets which contain all the elements of any other unequal set.
	 * @param flagSets a collection of sets of flags
	 * @return a simplified collection
	 */
	public static Set<Set<Flag>> simplify(Collection<? extends Set<Flag>> flagSets)
	{
		if (flagSets == null) throw new IllegalArgumentException("flagSets must not be null");
		
		List<Set<Flag>> output = new ArrayList<>(new HashSet<>(flagSets));
		Collections.sort(output, Comparator.comparing(Set::size));
		
		if (true);
		
		for (int i = 0; i < output.size(); i++)
		{
			Set<Flag> set = output.get(i);
			
			//Remove all other sets that contain all the flags of this set
			for (int j = i + 1; j < flagSets.size();) if (set.containsAll(output.get(j))) output.remove(j);
			else j++;
		}
		
		return new HashSet<>(output);
	}
	
	/**
	 * Multiplies two collections of sets of flags together to represent performing two consecutive movements. Both input collections are simplified, multiplied together, and simplified again
	 * @param flagSets the first collection to multiply
	 * @param otherFlagSets the second collection to multiply
	 * @return the multiplied collection
	 */
	public static Set<Set<Flag>> multiply(Collection<? extends Set<Flag>> flagSets, Collection<? extends Set<Flag>> otherFlagSets)
	{
		if (flagSets == null) throw new IllegalArgumentException("flagSets must not be null");
		if (otherFlagSets == null) throw new IllegalArgumentException("otherFlagSets must not be null");
		
		//Simplify the provided collections
		Set<Set<Flag>> flagSetsSimple = Flags.simplify(flagSets);
		Set<Set<Flag>> otherFlagSetsSimple = Flags.simplify(otherFlagSets);
		
		//Create a collection of multiplied flag sets
		Set<Set<Flag>> multipliedSets = new HashSet<>();
		
		//Multiply flag sets together
		for (Set<Flag> flags : flagSetsSimple) for (Set<Flag> otherFlags : otherFlagSetsSimple)
		{
			Set<Flag> set = new HashSet<>();
			set.addAll(flags);
			set.addAll(otherFlags);
			multipliedSets.add(set);
		}
		
		return Flags.simplify(multipliedSets);
	}
	
}