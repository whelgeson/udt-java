/** *******************************************************************************
 * Copyright (c) 2010 Forschungszentrum Juelich GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * DISCLAIMER
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************** */
package udt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.text.NumberFormat;

import udt.UDTClient;
import udt.UDTInputStream;
import udt.UDTOutputStream;
import udt.UDTReceiver;

/**
 * helper class for transmitting a single file via UDT
 *
 * main method USAGE: java -cp ... udt.util.ClientPushFile <server_ip>
 * <server_port> <filename>
 */
public class ClientPushFile extends Application {

    private final int serverPort;
    private final String serverHost;
    private final String filename;
    private final NumberFormat format;

    public ClientPushFile(String serverHost, int serverPort, String filename) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.filename = filename;
        format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(3);
    }

    public void run() {
        configure();
        verbose = true;
        try {
            UDTReceiver.connectionExpiryDisabled = true;
            InetAddress myHost = localIP != null ? InetAddress.getByName(localIP) : InetAddress.getLocalHost();
            UDTClient client = localPort != -1 ? new UDTClient(myHost, localPort) : new UDTClient(myHost);
            client.connect(serverHost, serverPort);
            UDTInputStream uIn = client.getInputStream();
            UDTOutputStream uOut = client.getOutputStream();

            System.out.println("[ClientPushFile] Pushing file " + filename + " to " + serverHost);
            PrintStream ps = new PrintStream(uOut);
            ps.println(filename);
            ps.flush();

            File file = new File(filename);
            ps.println(file.length());
            ps.flush();

            FileInputStream fis = new FileInputStream(file);
            Util.copy(fis, uOut, file.length(), false);
            fis.close();
            client.shutdown();

            System.out.println("[ClientPushFile] Finished sending data.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public static void main(String[] fullArgs) throws Exception {
        int serverPort = 65321;
        String serverHost = "localhost";
        String filename = "";

        String[] args = parseOptions(fullArgs);

        try {
            serverHost = args[0];
            serverPort = Integer.parseInt(args[1]);
            filename = args[2];
        } catch (Exception ex) {
            usage();
            System.exit(1);
        }

        ClientPushFile rf = new ClientPushFile(serverHost, serverPort, filename);
        rf.run();
    }

    public static void usage() {
        System.out.println("Usage: java -cp .. udt.util.ClientPushFile "
                + "<server_ip> <server_port> <filename> "
                + "[--verbose] [--localPort=<port>] [--localIP=<ip>]");
    }

}
