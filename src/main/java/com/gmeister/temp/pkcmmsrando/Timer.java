package com.gmeister.temp.pkcmmsrando;

import java.time.Duration;

public class Timer
{
	
	//start
	//splitandprint
	
	private long startTime;
	private long lastSplit;
	private long currentSplit;
	
	public Timer()
	{}
	
	public Timer start()
	{
		this.startTime = System.nanoTime();
		this.lastSplit = this.startTime;
		this.currentSplit = this.startTime;
		return this;
	}
	
	public Timer split()
	{
		long time = System.nanoTime();
		this.lastSplit = this.currentSplit;
		this.currentSplit = time;
		return this;
	}
	
	public Timer print()
	{
		long time = System.nanoTime();
		System.out.println(Duration.ofNanos(time - this.startTime).toString().substring(2));
		return this;
	}
	
	public Timer print(String label)
	{
		long time = System.nanoTime();
		System.out.println(label + ": " + Duration.ofNanos(time - this.startTime).toString().substring(2));
		return this;
	}
	
	public Timer printSplit()
	{
		System.out.println(Duration.ofNanos(this.currentSplit - this.lastSplit).toString().substring(2));
		return this;
	}
	
	public Timer printSplit(String label)
	{
		System.out.println(label + ": " + Duration.ofNanos(this.currentSplit - this.lastSplit).toString().substring(2));
		return this;
	}
	
}
