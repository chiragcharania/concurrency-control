package concurrency.control;

import concurrency.StorageManager;
/**
 * The {@code TimestampConcurrencyControllerTWR} class implements timestamp-based concurrency control with Thomas' write rule.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 *
 * @param <V>
 *            the type of data items
 */
public class TimestampConcurrencyControllerTWR<V> extends TimestampConcurrencyController<V> {

	/**
	 * Constructs a {@code TimestampConcurrencyControllerTWR}.
	 * 
	 * @param storageManager
	 *            a {@code StorageManager}
	 */
	public TimestampConcurrencyControllerTWR(StorageManager<V> storageManager) {
		super(storageManager);
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
	 *             if the request cannot be permitted and thus the related {@code Transaction} must be aborted
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
			return;
		}
		else
		{
			wTimeStamp = timestamp;
			dWriteTS.put(dID, wTimeStamp);
		}		super.write(tID, dID, dValue);
	}

}
