package com.gmeister.temp.pkcmmsrando.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.gmeister.temp.pkcmmsrando.map.data.Warp;

public class WarpNode extends Node
{
	
	private final Set<Warp> warps;

	public WarpNode(Collection<? extends Warp> warps)
	{
		super();
		this.warps = new HashSet<>(warps);
	}

	public Set<Warp> getWarps()
	{ return this.warps; }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.warps == null) ? 0 : this.warps.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		WarpNode other = (WarpNode) obj;
		if (this.warps == null)
		{
			if (other.warps != null) return false;
		}
		else if (!this.warps.equals(other.warps)) return false;
		return true;
	}

	@Override
	public String toString()
	{ return "WarpNode [warps=" + this.warps + "]"; }
	
}
