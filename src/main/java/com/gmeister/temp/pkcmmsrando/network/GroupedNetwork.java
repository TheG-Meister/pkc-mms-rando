package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class GroupedNetwork<N extends Node, E extends Edge<? extends N>> extends Network<NodeGroup<N>, MimickedEdge<NodeGroup<N>, E>>
{
	
	private final Predicate<E> edgeFilter;
	private final Map<N, NodeGroup<N>> nodeMap;
	private final Map<E, MimickedEdge<NodeGroup<N>, E>> edgeMap;

	public GroupedNetwork(Predicate<E> edgeFilter)
	{
		super();
		this.edgeFilter = edgeFilter;
		this.nodeMap = new HashMap<>();
		this.edgeMap = new HashMap<>();
	}

	public GroupedNetwork(Collection<? extends NodeGroup<N>> nodes, Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges,
			Predicate<E> edgeFilter)
	{
		this(edgeFilter);
		
		this.addNodes(nodes);
		this.addEdges(edges);
	}

	public GroupedNetwork(Network<? extends N, ? extends E> other, Predicate<E> edgeFilter)
	{
		this(edgeFilter);
		
		this.addOriginalNodes(other.getNodes());
		this.addOriginalEdges(other.getEdges());
	}
	
	public GroupedNetwork(GroupedNetwork<N, E> other, Predicate<E> edgeFilter)
	{
		//create copies of everything?
		//Everything is basically final so it should be fine
		this(other.getNodes(), other.getEdges(), edgeFilter);
	}

	public Predicate<E> getEdgeFilter()
	{ return this.edgeFilter; }
	
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
		
		if (this.edgeFilter.test(edge.getOriginalEdge()))
		{
			super.addEdge(edge);
			this.edgeMap.put(edge.getOriginalEdge(), edge);
		}
		else if (!edge.getSource().equals(edge.getTarget())) this.mergeNodeGroups(edge.getSource(), edge.getTarget());
	}
	
	public void mergeNodeGroups(NodeGroup<N> node, NodeGroup<N> otherNode)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (otherNode == null) throw new IllegalArgumentException("otherNode must not be null");
		if (!this.getNodes().contains(node)) throw new IllegalArgumentException("node must be part of the network");
		if (!this.getNodes().contains(otherNode)) throw new IllegalArgumentException("otherNode must be part of the network");
		if (node.equals(otherNode)) throw new IllegalArgumentException("node and otherNode must not be equal");
		
		Set<NodeGroup<N>> nodeGroups = new HashSet<>();
		nodeGroups.add(node);
		nodeGroups.add(otherNode);
		this.mergeNodeGroups(nodeGroups);
	}
	
	public void mergeNodeGroups(Collection<NodeGroup<N>> nodeGroups)
	{
		for (NodeGroup<N> node : nodeGroups) this.validateNode(node);
		
		Set<NodeGroup<N>> nodeGroupSet = new HashSet<>(nodeGroups);
		if (nodeGroupSet.size() < 2) throw new IllegalArgumentException("nodeGroups must contain more than one unique element");
		
		Set<N> nodes = new HashSet<>();
		for (NodeGroup<N> node : nodeGroupSet) nodes.addAll(node.getNodes());
		NodeGroup<N> mergedNode = new NodeGroup<>(nodes);
		
		Set<MimickedEdge<NodeGroup<N>, E>> edges = new HashSet<>();
		for (NodeGroup<N> nodeGroup : this.getNodes()) for (MimickedEdge<NodeGroup<N>, E> edge : this.getEdges(nodeGroup))
		{
			boolean containsSource = nodeGroupSet.contains(edge.getSource());
			boolean containsTarget = nodeGroupSet.contains(edge.getTarget());
			
			//Should this first condition instead remove the edge?
			if (containsSource && containsTarget) edges.add(new MimickedEdge<>(mergedNode, mergedNode, edge.getOriginalEdge()));
			else if (containsSource) edges.add(new MimickedEdge<>(mergedNode, edge.getTarget(), edge.getOriginalEdge()));
			else if (containsTarget) edges.add(new MimickedEdge<>(edge.getSource(), mergedNode, edge.getOriginalEdge()));
		}
		
		//Edges should get removed automatically
		for (NodeGroup<N> node : nodeGroupSet) this.removeNode(node);
		
		//Should update nodeMap automatically
		this.addNode(mergedNode);
		
		for (MimickedEdge<NodeGroup<N>, E> edge : edges)
		{
			super.addEdge(edge);
			//Do we also need to remove from the edge map? probably
			this.edgeMap.put(edge.getOriginalEdge(), edge);
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
	
}
