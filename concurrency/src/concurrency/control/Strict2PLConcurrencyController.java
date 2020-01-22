package concurrency.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import concurrency.StorageManager;

/**
 * The {@code Strict2PLConcurrencyController} class implements the strict 2
 * phase-locking protocol.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 *
 * @param <V>
 *            the type of data items
 */
public class Strict2PLConcurrencyController<V> extends ConcurrencyController<V> {

	/**
	 * A map that associates data IDs with locks.
	 */
	HashMap<Integer, ReentrantReadWriteLock> dID2lock = new HashMap<Integer, ReentrantReadWriteLock>();
	HashMap<Integer, ArrayList<Lock>> tID2lock = new HashMap<Integer, ArrayList<Lock>>();

	/**
	 * Constructs a {@code Strict2PLConcurrencyController}.
	 * 
	 * @param storageManager
	 *            a {@code StorageManager}
	 */
	public Strict2PLConcurrencyController(StorageManager<V> storageManager) {
		super(storageManager);
	}

	/**
	 * Handles a read request.
	 * 
	 * @param tID
	 *            the ID of the {@code Transaction} that has made the request
	 * @param dID
	 *            the ID of the data item for which the request was made
	 * @throws InvalidTransactionIDException
	 *             if an invalid {@code Transaction} ID is given
	 * @throws AbortException
	 *             if the request cannot be permitted and thus the related
	 *             {@code Transaction} must be aborted
	 */
	@Override
	public V read(int tID, int dID) throws InvalidTransactionIDException, AbortException {
		Lock lock = getLock(dID).readLock();
		lock.lock(); // wait until the read lock is acquired remember the lock so that it can be
						// released/unlocked when releaseAllRemainingLocks(int tID) is called
		ArrayList<Lock> lockList;
		lockList = tID2lock.get(tID);
		if (lockList == null) {
			lockList = new ArrayList<Lock>();
			lockList.add(lock);
			tID2lock.put(tID, lockList);
		} else {
			lockList.add(lock);
			tID2lock.put(tID, lockList);
		}
		return super.read(tID, dID);
	}

	/**
	 * Handles a write request.
	 * 
	 * @param tID
	 *            the ID of the {@code Transaction} that has made the request
	 * @param dID
	 *            the ID of the data item for which the request was made
	 * @param dValue
	 *            the value of the data item for which the request was made
	 * @throws InvalidTransactionIDException
	 *             if an invalid {@code Transaction} ID is given
	 * @throws AbortException
	 *             if the request cannot be permitted and thus the related
	 *             {@code Transaction} must be aborted
	 */
	@Override
	public void write(int tID, int dID, V dValue) throws InvalidTransactionIDException, AbortException {
		Lock lock = getLock(dID).writeLock();
		lock.lock(); // wait until the write lock is acquired remember the lock so that it can be released/unlocked when releaseAllRemainingLocks(int tID) is called
		ArrayList<Lock> lockList;
		lockList = tID2lock.get(tID);
		if (lockList == null) {
			lockList = new ArrayList<Lock>();
			lockList.add(lock);
			tID2lock.put(tID, lockList);
		} else {
			lockList.add(lock);
			tID2lock.put(tID, lockList);
		}
		// remember the lock so that it can be released/unlocked when
		// releaseAllRemainingLocks(int tID) is called
		super.write(tID, dID, dValue);
	}

	/**
	 * Rolls back the specified {@code Transaction}.
	 * 
	 * @param tID
	 *            the ID of the {@code Transaction} to roll back.
	 */
	@Override
	public void rollback(int tID) {
		super.rollback(tID);
		releaseAllRemainingLocks(tID);
	}

	/**
	 * Commits the specified {@code Transaction}.
	 * 
	 * @param tID
	 *            the ID of the {@code Transaction} to commit
	 */
	@Override
	public void commit(int tID) {
		super.commit(tID);
		releaseAllRemainingLocks(tID);
	}

	/**
	 * Releases all remaining {@code Lock}s granted to the specified
	 * {@code Transaction}.
	 * 
	 * @param tID
	 *            the ID of the {@code Transaction}
	 */
	protected void releaseAllRemainingLocks(int tID) {
		ArrayList<Lock> lockList = tID2lock.get(tID);
		int i = 0;
		do {
			Lock lock = lockList.get(i);
			lock.unlock();
			lockList.remove(i);
			i--;
		} while (lockList.size() != 0);
		// use a member variable you added
	}

	/**
	 * Returns the {@code Lock} associated with the specified data item.
	 * 
	 * @param dID
	 *            the ID of the data item
	 * @return the {@code Lock} associated with the specified data item
	 */
	protected ReentrantReadWriteLock getLock(int dID) {
		if (dID2lock.get(dID) == null) {
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
			dID2lock.put(dID, lock);
			return lock;
		}
		return dID2lock.get(dID);
	}
}