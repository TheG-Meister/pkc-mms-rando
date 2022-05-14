package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;

public class UnreturnableNetwork<N extends Node, E extends Edge<? extends N>> extends GroupedNetwork<N, E>
{

	public UnreturnableNetwork(Collection<? extends NodeGroup<N>> nodes,
			Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges)
	{
		super(nodes, edges, null);
		this.setEdgeFilter(e -> !this.hasPath(this.getNode(e.getTarget()), this.getNode(e.getSource())));
	}
	
	public UnreturnableNetwork(UnreturnableNetwork<N, E> other)
	{
		super(other, null);
		this.setEdgeFilter(e -> !this.hasPath(this.getNode(e.getTarget()), this.getNode(e.getSource())));
	}
	
	public UnreturnableNetwork(Network<? extends N, ? extends E> other)
	{
		super(other, null);
		this.setEdgeFilter(e -> !this.hasPath(this.getNode(e.getTarget()), this.getNode(e.getSource())));
	}
	
	public UnreturnableNetwork()
	{
		super();
		this.setEdgeFilter(e -> !this.hasPath(this.getNode(e.getTarget()), this.getNode(e.getSource())));
	}
	
}
