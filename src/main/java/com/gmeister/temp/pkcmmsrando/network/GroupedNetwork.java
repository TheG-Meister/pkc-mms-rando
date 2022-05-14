package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class GroupedNetwork<N extends Node, E extends Edge<? extends N>> extends Network<NodeGroup<N>, MimickedEdge<NodeGroup<N>, E>>
{
	
	private Predicate<E> edgeFilter;
	private final Map<N, NodeGroup<N>> nodeMap;
	private final Map<E, MimickedEdge<NodeGroup<N>, E>> edgeMap;

	public GroupedNetwork()
	{
		super();
		this.nodeMap = new HashMap<>();
		this.edgeMap = new HashMap<>();
	}

	public GroupedNetwork(Collection<? extends NodeGroup<N>> nodes, Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges,
			Predicate<E> edgeFilter)
	{
		this();
		
		this.edgeFilter = edgeFilter;
		this.addNodes(nodes);
		this.addEdges(edges);
	}

	public GroupedNetwork(Network<? extends N, ? extends E> other, Predicate<E> edgeFilter)
	{
		this();
		
		this.edgeFilter = edgeFilter;
		this.addOriginalNodes(other.getNodes());
		this.addOriginalEdges(other.getEdges());
	}
	
	public GroupedNetwork(GroupedNetwork<N, E> other, Predicate<E> edgeFilter)
	{
		//create copies of everything?
		//Everything is basically final so it should be fine
		super(other);
		this.nodeMap = new HashMap<>(other.nodeMap);
		this.edgeMap = new HashMap<>(other.edgeMap);
		this.edgeFilter = edgeFilter;
	}

	public Predicate<E> getEdgeFilter()
	{ return this.edgeFilter; }
	
	public void setEdgeFilter(Predicate<E> edgeFilter)
	{
		this.edgeFilter = edgeFilter;
		this.reevaluate();
	}
	
	public void addOriginalNodes(Collection<? extends N> nodes)
	{
		//null check on nodes itself
		if (nodes.contains(null)) throw new IllegalArgumentException("nodes must not contain null elements");
		
		for (N node : nodes) this.addOriginalNode(node);
	}
	
	public void addOriginalNode(N node)
	{
		//null check
		
		this.addNode(new NodeGroup<>(node));
	}
	
	@Override
	public void addNode(NodeGroup<N> node)
	{
		//null check
		
		super.addNode(node);
		for (N subNode : node.getNodes()) this.nodeMap.put(subNode, node);
	}
	
	protected void validateOriginalEdge(E edge)
	{
		if (edge == null) throw new IllegalArgumentException("edge must not be null");
		if (edge.getSource() == null) throw new IllegalArgumentException("edge source must not be null");
		if (edge.getTarget() == null) throw new IllegalArgumentException("edge target must not be null");
		if (!this.nodeMap.containsKey(edge.getSource())) throw new IllegalArgumentException("edge source must be part of the network");
		if (!this.nodeMap.containsKey(edge.getTarget())) throw new IllegalArgumentException("edge target must be part of the network");
	}
	
	public void addOriginalEdges(Collection<? extends E> edges)
	{
		for (E edge : edges) this.validateOriginalEdge(edge);
		
		for (E edge : edges) this.addOriginalEdge(edge);
	}
	
	public void addOriginalEdge(E edge)
	{
		this.validateOriginalEdge(edge);
		
		this.addEdge(new MimickedEdge<NodeGroup<N>, E>(this.nodeMap.get(edge.getSource()), this.nodeMap.get(edge.getTarget()), edge));
	}

	@Override
	public void addEdge(MimickedEdge<NodeGroup<N>, E> edge)
	{
		this.validateEdge(edge);
		
		//Checks for whether the original edge matches the new edge? Could be done in MimickedEdge
		
		if (this.edgeFilter == null || this.edgeFilter.test(edge.getOriginalEdge()))
		{
			super.addEdge(edge);
			this.edgeMap.put(edge.getOriginalEdge(), edge);
		}
		else if (!edge.getSource().equals(edge.getTarget())) this.merge(edge.getSource(), edge.getTarget());
	}
	
	public void merge(NodeGroup<N> node, NodeGroup<N> otherNode)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (otherNode == null) throw new IllegalArgumentException("otherNode must not be null");
		if (!this.getNodes().contains(node)) throw new IllegalArgumentException("node must be part of the network");
		if (!this.getNodes().contains(otherNode)) throw new IllegalArgumentException("otherNode must be part of the network");
		if (node.equals(otherNode)) throw new IllegalArgumentException("node and otherNode must not be equal");
		
		Set<NodeGroup<N>> nodeGroups = new HashSet<>();
		nodeGroups.add(node);
		nodeGroups.add(otherNode);
		this.merge(nodeGroups);
	}
	
	public void merge(Collection<NodeGroup<N>> nodeGroups)
	{
		Set<Collection<NodeGroup<N>>> collections = new HashSet<>();
		collections.add(nodeGroups);
		this.mergeAll(collections);
	}
	
	public void mergeAll(Collection<? extends Collection<NodeGroup<N>>> collections)
	{
		//No node group should be present multiple times
		if (collections == null) throw new IllegalArgumentException("collections must not be null");
		
		for (Collection<NodeGroup<N>> collection : collections)
		{
			if (collection == null) throw new IllegalArgumentException("collections must not contain null elements");
			if (new HashSet<>(collection).size() < 2) throw new IllegalArgumentException("collection must only contain elements that contain 2 or more unique elements");
			for (NodeGroup<N> node : collection) this.validateNode(node);
		}
		
		Set<MimickedEdge<NodeGroup<N>, E>> modifiedEdges = new HashSet<>();
		for (Collection<NodeGroup<N>> collection : collections)
		{
			Set<N> nodes = new HashSet<>();
			for (NodeGroup<N> node : collection) nodes.addAll(node.getNodes());
			NodeGroup<N> nodeGroup = new NodeGroup<>(nodes);
			
			for (NodeGroup<N> node : this.getNodes()) for (MimickedEdge<NodeGroup<N>, E> edge : this.getEdges(node))
			{
				if (collection.contains(edge.getSource()) || collection.contains(edge.getTarget())) modifiedEdges.add(edge);
			}
			
			for (NodeGroup<N> node : collection) this.removeNode(node);
			
			super.addNode(nodeGroup);
			for (N node : nodes) this.nodeMap.put(node, nodeGroup);
		}
		
		for (MimickedEdge<NodeGroup<N>, E> edge : modifiedEdges)
		{
			MimickedEdge<NodeGroup<N>, E> newEdge = new MimickedEdge<>(this.nodeMap.get(edge.getOriginalEdge().getSource()), this.nodeMap.get(edge.getOriginalEdge().getTarget()), edge.getOriginalEdge());
			super.addEdge(newEdge);
			this.edgeMap.put(edge.getOriginalEdge(), newEdge);
		}
		
	}
	
	public void removeOriginalNode(N node)
	{
		//null check
		this.removeNode(this.nodeMap.get(node));
	}
	
	public void removeOriginalEdge(E edge)
	{
		//null check
		this.removeEdge(this.edgeMap.get(edge));
	}
	
	@Override
	public void removeNode(NodeGroup<N> node)
	{
		super.removeNode(node);
		for (N subNode : node.getNodes()) this.nodeMap.remove(subNode);
	}
	
	@Override
	public void removeEdge(MimickedEdge<NodeGroup<N>, E> edge)
	{
		super.removeEdge(edge);
		this.edgeMap.remove(edge.getOriginalEdge());
	}
	
	public NodeGroup<N> getNode(N node)
	{
		return this.nodeMap.get(node);
	}
	
	public MimickedEdge<NodeGroup<N>, E> getEdge(E edge)
	{
		return this.edgeMap.get(edge);
	}
	
	public Set<E> getOriginalEdges()
	{
		return new HashSet<>(this.edgeMap.keySet());
	}
	
	public void splitNodeGroups(Collection<NodeGroup<N>> nodeGroups, Collection<? extends E> edges)
	{
		for (NodeGroup<N> nodeGroup : nodeGroups)
		{
			if (nodeGroup == null) throw new IllegalArgumentException("nodeGroup must not be null");
			if (!this.getNodes().contains(nodeGroup)) throw new IllegalArgumentException("nodeGroup must be part of the network");
		}
		
		if (edges == null) throw new IllegalArgumentException("edges must not be null");
		Set<E> edgeSet = new HashSet<>(edges);
		edgeSet.remove(null);
		
		for (NodeGroup<N> nodeGroup : nodeGroups)
		{
			this.removeNode(nodeGroup);
			for (N node : nodeGroup.getNodes()) this.addOriginalNode(node);
		}
		
		for (E edge : edgeSet)
		{
			if (this.nodeMap.containsKey(edge.getSource()) && this.nodeMap.containsKey(edge.getTarget())) this.addOriginalEdge(edge);
		}
	}
	
	public void reevaluate()
	{
		List<Set<NodeGroup<N>>> sets;
		
		do
		{
			sets = new ArrayList<>();
			
			if (this.edgeFilter != null) for (NodeGroup<N> node : this.getNodes()) for (MimickedEdge<NodeGroup<N>, E> edge : this.getEdges(node)) if (!this.edgeFilter.test(edge.getOriginalEdge()))
			{
				if (edge.getSource().equals(edge.getTarget())) this.removeEdge(edge);
				else
				{
					Set<NodeGroup<N>> sourceSet = sets.stream().filter(s -> s.contains(edge.getSource())).findAny().orElse(null);
					Set<NodeGroup<N>> targetSet = sets.stream().filter(s -> s.contains(edge.getTarget())).findAny().orElse(null);
					
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
			}
			
			if (!sets.isEmpty()) this.mergeAll(sets);
		}
		while (!sets.isEmpty());
		
	}
	
}
