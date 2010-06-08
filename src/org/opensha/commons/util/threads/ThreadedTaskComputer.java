package org.opensha.commons.util.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Class for calculating a {@link Collection} of embarassingly parallel {@link Task}
 * items in parallel on a single machine.
 * 
 * @author kevin
 *
 */
public class ThreadedTaskComputer implements Runnable {
	
	private Stack<Task> stack;
	
	public ThreadedTaskComputer(Collection<Task> tasks) {
		this(tasks, true);
	}
	
	private static Stack<Task> colToStack(Collection<Task> tasks) {
		Stack<Task> stack = new Stack<Task>();
		for (Task task : tasks)
			stack.push(task);
		return stack;
	}
	
	public ThreadedTaskComputer(Collection<Task> tasks, boolean shuffle) {
		this(colToStack(tasks), shuffle);
	}
	
	public ThreadedTaskComputer(Stack<Task> stack, boolean shuffle) {
		this.stack = stack;
		
		if (shuffle)
			Collections.shuffle(stack);
	}
	
	private synchronized Task getNextTask() {
		return stack.pop();
	}
	
	public void computeSingleThread() {
		run();
	}
	
	/**
	 * Calculates all {@link Task}s in parellel with the number of available processors.
	 * 
	 * This method will block until all threads have completed
	 * 
	 * @param numThreads
	 * @throws InterruptedException
	 */
	public void computThreaded() throws InterruptedException {
		computThreaded(Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Calculates all {@link Task}s in parellel with the given number of threads.
	 * 
	 * This method will block until all threads have completed
	 * 
	 * @param numThreads
	 * @throws InterruptedException
	 */
	public void computThreaded(int numThreads) throws InterruptedException {
		if (numThreads < 2) {
			computeSingleThread();
			return;
		}
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		// create the threads
		for (int i=0; i<numThreads; i++) {
			Thread t = new Thread(this);
			threads.add(t);
		}

		// start the threads
		for (Thread t : threads) {
			t.start();
		}
		
		// join the threads
		for (Thread t : threads) {
			t.join();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Task task = getNextTask();
				task.compute();
			} catch (EmptyStackException e) {
				break;
			}
		}
	}

}
