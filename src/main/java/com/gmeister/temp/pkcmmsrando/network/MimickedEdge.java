package com.gmeister.temp.pkcmmsrando.network;

public class MimickedEdge<N extends Node, E extends Edge<?>> extends Edge<N>
{
	
	private final E originalEdge;
	
	public MimickedEdge(N source, N target, E originalEdge)
	{
		super(source, target);
		this.originalEdge = originalEdge;
	}

	public E getOriginalEdge()
	{ return this.originalEdge; }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.originalEdge == null) ? 0 : this.originalEdge.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		MimickedEdge<?, ?> other = (MimickedEdge<?, ?>) obj;
		if (this.originalEdge == null)
		{
			if (other.originalEdge != null) return false;
		}
		else if (!this.originalEdge.equals(other.originalEdge)) return false;
		return true;
	}
	
}
