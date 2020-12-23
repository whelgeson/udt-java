package echo;

import org.junit.jupiter.api.Test;
import udt.UDTClient;
import udt.util.Util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestEchoServer {

    @Test
    public void test1() throws Exception {
        EchoServer es = new EchoServer(65322);
        es.start();
        Thread.sleep(1000);
        UDTClient client = new UDTClient(InetAddress.getByName("localhost"), 12345);
        client.connect("localhost", 65322);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
        pw.println("test");
        pw.flush();
        System.out.println("Message sent.");
        client.getInputStream().setBlocking(false);
        String line = Util.readLine(client.getInputStream());
        assertNotNull(line);
        System.out.println(line);
        assertEquals("test", line);
    }
}
