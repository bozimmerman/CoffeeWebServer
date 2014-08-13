/**
 * 
 */
package com.planet_ink.coffee_web.tests;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.planet_ink.coffee_common.logging.Log;
import com.planet_ink.coffee_web.util.RequestStats;
import com.planet_ink.coffee_web.util.CWThreadExecutor;
import com.planet_ink.coffee_web.util.CWConfig;

/**
 * @author Bo Zimmerman
 *
 */
public class UtilTests
{

	private final static Random random=new Random(System.currentTimeMillis());
	
	
	@Test
	public void testExecutor()
	{
		CWConfig config=new CWConfig();
		config.setLogger(Log.instance());
		CWThreadExecutor executor = new CWThreadExecutor("test", config, 1, 10, 1000, TimeUnit.SECONDS, 10, 500);
		final LinkedList<Integer> todo=new LinkedList<Integer>();
		int total=0;
		final int numObjects=500;
		for(int i=1;i<=numObjects;i++)
		{
			total+=i;
			todo.add(Integer.valueOf(i));
		}
		final List<Integer> todone=Collections.synchronizedList(new LinkedList<Integer>());
		while(todo.size()>0)
		{
			final Integer myObject = todo.removeFirst();
			executor.execute(new Runnable(){
				@Override
				public void run()
				{
					if(random.nextInt(10)>5)
						try{Thread.sleep(random.nextInt(10));}catch(Exception e){}
					todone.add(myObject);
				}
			});
		}
		while(executor.getQueue().size()>0)
			try{Thread.sleep(100);}catch(Exception e){}
		int total2=0;
		for(Integer i : todone)
			total2+=i.intValue();
		if((total != total2)||(todone.size()!=numObjects))
			fail("FAIL, "+total+"!="+total2+", returned="+todone.size());
		executor.shutdown();
		System.out.println("Executor smoke test: PASS");
	}

	@Test
	public void requestStatsTester()
	{
		CWConfig config=new CWConfig();
		config.setLogger(Log.instance());
		CWThreadExecutor executor = new CWThreadExecutor("test", config, 1, 5, 100, TimeUnit.SECONDS, 10, 500);
		final RequestStats stats = new RequestStats();
		int numRequests=500;
		for(int i=0;i<numRequests;i++)
		{
			executor.execute(new Runnable(){
				@Override
				public void run()
				{
					final long startTime=System.nanoTime();
					try
					{
						stats.startProcessing();
						try{Thread.sleep(random.nextInt(10));}catch(Exception e){}
					}
					finally
					{
						stats.endProcessing(System.nanoTime()-startTime);
					}
				}
			});
		}
		boolean success=false;
		for(int i=1;i<8000;i++)
		{
			if((stats.getAverageEllapsedNanos()>0)
			&&(stats.getNumberOfRequestsInProcess()>0))
			{
				success=true;
				break;
			}
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(!success)
		{
			fail("FAIL, nothing is in progress: "+stats.getAverageEllapsedNanos()+"/"+stats.getNumberOfRequestsInProcess());
		}
		while(executor.getQueue().size()>0)
			try{Thread.sleep(100);}catch(Exception e){}
		try{Thread.sleep(1000);}catch(Exception e){}
		if((stats.getAverageEllapsedNanos()==0)
		||(stats.getNumberOfRequests()!=numRequests))
		{
			fail("FAIL, num requests = "+stats.getNumberOfRequests());
		}
		System.out.println("Stats smoke test: PASS");
	}
}
