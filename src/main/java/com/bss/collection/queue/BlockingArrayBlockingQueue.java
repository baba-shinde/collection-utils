package com.bss.collection.queue;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is an extension to {@link ArrayBlockingQueue}. Specially to be used with
 * {@link ExecutorService}'s implementations.
 * 
 * 
 * @author baba
 *
 */
public class BlockingArrayBlockingQueue<T> extends ArrayBlockingQueue<T> {

	private static final long serialVersionUID = -1412590827209081602L;

	public BlockingArrayBlockingQueue(int capacity) {
		super(capacity, false);
	}

	public BlockingArrayBlockingQueue(int capacity, boolean fair) {
		super(capacity, fair);
	}

	public BlockingArrayBlockingQueue(int capacity, boolean fair, Collection<? extends T> c) {
		super(capacity, fair, c);
	}

	@Override
	public boolean offer(T e) {
		boolean ret = false;
		try {
			super.put(e);
			ret = true;
		} catch (InterruptedException ie) {
			throw new RuntimeException("Exception occurred while adding element in Blocking queue !");
		}

		return ret;
	}

	@Override
	public boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
		super.put(e);
		return true;
	}
}