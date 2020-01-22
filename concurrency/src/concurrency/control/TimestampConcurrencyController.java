package concurrency.control;

import java.util.HashMap;

import concurrency.StorageManager;

/**
 * The {@code TimestampConcurrencyController} class implements timestamp-based
 * concurrency control.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 *
 * @param <V>
 *            the type of data items
 */
public class TimestampConcurrencyController<V> extends ConcurrencyController<V> {

	/**
	 * A map that associates {@code Transaction} IDs with timestamps.
	 */
	HashMap<Integer, Integer> tID2timestamp = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> dReadTS = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> dWriteTS = new HashMap<Integer, Integer>();

	/**
	 * The number of {@code Transaction}s that have been registered so far.
	 */
	int count = 0;

	/**
	 * Constructs a {@code TimestampConcurrencyController}.
	 * 
	 * @param storageManager
	 *            a {@code StorageManager}
	 */
	public TimestampConcurrencyController(StorageManager<V> storageManager) {
		super(storageManager);
	}

	/**
	 * Registers a {@code Transaction}.
	 * 
	 * @param tID
	 *            the ID of the {@code Transaction}
	 */
	public void register(int tID) {
		tID2timestamp.put(tID, count++);
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
		Integer timestamp = tID2timestamp.get(tID); // the timestamp of the transaction specified by tID
		Integer wTimeStamp = dWriteTS.get(dID);
		Integer rTimeStamp = dReadTS.get(dID);
		if (wTimeStamp == null) {
			wTimeStamp = 0;
		}
		if (rTimeStamp == null) {
			rTimeStamp = 0;
		}
		if (timestamp >= wTimeStamp) {
			rTimeStamp = Math.max(timestamp, rTimeStamp);
			dReadTS.put(dID, rTimeStamp);
			return super.read(tID, dID);
		} else {
			throw new AbortException();
		}
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
		Integer timestamp = tID2timestamp.get(tID); // the timestamp of the transaction specified by tID
		Integer wTimeStamp = dWriteTS.get(dID);
		Integer rTimeStamp = dReadTS.get(dID);
		if (wTimeStamp == null) {
			wTimeStamp = 0;
		}
		if (rTimeStamp == null) {
			rTimeStamp = 0;
		}
		if (timestamp < rTimeStamp) {
			throw new AbortException();
		} else if (timestamp < wTimeStamp) {
			throw new AbortException();
		}
		else
		{
			wTimeStamp = timestamp;
			dWriteTS.put(dID, wTimeStamp);
		}
		super.write(tID, dID, dValue);
	}

}
