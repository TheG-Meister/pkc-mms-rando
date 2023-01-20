package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;

public interface NetworkExploration<N extends Node, E extends Edge<? extends N>>
{
	
	public default boolean hasNextNode()
	{
		return !this.getNodesToExplore().isEmpty();
	}
	
	public Collection<? extends N> getNodesToExplore();
	public void exploreEdge(E edge);
	
}
