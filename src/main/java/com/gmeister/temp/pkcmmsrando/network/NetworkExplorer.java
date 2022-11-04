package com.gmeister.temp.pkcmmsrando.network;

import java.util.function.Predicate;

public class NetworkExplorer<N extends Node, E extends Edge<? extends N>>
{
	
	private Network<N, E> network;
	private NetworkExploration<N, E> exploration;
	private Predicate<E> travelCondition;
	
	public NetworkExplorer(Network<N, E> network, Predicate<E> travelCondition, NetworkExploration<N, E> exploration)
	{
		super();
		this.network = network;
		this.exploration = exploration;
		this.travelCondition = travelCondition;
	}
	
	//What about exploring backwards? How do we implement that?
	public void exploreForwards()
	{
		if (this.exploration.hasNextNode())
		{
			N source = this.exploration.getNextNode();
			
			for (E edge : this.network.getEdges(source)) if (this.travelCondition.test(edge)) this.exploration.exploreEdge(edge);
		}
	}
	
	public Network<N, E> getNetwork()
	{ return this.network; }
	
	public NetworkExploration<N, E> getExploration()
	{ return this.exploration; }
	
	public Predicate<E> getTravelCondition()
	{ return this.travelCondition; }
	
}
