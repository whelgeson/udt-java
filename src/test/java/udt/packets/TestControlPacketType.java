package udt.packets;


import org.junit.jupiter.api.Test;
import udt.packets.ControlPacket.ControlPacketType;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestControlPacketType {

    @Test
    public void testSequenceNumber1() {
        ControlPacket p = new DummyControlPacket();
        byte[] x = p.getHeader();
        byte highest = x[0];
        assertEquals(128, highest & 0x80);
    }

    @Test
    public void testControlPacketTypes() {
        ControlPacketType t = ControlPacketType.CONNECTION_HANDSHAKE;
        assertEquals(0, t.ordinal());
        t = ControlPacketType.KEEP_ALIVE;
        assertEquals(1, t.ordinal());
        t = ControlPacketType.ACK;
        assertEquals(2, t.ordinal());
        t = ControlPacketType.NAK;
        assertEquals(3, t.ordinal());
        t = ControlPacketType.SHUTDOWN;
        assertEquals(5, t.ordinal());
        t = ControlPacketType.ACK2;
        assertEquals(6, t.ordinal());
        t = ControlPacketType.MESSAGE_DROP_REQUEST;
        assertEquals(7, t.ordinal());
        t = ControlPacketType.USER_DEFINED;
        assertEquals(15, t.ordinal());
    }
}
