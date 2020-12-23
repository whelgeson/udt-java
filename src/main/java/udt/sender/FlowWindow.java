package udt.sender;

import udt.packets.DataPacket;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds a fixed number of {@link DataPacket} instances which are sent out.
 * <p>
 * it is assumed that a single thread (the producer) stores new data,
 * and another single thread (the consumer) reads/removes data.
 *
 * @author schuller
 */
public class FlowWindow {

    private final DataPacket[] packets;

    private final int length;
    private final ReentrantLock lock;
    private volatile boolean isEmpty = true;
    private volatile boolean isFull = false;
    //valid entries that can be read
    private volatile int validEntries = 0;
    private volatile boolean isCheckout = false;
    //index where the next data packet will be written to
    private volatile int writePos = 0;
    //one before the index where the next data packet will be read from
    private volatile int readPos = -1;
    private volatile int consumed = 0;
    private volatile int produced = 0;

    /**
     * @param size      - flow window size
     * @param chunksize - data chunk size
     */
    public FlowWindow(int size, int chunksize) {
        this.length = size + 1;
        packets = new DataPacket[length];
        for (int i = 0; i < packets.length; i++) {
            packets[i] = new DataPacket();
            packets[i].setData(new byte[chunksize]);
        }
        lock = new ReentrantLock(true);
    }

    /**
     * get a data packet for updating with new data
     *
     * @return <code>null</code> if flow window is full
     */
    public DataPacket getForProducer() {
        lock.lock();
        try {
            if (isFull) {
                return null;
            }
            if (isCheckout) throw new IllegalStateException();
            isCheckout = true;
            return packets[writePos];
        } finally {
            lock.unlock();
        }
    }

    /**
     * notify the flow window that the data packet obtained by {@link #getForProducer()}
     * has been filled with data and is ready for sending out
     */
    public void produce() {
        lock.lock();
        try {
            if (!isCheckout) throw new IllegalStateException();
            isCheckout = false;
            writePos++;
            if (writePos == length) writePos = 0;
            validEntries++;
            isFull = validEntries == length - 1;
            isEmpty = false;
            produced++;
        } finally {
            lock.unlock();
        }
    }


    public DataPacket consumeData() {
        lock.lock();
        try {
            if (isEmpty) {
                return null;
            }
            readPos++;
            DataPacket p = packets[readPos];
            if (readPos == length - 1) readPos = -1;
            validEntries--;
            isEmpty = validEntries == 0;
            isFull = false;
            consumed++;
            return p;
        } finally {
            lock.unlock();
        }
    }

    boolean isEmpty() {
        return isEmpty;
    }

    /**
     * check if another entry can be added
     *
     * @return
     */
    public boolean isFull() {
        return isFull;
    }

    int readPos() {
        return readPos;
    }

    int writePos() {
        return writePos;
    }

    int consumed() {
        return consumed;
    }

    public String toString() {
        return "FlowWindow size=" + length +
                            " full=" + isFull + " empty=" + isEmpty +
                            " readPos=" + readPos + " writePos=" + writePos +
                            " consumed=" + consumed + " produced=" + produced;
    }
}
