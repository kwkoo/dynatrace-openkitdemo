package demo;

import java.util.Random;

public class SimpleRecorder implements Recorder
{
	public final static int MAX_WAIT_SECS = 2;
	private Random rand;
	private int count;

	public void init()
	{
		rand = new Random();
		count = 0;
	}

	public void record(float value)
	{
		try { Thread.sleep(getSleepInterval()); } catch (InterruptedException e) {}
		count++;
	}

	public int getReadingCount()
	{
		return count;
	}

	private long getSleepInterval()
	{
		return (long)rand.nextInt(MAX_WAIT_SECS * 1000);
	}
}
