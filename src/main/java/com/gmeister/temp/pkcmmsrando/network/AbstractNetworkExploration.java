package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNetworkExploration<N extends Node, E extends Edge<? extends N>> implements NetworkExploration<N, E>
{
	
	protected List<N> nodesToExplore;
	protected List<N> nodesAccessed;
	protected List<E> edgesAccessed;
	
	public AbstractNetworkExploration()
	{
		super();
		this.nodesToExplore = new ArrayList<>();
		this.nodesAccessed = new ArrayList<>();
		this.edgesAccessed = new ArrayList<>();
	}
	
	public AbstractNetworkExploration(List<N> nodesToExplore, List<N> nodesAccessed, List<E> edgesAccessed)
	{
		super();
		this.nodesToExplore = nodesToExplore;
		this.nodesAccessed = nodesAccessed;
		this.edgesAccessed = edgesAccessed;
	}
	
	public AbstractNetworkExploration(AbstractNetworkExploration<? extends N, ? extends E> other)
	{
		super();
		this.nodesToExplore = new ArrayList<>(other.nodesToExplore);
		this.nodesAccessed = new ArrayList<>(other.nodesAccessed);
		this.edgesAccessed = new ArrayList<>(other.edgesAccessed);
	}
	
	public List<N> getNodesToExplore()
	{ return this.nodesToExplore; }
	
	public List<N> getNodesAccessed()
	{ return new ArrayList<>(this.nodesAccessed); }
	
	public List<E> getEdgesAccessed()
	{ return new ArrayList<>(this.edgesAccessed); }
	
	public abstract void exploreEdge(E edge);
	
}
