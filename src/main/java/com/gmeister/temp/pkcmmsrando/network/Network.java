package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Network<N extends Node, E extends Edge<N>>
{
	
	private Set<N> nodes;
	private Map<N, Set<E>> edgeMap;
	private Set<Set<N>> components;
	
	public Network(Collection<? extends N> nodes, Collection<? extends E> edges)
	{
		this.nodes = new HashSet<>();
		this.edgeMap = new HashMap<>();
		this.components = new HashSet<>();
		
		this.addNodes(nodes);
		this.addEdges(edges);
	}
	
	public Network(Network<? extends N, ? extends E> other)
	{
		this.nodes = new HashSet<>();
		this.edgeMap = new HashMap<>();
		this.components = new HashSet<>();
		
		this.addNodes(other.getNodes());
		
		Set<E> edges = new HashSet<>();
		for (Set<? extends E> nodeEdges : other.edgeMap.values()) edges.addAll(nodeEdges);
		this.addEdges(edges);
	}
	
	public Set<N> getNodes()
	{ return new HashSet<>(this.nodes); }
	
	public Set<Set<N>> getComponents()
	{
		Set<Set<N>> components = new HashSet<>();
		for (Set<N> component : this.components) components.add(new HashSet<>(component));
		return components;
	}
	
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
		
		Set<N> component = new HashSet<>();
		component.add(node);
		this.components.add(component);
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
		
		Set<N> sourceComponent = this.getComponentOf(edge.getSource());
		Set<N> targetComponent = this.getComponentOf(edge.getTarget());
		if (sourceComponent != targetComponent) this.mergeComponents(sourceComponent, targetComponent);
	}
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget");
		for (N key : this.edgeMap.keySet()) for (E edge : this.edgeMap.get(key))
			System.out.println(
					edge.getSource() + "\t" +
					edge.getTarget());
	}
	
	/**
	 * Initialises the set of components. This can be used to recalculate the components of this network if any of the nodes or edges have been modified.
	 */
	public void initComponents()
	{
		this.components = new HashSet<>();
		Map<N, Set<N>> componentMap = new HashMap<>();
		
		for (N node : this.nodes)
		{
			Set<N> component = new HashSet<>();
			component.add(node);
			this.components.add(component);
			componentMap.put(node, component);
		}
		
		for (N source : this.edgeMap.keySet())
		{
			Set<N> sourceComponent = componentMap.get(source);
			for (E edge : this.getEdgeEntry(source)) if (!sourceComponent.contains(edge.getTarget()))
			{
				Set<N> targetComponent = componentMap.get(edge.getTarget());
				this.mergeComponents(sourceComponent, targetComponent);
				for (N node : targetComponent) componentMap.put(node, sourceComponent);
			}
		}
	}
	
	/**
	 * Merges two components into a single component. More specifically, puts all elements of <code>otherComponent</code> into <code>component</code>.
	 * @param component
	 * @param otherComponent
	 */
	private void mergeComponents(Set<N> component, Set<N> otherComponent)
	{
		if (!this.components.contains(component)) throw new IllegalArgumentException("component must be part of the network");
		if (!this.components.contains(otherComponent)) throw new IllegalArgumentException("otherComponent must be part of the network");
		
		if (component == otherComponent) throw new IllegalArgumentException("component and otherComponent must not be the same object");
		
		component.addAll(otherComponent);
		this.components.remove(otherComponent);
	}
	
	//This should probably return a copy, as a getting class could modify the component
	public Set<N> getComponentOf(N node)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (!this.nodes.contains(node)) throw new IllegalArgumentException("node must be part of the network");
		
		return this.components.stream().filter(c -> c.contains(node)).findAny().orElseThrow();
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
	
}
