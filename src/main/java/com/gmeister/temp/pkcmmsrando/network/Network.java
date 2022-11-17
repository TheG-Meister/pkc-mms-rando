package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Network<N extends Node, E extends Edge<? extends N>>
{
	
	protected Set<N> nodes;
	protected Map<N, Set<E>> edgeMap;
	
	public Network()
	{ this.initialise(); }
	
	public Network(int nodeCapacity, int edgeCapacity)
	{ this.initialise(nodeCapacity, edgeCapacity); }
	
	public Network(Collection<? extends N> nodes, Collection<? extends E> edges)
	{ this.initialise(nodes, edges); }
	
	public Network(Network<? extends N, ? extends E> other)
	{ this.initialise(other); }
	
	private void initialise()
	{
		this.nodes = new HashSet<>();
		this.edgeMap = new HashMap<>();
	}
	
	private void initialise(int nodeCapacity, int edgeCapacity)
	{
		this.nodes = new HashSet<>(nodeCapacity);
		this.edgeMap = new HashMap<>(edgeCapacity);
	}
	
	private void initialise(Collection<? extends N> nodes, Collection<? extends E> edges)
	{
		this.initialise(nodes.size(), edges.size());
		
		this.addNodes(nodes);
		this.addEdges(edges);
	}
	
	private void initialise(Network<? extends N, ? extends E> other)
	{
		this.nodes = new HashSet<>(other.nodes);
		this.edgeMap = new HashMap<>(other.edgeMap.size());
		for (N node : other.edgeMap.keySet()) this.edgeMap.put(node, new HashSet<>(other.edgeMap.get(node)));
	}
	
	public Set<N> getNodes()
	{ return new HashSet<>(this.nodes); }
	
	protected Set<E> getEdgeEntry(N source)
	{
		if (source == null) throw new IllegalArgumentException("source node must not be null");
		if (!this.nodes.contains(source)) throw new IllegalArgumentException("source node must be part of the network");
		
		Set<E> edges = this.edgeMap.get(source);
		//This should never be the case as addNode also adds entries to edgeMap
		if (edges == null)
		{
			edges = new HashSet<>();
			this.edgeMap.put(source, edges);
		}
		return edges;
	}
	
	public Set<E> getEdges(N source)
	{ return new HashSet<>(this.getEdgeEntry(source)); }
	
	public Set<E> getEdges()
	{
		Set<E> edges = new HashSet<>();
		for (N node : this.edgeMap.keySet()) edges.addAll(this.getEdgeEntry(node));
		
		return edges;
	}
	
	/**
	 * Throws exceptions for any node that may be considered by this network.
	 * @param node the node being considered
	 * @param name string identifier for the node to include in exception messages
	 * @throws IllegalArgumentException if the node is null
	 */
	protected void validateNode(N node, String name) throws IllegalArgumentException
	{
		if (node == null) throw new IllegalArgumentException(name + " must not be null");
	}

	/**
	 * Throws exceptions for any node that may be added to this network. Runs {@link Network#validateNode(Node, String) validateNode} first.
	 * @param node the node being considered
	 * @param name string identifier for the node to include in exception messages
	 * @throws IllegalArgumentException if the node is already part of the network
	 */
	protected void validateNewNode(N node, String name) throws IllegalArgumentException
	{
		this.validateNode(node, name);
		if (this.nodes.contains(node)) throw new IllegalArgumentException(name + " must not be part of the network");
	}

	/**
	 * Throws exceptions for any node that should be part of this network. Runs {@link Network#validateNode(Node, String) validateNode} first.
	 * @param node the node being considered
	 * @param name string identifier for the node to include in exception messages
	 * @throws IllegalArgumentException if the node is not part of the network
	 */
	protected void validateExistingNode(N node, String name) throws IllegalArgumentException
	{
		this.validateNode(node, name);
		if (!this.nodes.contains(node)) throw new IllegalArgumentException(name + " must be part of the network");
	}
	
	public void addNodes(Collection<? extends N> nodes)
	{
		if (nodes == null) throw new IllegalArgumentException("nodes must not be null");
		for (N node : nodes) this.validateNewNode(node, "node");
		for (N node : nodes) this.addKnownNode(node);
	}
	
	public void addNode(N node)
	{
		this.validateNewNode(node, "node");
		this.addKnownNode(node);
	}
	
	protected void addKnownNode(N node)
	{
		this.nodes.add(node);
		this.edgeMap.put(node, new HashSet<>());
	}
	
	public void removeNode(N node)
	{
		this.validateExistingNode(node, "node");
		this.removeKnownNode(node);
	}
	
	protected void removeKnownNode(N node)
	{
		for (N source : this.edgeMap.keySet())
		{
			Set<E> edges = this.getEdges(source);
			for (E edge : edges) if (edge.getSource()
					.equals(node)
					|| edge.getTarget()
							.equals(node))
				this.removeEdge(edge);
		}
		
		this.edgeMap.remove(node);
		this.nodes.remove(node);
	}
	
	protected void validateEdge(E edge, String name)
	{
		if (edge == null) throw new IllegalArgumentException(name + " must not be null");
		if (edge.getSource() == null) throw new IllegalArgumentException("edge source must not be null");
		if (edge.getTarget() == null) throw new IllegalArgumentException("edge target must not be null");
	}
	
	protected void validateNewEdge(E edge, String name)
	{
		this.validateEdge(edge, name);
		if (this.getEdgeEntry(edge.getSource()).contains(edge)) throw new IllegalArgumentException(name + " must not be part of the network");
	}
	
	protected void validateExistingEdge(E edge, String name)
	{
		this.validateEdge(edge, name);
		if (!this.nodes.contains(edge.getSource()))
			throw new IllegalArgumentException("edge source must be part of the network");
		if (!this.nodes.contains(edge.getTarget()))
			throw new IllegalArgumentException("edge target must be part of the network");
	}
	
	public void addEdges(Collection<? extends E> edges)
	{
		if (edges == null) throw new IllegalArgumentException("edges must not be null");
		for (E edge : edges) this.validateNewEdge(edge, "edge");
		for (E edge : edges) this.addKnownEdge(edge);
	}
	
	public void addEdge(E edge)
	{
		this.validateNewEdge(edge, "edge");
		this.addKnownEdge(edge);
	}
	
	protected void addKnownEdge(E edge)
	{
		this.getEdgeEntry(edge.getSource())
		.add(edge);
	}
	
	public void removeEdge(E edge)
	{
		this.validateExistingEdge(edge, "edge");
		this.removeKnownEdge(edge);
	}
	
	protected void removeKnownEdge(E edge)
	{
		this.getEdgeEntry(edge.getSource())
		.remove(edge);
	}
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget");
		for (N key : this.edgeMap.keySet())
			for (E edge : this.edgeMap.get(key)) System.out.println(edge.getSource() + "\t" + edge.getTarget());
	}
	
	public boolean hasPath(N source, N target)
	{
		this.validateExistingNode(source, "source");
		this.validateExistingNode(target, "target");
		
		List<N> targets = new ArrayList<>();
		targets.add(source);
		
		for (int i = 0; i < targets.size(); i++)
		{
			N node = targets.get(i);
			for (E edge : this.getEdgeEntry(node))
			{
				if (edge.getTarget()
						.equals(target))
					return true;
				else if (!targets.contains(edge.getTarget())) targets.add(edge.getTarget());
			}
		}
		
		return false;
	}
	
	//This method is similar to hasPath
	public boolean hasConnection(N source, N target)
	{
		this.validateExistingNode(source, "source");
		this.validateExistingNode(target, "target");
		
		List<N> nodes = new ArrayList<>();
		nodes.add(source);
		
		for (int i = 0; i < nodes.size(); i++)
		{
			//Get the node
			N node = nodes.get(i);
			//Create a list of connections to analyse
			List<N> connections = new ArrayList<>();
			
			//Add all nodes that have an edge arriving at node
			for (N otherNode : this.edgeMap.keySet()) for (E edge : this.getEdgeEntry(otherNode)) if (edge.getTarget()
					.equals(node))
				connections.add(edge.getSource());
			
			//Add all nodes that have an edge arriving from node 
			for (E edge : this.getEdgeEntry(node)) connections.add(edge.getTarget());
			
			//For all new nodes
			for (N otherNode : connections) if (!nodes.contains(otherNode))
			{
				//Return true if this node equals the target node
				if (otherNode.equals(target)) return true;
				//Otherwise add this node to nodes
				else nodes.add(otherNode);
			}
		}
		
		//Otherwise return false
		return false;
	}
	
	public boolean isSourceNode(N node)
	{ return !this.getEdgeEntry(node)
			.isEmpty(); }
	
	public boolean isTargetNode(N node)
	{
		for (N key : this.edgeMap.keySet()) for (E edge : this.getEdgeEntry(key)) if (edge.getTarget()
				.equals(node))
			return true;
		return false;
	}
	
	public Set<N> getTargetNodes()
	{
		Set<N> nodes = new HashSet<>();
		for (N node : this.edgeMap.keySet()) for (E edge : this.getEdgeEntry(node)) nodes.add(edge.getTarget());
		
		return nodes;
	}
	
	public Set<N> getSourceNodes()
	{
		Set<N> nodes = new HashSet<>();
		for (N node : this.edgeMap.keySet()) if (!this.getEdges(node)
				.isEmpty())
			nodes.add(node);
		
		return nodes;
	}
	
	public Set<N> getTargetsOf(N node)
	{
		this.validateExistingNode(node, "node");
		
		List<N> nodes = new ArrayList<>();
		nodes.add(node);
		
		for (int i = 0; i < nodes.size(); i++) for (E edge : this.getEdgeEntry(nodes.get(i)))
			if (!nodes.contains(edge.getTarget())) nodes.add(edge.getTarget());
		
		return new HashSet<>(nodes);
	}
	
	public Set<N> getSourcesOf(N node)
	{
		this.validateExistingNode(node, "node");
		
		List<N> nodes = new ArrayList<>();
		nodes.add(node);
		
		for (int i = 0; i < nodes.size(); i++)
		{
			for (N key : this.getNodes()) for (E edge : this.getEdgeEntry(key)) if (edge.getTarget()
					.equals(nodes.get(i)) && !nodes.contains(edge.getSource()))
				nodes.add(edge.getSource());
		}
		
		return new HashSet<>(nodes);
	}
	
}
