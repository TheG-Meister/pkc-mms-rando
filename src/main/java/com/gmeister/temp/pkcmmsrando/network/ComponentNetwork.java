package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;

public class ComponentNetwork<N extends Node, E extends Edge<? extends N>> extends GroupedNetwork<N, E>
{
	
	public ComponentNetwork()
	{
		super();
		this.setEdgeFilter(e -> false);
	}

	public ComponentNetwork(Collection<? extends NodeGroup<N>> nodes,
			Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges)
	{ super(nodes, edges, e -> false); }

	public ComponentNetwork(ComponentNetwork<N, E> other)
	{ super(other, e -> false); }

	public ComponentNetwork(Network<? extends N, ? extends E> other)
	{ super(other, e -> false); }
	
}
