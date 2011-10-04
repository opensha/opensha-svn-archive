package org.opensha.sha.calc.hazardMap.mpj;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;

import mpi.MPI;
import mpi.MPIException;

import com.google.common.base.Preconditions;

public class DispatcherThread extends Thread {
	
	private static final boolean D = true;
	
	private int size;
	private int maxPerDispatch;
	private int minPerDispatch;
	
	private Deque<Integer> stack;
	
	public DispatcherThread(int size, int numSites, int minPerDispatch, int maxPerDispatch) {
		this.size = size;
		this.minPerDispatch = minPerDispatch;
		this.maxPerDispatch = maxPerDispatch;
		Preconditions.checkArgument(minPerDispatch <= maxPerDispatch, "min per dispatch must be <= max");
		Preconditions.checkArgument(minPerDispatch >= 1, "min per dispatch must be >= 1");
		Preconditions.checkArgument(size >= 1, "size must be >= 1");
		Preconditions.checkArgument(numSites >= 1, "num sites must be >= 1");
		
		debug("starting with "+size+" processes and "+numSites+" sites." +
				" minPerDispatch="+minPerDispatch+", maxPerDispatch="+maxPerDispatch);
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i=0; i<numSites; i++)
			list.add(i);
		
		debug("shuffling stack");
		Collections.shuffle(list);
		stack = new ArrayDeque<Integer>(list);
	}
	
	protected synchronized int[] getNextBatch() {
		int numLeft = stack.size();
		debug("getting batch with "+numLeft+" left");
		if (numLeft == 0)
			return new int[0];
		double numLeftPer = (double)numLeft / (double)size;
		
		int numToDispatch = (int)Math.ceil(numLeftPer);
		if (numToDispatch > maxPerDispatch)
			numToDispatch = maxPerDispatch;
		if (numToDispatch < minPerDispatch)
			numToDispatch = minPerDispatch;
		
		if (numToDispatch > numLeft)
			numToDispatch = numLeft;
		
		int[] batch = new int[numToDispatch];
		for (int i=0; i<numToDispatch; i++)
			batch[i] = stack.pop();
		
		debug("returning batch of size: "+numToDispatch);
		
		return batch;
	}
	
	@Override
	public void run() {
		debug("now running.");
		try {
			// this keeps track of if each process has finished once all batches have been sent out;
			boolean[] dones = null;
			
			int[] single_int_buf = new int[1];
			while (true) {
				debug("waiting for READY message.");
				// this receives a READY_FOR_BATCH message from any process. the process # is sent
				MPI.COMM_WORLD.Recv(single_int_buf, 0, 1, MPI.INT, MPI.ANY_SOURCE, MPJHazardCurveDriver.TAG_READY_FOR_BATCH);
				int proc_id = single_int_buf[0];
				
				debug("received READY from "+proc_id);
				
				int[] batch = getNextBatch();
				
				// now we send the the length of the batch
				single_int_buf[0] = batch.length;
				debug("sending batch length ("+batch.length+") to: "+proc_id);
				MPI.COMM_WORLD.Send(single_int_buf, 0, 1, MPI.INT, proc_id, MPJHazardCurveDriver.TAG_NEW_BATCH_LENGH);
				
				if (batch.length > 0) {
					// now we send the batch to the process.
					debug("sending batch of length "+batch.length+" to: "+proc_id);
					MPI.COMM_WORLD.Send(batch, 0, batch.length, MPI.INT, proc_id, MPJHazardCurveDriver.TAG_NEW_BATCH);
				} else if (dones == null) {
					// if we're done and we haven't initialized the dones array, do it now
					debug("initializing dones array "+proc_id);
					dones = new boolean[size];
					dones[0] = true; // hard code this node to done, since it operates outside of MPJ
				}
				
				if (dones != null) {
					// set the index for the process we just communicated with to "done"
					Preconditions.checkState(!dones[proc_id],
							"proc id "+proc_id+" has already been marked done!");
					dones[proc_id] = true;
					
					// this means that we're done dispatching batches, and are waiting for everyone to report back
					debug("checking if we're all done...");
					boolean allDone = true;
					for (boolean done : dones) {
						if (!done) {
							allDone = false;
							break;
						}
					}
					if (allDone) {
						// this means that all curves have been calculated
						debug("DONE!");
						break;
					}
					debug("not yet.");
				}
			}
		} catch (Throwable t) {
			MPJHazardCurveDriver.abortAndExit(t);
		}
	}
	
	private static void debug(String message) {
		if (!D)
			return;
		
		System.out.println("["+MPJHazardCurveDriver.df.format(new Date())+" DispatcherThread]: "+message);
	}

}
