package udt;

import org.junit.jupiter.api.Test;
import udt.receiver.ReceiverLossList;
import udt.receiver.ReceiverLossListEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestReceiverLossList {

    @Test
    public void test1() {
        ReceiverLossList l = new ReceiverLossList();
        ReceiverLossListEntry e1 = new ReceiverLossListEntry(1);
        ReceiverLossListEntry e2 = new ReceiverLossListEntry(3);
        ReceiverLossListEntry e3 = new ReceiverLossListEntry(2);
        l.insert(e1);
        l.insert(e2);
        l.insert(e3);
        assertEquals(1, l.getFirstEntry().getSequenceNumber());
    }
}
