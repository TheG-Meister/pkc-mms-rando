package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.gmeister.temp.pkcmmsrando.map.data.Flag;

public class FlaggedWarpNetwork<N extends Node, E extends FlaggedEdge<? extends N>> extends WarpNetwork<N, E>
{
	
	public FlaggedWarpNetwork()
	{ super(); }

	public FlaggedWarpNetwork(Collection<? extends N> nodes, Collection<? extends E> edges)
	{ super(nodes, edges); }
	
	public FlaggedWarpNetwork(FlaggedWarpNetwork<N, E> other)
	{
		super(other);
	}

	public WarpNetwork<N, E> collapse(Collection<? extends Flag> flags)
	{
		Set<E> edges = new HashSet<>();
		for (N node : this.getNodes()) for (E edge : this.getEdges(node)) if (flags.containsAll(edge.getFlags())) edges.add(edge);
		
		return new WarpNetwork<>(this.getNodes(), edges);
	}
	
}
