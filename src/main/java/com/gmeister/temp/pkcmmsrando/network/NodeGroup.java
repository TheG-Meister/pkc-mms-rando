package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NodeGroup<N extends Node> extends Node
{
	
	private final Set<N> nodes;
	
	public NodeGroup(N node)
	{
		super();
		this.nodes = new HashSet<>();
		this.nodes.add(node);
	}

	public NodeGroup(Collection<? extends N> nodes)
	{
		super();
		this.nodes = new HashSet<>(nodes);
	}

	public final Set<N> getNodes()
	{ return new HashSet<>(this.nodes); }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.nodes == null) ? 0 : this.nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NodeGroup<?> other = (NodeGroup<?>) obj;
		if (this.nodes == null)
		{
			if (other.nodes != null) return false;
		}
		else if (!this.nodes.equals(other.nodes)) return false;
		return true;
	}

	@Override
	public String toString()
	{ return "NodeGroup [nodes=" + this.nodes + "]"; }
	
}
