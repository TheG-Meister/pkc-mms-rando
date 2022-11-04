package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;
import java.util.function.Predicate;

public class AutoFilteredNetwork<N extends Node, E extends Edge<? extends N>> extends FilteredNetwork<N, E>
{

	public AutoFilteredNetwork(Collection<? extends NodeGroup<N>> nodes,
			Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges, Predicate<E> edgeFilter)
	{ super(nodes, edges, edgeFilter); }

	public AutoFilteredNetwork(GroupedNetwork<N, E> other, Predicate<E> edgeFilter)
	{ super(other, edgeFilter); }

	public AutoFilteredNetwork(Network<? extends N, ? extends E> other, Predicate<E> edgeFilter)
	{ super(other, edgeFilter); }

	public AutoFilteredNetwork(Predicate<E> edgeFilter)
	{ super(edgeFilter); }

	@Override
	public void addEdge(MimickedEdge<NodeGroup<N>, E> edge)
	{
		if (this.edgeFilter == null || this.edgeFilter.test(edge.getOriginalEdge()))
		{
			super.addEdge(edge);
			this.edgeMap.put(edge.getOriginalEdge(), edge);
		}
		else if (!edge.getSource()
				.equals(edge.getTarget()))
			this.merge(edge.getSource(), edge.getTarget());
	}
	
}
