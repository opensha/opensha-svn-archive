package org.opensha.sha.calc.hazardMap.mpj;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;

import mpi.MPI;
import mpi.MPIException;

import com.google.common.base.Preconditions;

public class DispatcherThread extends Thread {
	
	private static final boolean D = true;
	private static final String D_PREFIX = "DispatcherThread: ";
	
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
		
		if (D) System.out.println(D_PREFIX+"starting with "+size+" processes and "+numSites+" sites." +
				" minPerDispatch="+minPerDispatch+", maxPerDispatch="+maxPerDispatch);
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i=0; i<numSites; i++)
			list.add(i);
		
		if (D) System.out.println(D_PREFIX+"shuffling stack");
		Collections.shuffle(list);
		stack = new ArrayDeque<Integer>(list);
	}
	
	protected synchronized int[] getNextBatch() {
		int numLeft = stack.size();
		if (D) System.out.println(D_PREFIX+"getting batch with "+numLeft+" left");
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
		
		if (D) System.out.println(D_PREFIX+"returning batch of size: "+numToDispatch);
		
		return batch;
	}
	
	@Override
	public void run() {
		if (D) System.out.println(D_PREFIX+"now running.");
		try {
			// this keeps track of if each process has finished once all batches have been sent out;
			boolean[] dones = null;
			
			int[] single_int_buf = new int[1];
			while (true) {
				if (D) System.out.println(D_PREFIX+"waiting for READY message.");
				// this receives a READY_FOR_BATCH message from any process. the process # is sent
				MPI.COMM_WORLD.Recv(single_int_buf, 0, 1, MPI.INT, MPI.ANY_SOURCE, MPJHazardCurveDriver.TAG_READY_FOR_BATCH);
				int proc_id = single_int_buf[0];
				
				if (D) System.out.println(D_PREFIX+"received READY from "+proc_id);
				
				int[] batch = getNextBatch();
				
				// now we send the the length of the batch
				single_int_buf[0] = batch.length;
				MPI.COMM_WORLD.Send(single_int_buf, 0, 1, MPI.INT, proc_id, MPJHazardCurveDriver.TAG_NEW_BATCH_LENGH);
				
				if (batch.length > 0)
					// now we send the batch to the process.
					MPI.COMM_WORLD.Send(batch, 0, batch.length, MPI.INT, proc_id, MPJHazardCurveDriver.TAG_NEW_BATCH);
				else if (dones == null)
					// if we're done and we haven't initialized the dones array, do it now
					dones = new boolean[size];
				
				if (D) System.out.println(D_PREFIX+"sending NEW BATCH of size "+batch.length+" to "+proc_id);
				
				
				
				if (dones != null) {
					// set the index for the process we just communicated with to "done"
					Preconditions.checkState(!dones[proc_id],
							"proc id "+proc_id+" has already been marked done!");
					dones[proc_id] = true;
					
					// this means that we're done dispatching batches, and are waiting for everyone to report back
					if (D) System.out.println(D_PREFIX+"checking if we're all done...");
					boolean allDone = true;
					for (boolean done : dones) {
						if (!done) {
							allDone = false;
							break;
						}
					}
					if (allDone) {
						// this means that all curves have been calculated
						if (D) System.out.println(D_PREFIX+"DONE!");
						break;
					}
					if (D) System.out.println(D_PREFIX+"not yet.");
				}
			}
		} catch (Throwable t) {
			MPJHazardCurveDriver.abortAndExit(t);
		}
	}

}
