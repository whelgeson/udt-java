package udt.sender;

import org.junit.jupiter.api.Test;
import udt.packets.DataPacket;

import static org.junit.jupiter.api.Assertions.*;

public class TestFlowWindow {

    volatile boolean read = true;
    volatile boolean write = true;
    int N = 100000;
    private volatile boolean fail = false;

    @Test
    public void testFillWindow() {
        FlowWindow fw = new FlowWindow(3, 128);
        DataPacket p1 = fw.getForProducer();
        assertNotNull(p1);
        fw.produce();
        DataPacket p2 = fw.getForProducer();
        assertNotNull(p2);
        fw.produce();
        assertNotSame(p1, p2);
        DataPacket p3 = fw.getForProducer();
        assertNotNull(p3);
        assertNotSame(p1, p3);
        assertNotSame(p2, p3);
        fw.produce();
        assertTrue(fw.isFull());

        DataPacket no = fw.getForProducer();
        assertNull(no, "Window should be full");
        assertTrue(fw.isFull());

        DataPacket c1 = fw.consumeData();
        //must be p1
        assertSame(c1, p1);
        DataPacket c2 = fw.consumeData();
        //must be p2
        assertSame(c2, p2);
        DataPacket c3 = fw.consumeData();
        //must be p3
        assertSame(c3, p3);
        assertTrue(fw.isEmpty());
    }

    @Test
    public void testOverflow() {
        FlowWindow fw = new FlowWindow(3, 64);
        DataPacket p1 = fw.getForProducer();
        assertNotNull(p1);
        fw.produce();
        DataPacket p2 = fw.getForProducer();
        assertNotNull(p2);
        fw.produce();
        assertNotSame(p1, p2);
        DataPacket p3 = fw.getForProducer();
        assertNotNull(p3);
        assertNotSame(p1, p3);
        assertNotSame(p2, p3);
        fw.produce();
        assertTrue(fw.isFull());

        //read one
        DataPacket c1 = fw.consumeData();
        //must be p1
        assertSame(c1, p1);
        assertFalse(fw.isFull());

        //now a slot for writing should be free again
        DataPacket p4 = fw.getForProducer();
        assertNotNull(p4);
        fw.produce();
        fw.consumeData();

        DataPacket p5 = fw.getForProducer();
        assertNotNull(p5);
        fw.produce();

        //which is again p1
        assertSame(p5, p1);

    }

    @Test
    public void testConcurrentReadWrite_20() throws InterruptedException {
        final FlowWindow fw = new FlowWindow(20, 64);
        Thread reader = new Thread(() -> doRead(fw));
        reader.setName("reader");
        Thread writer = new Thread(() -> doWrite(fw));
        writer.setName("writer");

        writer.start();
        reader.start();

        int c = 0;
        while (read && write && c < 10) {
            Thread.sleep(1000);
            c++;
        }
        assertFalse(fail, "An error occured in reader or writer");

    }

    @Test
    public void testConcurrentReadWrite_2() throws InterruptedException {
        final FlowWindow fw = new FlowWindow(2, 64);
        Thread reader = new Thread(() -> doRead(fw));
        reader.setName("reader");
        Thread writer = new Thread(() -> doWrite(fw));
        writer.setName("writer");

        writer.start();
        reader.start();

        int c = 0;
        while (read && write && c < 10) {
            Thread.sleep(1000);
            c++;
        }
        assertFalse(fail, "An error occured in reader or writer");

    }

    private void doRead(final FlowWindow fw) {
        System.out.println("Starting reader...");
        try {
            for (int i = 0; i < N; i++) {
                DataPacket p;
                while ((p = fw.consumeData()) == null) {
                    Thread.sleep(1);
                }
                assertEquals(i, p.getMessageNumber());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.out.println(fw);
            fail = true;
        }
        System.out.println("Exiting reader...");
        read = false;
    }

    private void doWrite(final FlowWindow fw) {
        System.out.println("Starting writer...");
        DataPacket p;
        try {
            for (int i = 0; i < N; i++) {
                do {
                    p = fw.getForProducer();
                    if (p != null) {
                        p.setData(("test" + i).getBytes());
                        p.setMessageNumber(i);
                        fw.produce();
                    }
                } while (p == null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR****");
            fail = true;
        }
        System.out.println("Exiting writer...");
        write = false;
    }
}
