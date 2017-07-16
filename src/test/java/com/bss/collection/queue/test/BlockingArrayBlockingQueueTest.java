package com.bss.collection.queue.test;

import java.util.ArrayList; 
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bss.collection.queue.BlockingArrayBlockingQueue;

public class BlockingArrayBlockingQueueTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BlockingArrayBlockingQueueTest.class);
	private BlockingArrayBlockingQueue<Runnable> blockingArrayBlockingQueue;
	private ThreadPoolExecutor threadPoolExecutor;

	@Before
	public void setUp() {
		blockingArrayBlockingQueue = new BlockingArrayBlockingQueue<>(500);
		this.threadPoolExecutor = new ThreadPoolExecutor(10, 10, 100, TimeUnit.MILLISECONDS, blockingArrayBlockingQueue);
	}

	@Test(expected = RejectedExecutionException.class)
	public void testExecutionWithArrayBlockingQueue() {
		ArrayBlockingQueue<Runnable>  queue = new ArrayBlockingQueue<>(500);
		this.threadPoolExecutor = new ThreadPoolExecutor(10, 10, 100, TimeUnit.MILLISECONDS, queue);
		int noOfCalls = 700;
		List<Callable<Boolean>> tasks = getTasks(noOfCalls);
		try {
			this.threadPoolExecutor.invokeAll(tasks);
		} catch (InterruptedException e) {
			LOGGER.error("Error occurred while processing tasks", e);
			Assert.assertTrue("Exception not expected here", false);
		}
	}

	@Test(expected = StackOverflowError.class)
	public void testArrayBlockingQueueWithRejectedExecutionHandler() {
		ArrayBlockingQueue<Runnable>  queue = new ArrayBlockingQueue<>(500);
		RejectedExecutionHandler handler = (final Runnable r, final ThreadPoolExecutor executor) -> {
			//Why to submit again on executor as it is possibly going to fail. 
			LOGGER.warn("Inside Exception Handler !!, submitting again !!");
			//lets try
			executor.submit(r);
		};
		this.threadPoolExecutor = new ThreadPoolExecutor(10, 10, 100, TimeUnit.MILLISECONDS, queue, handler);
		
		int noOfCalls = 900;
		List<Callable<Boolean>> tasks = getTasks(noOfCalls);
		try {
			this.threadPoolExecutor.invokeAll(tasks);
		} catch (InterruptedException e) {
			LOGGER.error("Error occurred while processing tasks", e);
			Assert.assertTrue("Exception not expected here", false);
		}
	}

	@Test
	public void testArrayBlockingQueueWithRejectedExecutionHandlerRunIndependent() {
		ArrayBlockingQueue<Runnable>  queue = new ArrayBlockingQueue<>(500);
		RejectedExecutionHandler handler = (final Runnable r, final ThreadPoolExecutor executor) -> {
			//Why to submit again on executor as it is possibly going to fail. 
			LOGGER.info("Inside Exception Handler !!, submitting again !!");
			Thread t = new Thread(r);
			t.start();
		};
		this.threadPoolExecutor = new ThreadPoolExecutor(10, 10, 100, TimeUnit.MILLISECONDS, queue, handler);
		
		int noOfCalls = 700;
		List<Callable<Boolean>> tasks = getTasks(noOfCalls);
		List<Future<Boolean>> futureList = null;
		try {
			futureList = this.threadPoolExecutor.invokeAll(tasks);
		} catch (InterruptedException e) {
			LOGGER.error("Error occurred while processing tasks", e);
			Assert.assertTrue("Exception not expected here", false);
		}
		Assert.assertSame("Should be same", 0, threadPoolExecutor.getActiveCount());
		Assert.assertEquals("Should be equal", noOfCalls, futureList.size());
	}
	
	@Test
	public void testExecutionWithBlockingArrayBlockingQueue() {
		int noOfCalls = 800;
		List<Callable<Boolean>> tasks = getTasks(noOfCalls);
		List<Future<Boolean>> futureList = null;
		try {
			futureList = this.threadPoolExecutor.invokeAll(tasks);
		} catch (InterruptedException | RuntimeException e) {
			Assert.assertTrue("Exception not expected here", false);
		}

		Assert.assertSame("Should be same", 0, threadPoolExecutor.getActiveCount());
		Assert.assertEquals("Should be equal", noOfCalls, futureList.size());
	}

	private List<Callable<Boolean>> getTasks(final int noOfTasks){
		List<Callable<Boolean>> tasks = new ArrayList<>();
		for (int i=0; i<noOfTasks; i++) {
			tasks.add(()-> {
				Thread.sleep(100);
				LOGGER.info("Task is running with thread: {}", Thread.currentThread().getName());
				return true;
			});
		}

		return tasks;
	}

	@After
	public void cleanUp() {
		if (!this.threadPoolExecutor.isShutdown()) {
			threadPoolExecutor.shutdown();
		}
	}
}
