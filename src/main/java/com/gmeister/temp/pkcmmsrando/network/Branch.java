package com.gmeister.temp.pkcmmsrando.network;

public class Branch<N>
{
	
	private final N source;
	private final N target;
	
	public Branch(N source, N target)
	{
		super();
		this.source = source;
		this.target = target;
	}
	
	public N getSource()
	{ return this.source; }
	
	public N getTarget()
	{ return this.target; }
	
}
