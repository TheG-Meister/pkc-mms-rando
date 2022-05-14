package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;

public class WarpNetwork<N extends Node, E extends Edge<? extends N>> extends Network<N, E>
{
	
	//no collapse method
	//components
	//grouped network
	
	private ComponentNetwork<N, E> componentNetwork;
	private UnreturnableNetwork<N, E> unreturnableNetwork;
	
	public WarpNetwork()
	{
		super();
		
		this.componentNetwork = new ComponentNetwork<>(this);
		this.unreturnableNetwork = new UnreturnableNetwork<>(this);
	}
	
	public WarpNetwork(Collection<? extends N> nodes, Collection<? extends E> edges)
	{
		super();
		
		for (N node : nodes) super.addNode(node);
		for (E edge : edges) super.addEdge(edge);
		
		this.componentNetwork = new ComponentNetwork<>(this);
		this.unreturnableNetwork = new UnreturnableNetwork<>(this);
	}
	
	public WarpNetwork(WarpNetwork<N, E> other)
	{
		super(other);
		
		this.componentNetwork = new ComponentNetwork<N, E>(other.componentNetwork);
		this.unreturnableNetwork = new UnreturnableNetwork<>(other.unreturnableNetwork);
	}
	
	public ComponentNetwork<N, E> getComponentNetwork()
	{ return this.componentNetwork; }
	
	public UnreturnableNetwork<N, E> getUnreturnableNetwork()
	{ return this.unreturnableNetwork; }

	@Override
	public void addNode(N node)
	{
		super.addNode(node);
		this.componentNetwork.addOriginalNode(node);
		this.unreturnableNetwork.addOriginalNode(node);
		this.componentNetwork.reevaluate();
		this.unreturnableNetwork.reevaluate();
	}
	
	@Override
	public void addEdge(E edge)
	{
		super.addEdge(edge);
		this.componentNetwork.addOriginalEdge(edge);
		this.unreturnableNetwork.addOriginalEdge(edge);
		this.componentNetwork.reevaluate();
		this.unreturnableNetwork.reevaluate();
	}
	
	@Override
	public void removeNode(N node)
	{
		super.removeNode(node);
		this.componentNetwork.removeOriginalNode(node);
		this.unreturnableNetwork.removeOriginalNode(node);
		this.componentNetwork.reevaluate();
		this.unreturnableNetwork.reevaluate();
	}
	
	@Override
	public void removeEdge(E edge)
	{
		super.removeEdge(edge);
		
		this.componentNetwork = new ComponentNetwork<>(this);
		this.unreturnableNetwork = new UnreturnableNetwork<>(this);
	}
	
}
