package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.gmeister.temp.pkcmmsrando.map.data.Flag;

public class FlaggedEdge<N extends Node> extends Edge<N>
{
	
	private final Set<Flag> flags;

	public FlaggedEdge(N source, N target, Collection<? extends Flag> flags)
	{
		super(source, target);
		this.flags = new HashSet<>(flags);
	}
	
	public FlaggedEdge(Edge<? extends N> other)
	{
		super(other.getSource(), other.getTarget());
		this.flags = new HashSet<>();
	}
	
	public FlaggedEdge(N source, N target, FlaggedEdge<?> other)
	{
		super(source, target);
		this.flags = new HashSet<>(other.getFlags());
	}

	public Set<Flag> getFlags()
	{ return new HashSet<>(this.flags); }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.flags == null) ? 0 : this.flags.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		FlaggedEdge<?> other = (FlaggedEdge<?>) obj;
		if (this.flags == null)
		{
			if (other.flags != null) return false;
		}
		else if (!this.flags.equals(other.flags)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "FlaggedEdge [flags=" + this.flags + ", getSource()=" + this.getSource() + ", getTarget()="
				+ this.getTarget() + "]";
	}
	
}
