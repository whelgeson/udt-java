package udt.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import udt.UDTInputStream.AppData;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestReceiveBuffer {

    volatile boolean poll = false;

    @Test
    public void testInOrder() {
        ReceiveBuffer b = new ReceiveBuffer(16, 1);
        byte[] test1 = "test1".getBytes();
        byte[] test2 = "test2".getBytes();
        byte[] test3 = "test3".getBytes();

        b.offer(new AppData(1L, test1));
        b.offer(new AppData(2L, test2));
        b.offer(new AppData(3L, test3));

        AppData a = b.poll();
        assertEquals(1L, a.getSequenceNumber());

        a = b.poll();
        assertEquals(2L, a.getSequenceNumber());

        a = b.poll();
        assertEquals(3L, a.getSequenceNumber());

        assertNull(b.poll());
    }

    @Test
    public void testOutOfOrder() {
        ReceiveBuffer b = new ReceiveBuffer(16, 1);
        byte[] test1 = "test1".getBytes();
        byte[] test2 = "test2".getBytes();
        byte[] test3 = "test3".getBytes();

        b.offer(new AppData(3L, test3));
        b.offer(new AppData(2L, test2));
        b.offer(new AppData(1L, test1));

        AppData a = b.poll();
        assertEquals(1L, a.getSequenceNumber());

        a = b.poll();
        assertEquals(2L, a.getSequenceNumber());

        a = b.poll();
        assertEquals(3L, a.getSequenceNumber());

        assertNull(b.poll());
    }

    @Test
    public void testInterleaved() {
        ReceiveBuffer b = new ReceiveBuffer(16, 1);
        byte[] test1 = "test1".getBytes();
        byte[] test2 = "test2".getBytes();
        byte[] test3 = "test3".getBytes();

        b.offer(new AppData(3L, test3));

        b.offer(new AppData(1L, test1));

        AppData a = b.poll();
        assertEquals(1L, a.getSequenceNumber());

        assertNull(b.poll());

        b.offer(new AppData(2L, test2));

        a = b.poll();
        assertEquals(2L, a.getSequenceNumber());

        a = b.poll();
        assertEquals(3L, a.getSequenceNumber());
    }

    @Test
    public void testOverflow() {
        ReceiveBuffer b = new ReceiveBuffer(4, 1);

        for (int i = 0; i < 3; i++) {
            b.offer(new AppData(i + 1, "test".getBytes()));
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, b.poll().getSequenceNumber());
        }

        for (int i = 0; i < 3; i++) {
            b.offer(new AppData(i + 4, "test".getBytes()));
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 4, b.poll().getSequenceNumber());
        }
    }

    @Test
    public void testTimedPoll() throws Exception {
        final ReceiveBuffer b = new ReceiveBuffer(4, 1);

        Runnable write = () -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(500);
                    b.offer(new AppData(i + 1, "test".getBytes()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail();
            }
        };

        Callable<String> reader = () -> {
            for (int i = 0; i < 5; i++) {
                AppData r = null;
                do {
                    try {
                        r = b.poll(200, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } while (r == null);
            }
            return "OK.";
        };

        ScheduledExecutorService es = Executors.newScheduledThreadPool(2);
        es.execute(write);
        Future<String> res = es.submit(reader);
        res.get();
        es.shutdownNow();
    }

    @Test
    public void testTimedPoll2() throws Exception {
        final ReceiveBuffer b = new ReceiveBuffer(4, 1);

        Runnable write = () -> {
            try {
                Thread.sleep(2979);
                System.out.println("PUT");
                while (!poll) Thread.sleep(10);
                b.offer(new AppData(1, "test".getBytes()));
                System.out.println("... PUT OK");
            } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail();
            }
        };

        Callable<String> reader = () -> {
            AppData r = null;
            do {
                try {
                    poll = true;
                    System.out.println("POLL");
                    r = b.poll(1000, TimeUnit.MILLISECONDS);
                    poll = false;
                    if (r != null) System.out.println("... POLL OK");
                    else System.out.println("... nothing.");
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } while (r == null);
            return "OK.";
        };

        ScheduledExecutorService es = Executors.newScheduledThreadPool(2);
        es.execute(write);
        Future<String> res = es.submit(reader);
        res.get();
        es.shutdownNow();
    }
}
