package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.gmeister.temp.pkcmmsrando.map.data.Flag;

/**
 * A helper class that explores networks using FlaggedEdges. 
 * 
 * 
 * @author The_G_Meister
 *
 * @param <N>
 * @param <E>
 */
public class FlaggedNetworkExplorer<N extends Node, E extends Edge<? extends N>>
{
	
	private Network<N, E> network;
	
	/*
	 * NOTE: this field is a workaround for the fact that GroupedNetworks always contain MimickedEdges
	 * This field would not be necessary subject to GroupedNetwork copying the edge type of the Network it is Grouping
	 */
	private Function<E, Collection<Flag>> edgeFlagGetter;
	
	public FlaggedNetworkExplorer(Network<N, E> network, Function<E, Collection<Flag>> edgeFlagGetter)
	{
		this.network = network;
		this.edgeFlagGetter = edgeFlagGetter;
	}
	
	//Much of this code is copied from MapExplorer
	public List<Set<Flag>> getFlagsForPath(N source, N target)
	{
		this.network.validateNode(source);
		this.network.validateNode(target);
		
		List<Set<Flag>> flagSets = new ArrayList<>();
		flagSets.add(new HashSet<>());
		
		if (source.equals(target)) return flagSets;
		
		Map<Set<Flag>, List<N>> accessibleNodes = new HashMap<>();
		List<N> startingNodes = new ArrayList<>();
		startingNodes.add(source);
		accessibleNodes.put(new HashSet<>(), startingNodes);
		
		Set<N> testedNodes = new HashSet<>();
		for (int i = 0; i < flagSets.size(); i++)
		{
			Set<Flag> flags = flagSets.get(i);
			
			//Perform map exploration for this set of flags
			List<N> accessedNodes = accessibleNodes.get(flags);
			for (int j = 0; j < accessedNodes.size(); j++)
			{
				N node = accessedNodes.get(j);
				
				if (!testedNodes.contains(node))
				{
					for (E edge : this.network.getEdges(node))
					{
						Set<Flag> edgeFlags = new HashSet<>(this.edgeFlagGetter.apply(edge));
						List<N> nodeAccessedNodes = accessibleNodes.get(edgeFlags);
						if (nodeAccessedNodes == null)
						{
							nodeAccessedNodes = new ArrayList<>();
							accessibleNodes.put(edgeFlags, nodeAccessedNodes);
						}
						
						nodeAccessedNodes.add(edge.getTarget());
						if (!flagSets.contains(edgeFlags)) flagSets.add(edgeFlags);
					}
					testedNodes.add(node);
				}
			}
		}
		
		//Find the minimum flags to get to the target node
		for (int i = 0; i < flagSets.size(); i++)
		{
			Set<Flag> set = flagSets.get(i);
			
			//Remove all other sets that contain all the permissions of this set
			for (int j = i + 1; j < flagSets.size();) if (set.containsAll(flagSets.get(j))) flagSets.remove(j);
			else j++;
		}
		
		return flagSets;
	}
	
}
