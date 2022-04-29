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
	
	private Set<N> nodes;
	private Map<N, Set<E>> edgeMap;
	
	public Network()
	{
		this.nodes = new HashSet<>();
		this.edgeMap = new HashMap<>();
	}
	
	public Network(Collection<? extends N> nodes, Collection<? extends E> edges)
	{
		this();
		
		this.addNodes(nodes);
		this.addEdges(edges);
	}
	
	public Network(Network<? extends N, ? extends E> other)
	{
		this();
		
		this.addNodes(other.getNodes());
		
		Set<E> edges = new HashSet<>();
		for (Set<? extends E> nodeEdges : other.edgeMap.values()) edges.addAll(nodeEdges);
		this.addEdges(edges);
	}
	
	public Set<N> getNodes()
	{ return new HashSet<>(this.nodes); }
	
	private Set<E> getEdgeEntry(N source)
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
	
	public void addNodes(Collection<? extends N> nodes)
	{
		if (nodes == null) throw new IllegalArgumentException("nodes must not be null");
		if (nodes.contains(null)) throw new IllegalArgumentException("nodes must not contain null elements");
		
		for (N node : nodes) this.addNode(node);
	}
	
	protected void validateNode(N node)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (!this.nodes.contains(node)) throw new IllegalArgumentException("node must be part of the network");
	}
	
	public void addNode(N node)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		
		this.nodes.add(node);
		this.edgeMap.put(node, new HashSet<>());
	}
	
	public void removeNode(N node)
	{
		this.validateNode(node);
		
		for (N source : this.edgeMap.keySet())
		{
			Set<E> edges = this.getEdges(source);
			for (E edge : edges) if (edge.getSource().equals(node) || edge.getTarget().equals(node)) this.removeEdge(edge);
		}

		this.edgeMap.remove(node);
		this.nodes.remove(node);
	}
	
	protected void validateEdge(E edge)
	{
		if (edge == null) throw new IllegalArgumentException("edge must not be null");
		
		if (edge.getSource() == null) throw new IllegalArgumentException("edge source must not be null");
		if (edge.getTarget() == null) throw new IllegalArgumentException("edge target must not be null");
		
		if (!this.nodes.contains(edge.getSource())) throw new IllegalArgumentException("edge source must be part of the network");
		if (!this.nodes.contains(edge.getTarget())) throw new IllegalArgumentException("edge target must be part of the network");
	}
	
	public void addEdges(Collection<? extends E> edges)
	{
		if (edges == null) throw new IllegalArgumentException("edges must not be null");
		
		//Validate all edges
		for (E edge : edges) this.validateEdge(edge);
		
		//Add all edges
		for (E edge : edges) this.addEdge(edge);
	}
	
	public void addEdge(E edge)
	{
		this.validateEdge(edge);
		
		this.getEdgeEntry(edge.getSource())
			.add(edge);
	}
	
	public void removeEdge(E edge)
	{
		this.validateEdge(edge);
		
		this.getEdgeEntry(edge.getSource()).remove(edge);
	}
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget");
		for (N key : this.edgeMap.keySet()) for (E edge : this.edgeMap.get(key))
			System.out.println(
					edge.getSource() + "\t" +
					edge.getTarget());
	}
	
	public boolean hasPath(N source, N target)
	{
		this.validateNode(source);
		this.validateNode(target);
		
		List<N> targets = new ArrayList<>();
		targets.add(source);
		
		for (int i = 0; i < targets.size(); i++)
		{
			N node = targets.get(i);
			for (E edge : this.getEdgeEntry(node))
			{
				if (edge.getTarget().equals(target)) return true;
				else if (!targets.contains(edge.getTarget())) targets.add(edge.getTarget());
			}
		}
		
		return false;
	}
	
	//This method is similar to hasPath
	public boolean hasConnection(N source, N target)
	{
		this.validateNode(source);
		this.validateNode(target);
		
		List<N> nodes = new ArrayList<>();
		nodes.add(source);
		
		for (int i = 0; i < nodes.size(); i++)
		{
			//Get the node
			N node = nodes.get(i);
			//Create a list of connections to analyse
			List<N> connections = new ArrayList<>();
			
			//Add all nodes that have an edge arriving at node
			for (N otherNode : this.edgeMap.keySet())
				for (E edge : this.getEdgeEntry(otherNode))
					if (edge.getTarget().equals(node))
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
	
}
