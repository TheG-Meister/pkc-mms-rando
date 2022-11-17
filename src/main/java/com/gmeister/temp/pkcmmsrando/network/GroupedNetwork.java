package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupedNetwork<N extends Node, E extends Edge<? extends N>>
		extends Network<NodeGroup<N>, MimickedEdge<NodeGroup<N>, E>>
{
	
	protected Map<N, NodeGroup<N>> nodeMap;
	protected Map<E, MimickedEdge<NodeGroup<N>, E>> edgeMap;
	
	public GroupedNetwork()
	{
		super();
		this.nodeMap = new HashMap<>();
		this.edgeMap = new HashMap<>();
	}
	
	public GroupedNetwork(Collection<? extends NodeGroup<N>> nodes,
			Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges)
	{
		this();
		
		this.addNodes(nodes);
		this.addEdges(edges);
	}
	
	public GroupedNetwork(Network<? extends N, ? extends E> other)
	{
		this();
		
		this.addOriginalNodes(other.getNodes());
		this.addOriginalEdges(other.getEdges());
	}
	
	public GroupedNetwork(GroupedNetwork<N, E> other)
	{
		super(other);
		this.nodeMap = new HashMap<>(other.nodeMap);
		this.edgeMap = new HashMap<>(other.edgeMap);
	}
	
	@Override
	protected void validateNode(NodeGroup<N> node, String name)
	{
		super.validateNode(node, name);
		if (node.getNodes().contains(null)) throw new IllegalArgumentException(name + " must not contain null nodes");
	}
	
	@Override
	protected void addKnownNode(NodeGroup<N> node)
	{
		super.addKnownNode(node);
		for (N subNode : node.getNodes()) this.nodeMap.put(subNode, node);
	}
	
	@Override
	protected void validateEdge(MimickedEdge<NodeGroup<N>, E> edge, String name)
	{
		super.validateEdge(edge, name);
		if (edge.getOriginalEdge() == null) throw new IllegalArgumentException(name + " must not contain a null original edge");
	}
	
	@Override
	protected void addKnownEdge(MimickedEdge<NodeGroup<N>, E> edge)
	{
		super.addKnownEdge(edge);
		this.edgeMap.put(edge.getOriginalEdge(), edge);
	}
	
	@Override
	protected void removeKnownNode(NodeGroup<N> node)
	{
		super.removeKnownNode(node);
		for (N subNode : node.getNodes()) this.nodeMap.remove(subNode);
	}
	
	@Override
	protected void removeKnownEdge(MimickedEdge<NodeGroup<N>, E> edge)
	{
		super.removeKnownEdge(edge);
		this.edgeMap.remove(edge.getOriginalEdge());
	}
	
	private MimickedEdge<NodeGroup<N>, E> createEdge(E edge)
	{
		return new MimickedEdge<>(this.nodeMap.get(edge.getSource()), this.nodeMap.get(edge.getTarget()), edge);
	}
	
	public void addOriginalNodes(Collection<? extends N> nodes)
	{
		if (nodes == null) throw new IllegalArgumentException("nodes must not be null");
		
		Set<NodeGroup<N>> nodeGroups = new HashSet<>();
		for (N node : nodes)
		{
			NodeGroup<N> nodeGroup = new NodeGroup<>(node);
			this.validateNewNode(nodeGroup, "node");
			nodeGroups.add(nodeGroup);
		}
		
		for (NodeGroup<N> nodeGroup : nodeGroups) this.addKnownNode(nodeGroup);
	}
	
	public void addOriginalNode(N node)
	{
		NodeGroup<N> nodeGroup = new NodeGroup<>(node);
		this.validateNewNode(nodeGroup, "node");
		this.addKnownNode(nodeGroup);
	}
	
	public void addOriginalEdges(Collection<? extends E> edges)
	{
		if (edges == null) throw new IllegalArgumentException("nodes must not be null");
		
		Set<MimickedEdge<NodeGroup<N>, E>> mimickedEdges = new HashSet<>();
		for (E edge : edges)
		{
			MimickedEdge<NodeGroup<N>, E> mimickedEdge = this.createEdge(edge);
			this.validateNewEdge(mimickedEdge, "edge");
			mimickedEdges.add(mimickedEdge);
		}
		
		for (MimickedEdge<NodeGroup<N>, E> mimickedEdge : mimickedEdges) this.addKnownEdge(mimickedEdge);
	}
	
	public void addOriginalEdge(E edge)
	{
		MimickedEdge<NodeGroup<N>, E> mimickedEdge = this.createEdge(edge);
		this.validateNewEdge(mimickedEdge, "edge");
		this.addKnownEdge(mimickedEdge);
	}
	
	public void merge(MimickedEdge<NodeGroup<N>, E> edge)
	{
		if (!this.getEdges()
				.contains(edge))
			this.validateExistingEdge(edge, "edge");
		
		Set<NodeGroup<N>> nodeGroups = new HashSet<>();
		nodeGroups.add(edge.getSource());
		nodeGroups.add(edge.getTarget());
		this.merge(nodeGroups);
	}
	
	public void mergeAllEdges(Collection<? extends MimickedEdge<NodeGroup<N>, E>> edges)
	{
		for (MimickedEdge<NodeGroup<N>, E> edge : edges) if (!this.getEdges()
				.contains(edge))
			this.validateExistingEdge(edge, "edge");
		
		List<Set<NodeGroup<N>>> sets = new ArrayList<>();
		
		for (MimickedEdge<NodeGroup<N>, E> edge : edges) if (!edge.getSource()
				.equals(edge.getTarget()))
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
	
	public void merge(E edge)
	{
		MimickedEdge<NodeGroup<N>, E> mimickedEdge = this.createEdge(edge);
		this.validateEdge(mimickedEdge, "edge");
		
		Set<NodeGroup<N>> nodeGroups = new HashSet<>();
		nodeGroups.add(this.nodeMap.get(edge.getSource()));
		nodeGroups.add(this.nodeMap.get(edge.getTarget()));
		this.merge(nodeGroups);
	}
	
	public void merge(NodeGroup<N> node, NodeGroup<N> otherNode)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (otherNode == null) throw new IllegalArgumentException("otherNode must not be null");
		if (!this.getNodes()
				.contains(node))
			throw new IllegalArgumentException("node must be part of the network");
		if (!this.getNodes()
				.contains(otherNode))
			throw new IllegalArgumentException("otherNode must be part of the network");
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
		this.mergeAllGroups(collections);
	}
	
	public void mergeAllGroups(Collection<? extends Collection<NodeGroup<N>>> collections)
	{
		//No node group should be present multiple times
		if (collections == null) throw new IllegalArgumentException("collections must not be null");
		
		for (Collection<NodeGroup<N>> collection : collections)
		{
			if (collection == null) throw new IllegalArgumentException("collections must not contain null elements");
			if (new HashSet<>(collection).size() < 2) throw new IllegalArgumentException(
					"collection must only contain elements that contain 2 or more unique elements");
			for (NodeGroup<N> node : collection) this.validateExistingNode(node, "node");
		}
		
		Set<MimickedEdge<NodeGroup<N>, E>> modifiedEdges = new HashSet<>();
		for (Collection<NodeGroup<N>> collection : collections)
		{
			Set<N> nodes = new HashSet<>();
			for (NodeGroup<N> node : collection) nodes.addAll(node.getNodes());
			NodeGroup<N> nodeGroup = new NodeGroup<>(nodes);
			
			for (NodeGroup<N> node : this.getNodes()) for (MimickedEdge<NodeGroup<N>, E> edge : this.getEdges(node))
				if (collection.contains(edge.getSource()) || collection.contains(edge.getTarget()))
					modifiedEdges.add(edge);
			
			for (NodeGroup<N> node : collection) this.removeNode(node);
			
			super.addNode(nodeGroup);
			for (N node : nodes) this.nodeMap.put(node, nodeGroup);
		}
		
		for (MimickedEdge<NodeGroup<N>, E> edge : modifiedEdges)
		{
			MimickedEdge<NodeGroup<N>, E> newEdge = new MimickedEdge<>(this.nodeMap.get(edge.getOriginalEdge()
					.getSource()), this.nodeMap.get(
							edge.getOriginalEdge()
									.getTarget()),
					edge.getOriginalEdge());
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
	
	public NodeGroup<N> getNode(N node)
	{ return this.nodeMap.get(node); }
	
	public MimickedEdge<NodeGroup<N>, E> getEdge(E edge)
	{ return this.edgeMap.get(edge); }
	
	public Set<E> getOriginalEdges()
	{ return new HashSet<>(this.edgeMap.keySet()); }
	
	public void splitNodeGroups(Collection<NodeGroup<N>> nodeGroups, Collection<? extends E> edges)
	{
		for (NodeGroup<N> nodeGroup : nodeGroups)
		{
			if (nodeGroup == null) throw new IllegalArgumentException("nodeGroup must not be null");
			if (!this.getNodes()
					.contains(nodeGroup))
				throw new IllegalArgumentException("nodeGroup must be part of the network");
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
			if (this.nodeMap.containsKey(edge.getSource()) && this.nodeMap.containsKey(edge.getTarget()))
				this.addOriginalEdge(edge);
	}
	
}
