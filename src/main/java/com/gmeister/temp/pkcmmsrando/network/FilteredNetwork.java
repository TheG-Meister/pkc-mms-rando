package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class FilteredNetwork<N extends Node, E extends Edge<? extends N>> extends GroupedNetwork<N, E>
{
	
	protected Predicate<E> edgeFilter;
	
	public FilteredNetwork(Predicate<E> edgeFilter)
	{
		super();
		this.edgeFilter = edgeFilter;
	}
	
	public FilteredNetwork(Collection<? extends NodeGroup<N>> nodes,
			Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges, Predicate<E> edgeFilter)
	{
		super(nodes, edges);
		
		this.edgeFilter = edgeFilter;
	}
	
	public FilteredNetwork(GroupedNetwork<N, E> other, Predicate<E> edgeFilter)
	{
		super(other);
		this.edgeFilter = edgeFilter;
	}
	
	public FilteredNetwork(Network<? extends N, ? extends E> other, Predicate<E> edgeFilter)
	{
		super(other);
		this.edgeFilter = edgeFilter;
	}
	
	public Predicate<E> getEdgeFilter()
	{ return this.edgeFilter; }
	
	public void reevaluate()
	{
		List<Set<NodeGroup<N>>> sets;
		
		do
		{
			sets = new ArrayList<>();
			
			if (this.edgeFilter != null)
				for (NodeGroup<N> node : this.getNodes()) for (MimickedEdge<NodeGroup<N>, E> edge : this.getEdges(node))
					if (!this.edgeFilter.test(edge.getOriginalEdge())) if (edge.getSource()
							.equals(edge.getTarget()))
						this.removeEdge(edge);
					else
			{
				Set<NodeGroup<N>> sourceSet = sets.stream()
						.filter(s -> s.contains(edge.getSource()))
						.findAny()
						.orElse(null);
				Set<NodeGroup<N>> targetSet = sets.stream()
						.filter(s -> s.contains(edge.getTarget()))
						.findAny()
						.orElse(null);
				
				if (sourceSet != null && targetSet != null)
				{
					if (sourceSet != targetSet)
					{
						if (!sets.remove(targetSet)) throw new IllegalStateException();
						sourceSet.addAll(targetSet);
					}
				}
				else if (sourceSet != null) sourceSet.add(edge.getTarget());
				else if (targetSet != null) targetSet.add(edge.getSource());
				else
				{
					Set<NodeGroup<N>> set = new HashSet<>();
					set.add(edge.getSource());
					set.add(edge.getTarget());
					sets.add(set);
				}
			}
					
			this.mergeAllGroups(sets);
		}
		while (!sets.isEmpty());
		
	}
	
}
