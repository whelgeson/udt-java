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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import udt.UDTInputStream;
import udt.UDTOutputStream;
import udt.UDTReceiver;
import udt.UDTServerSocket;
import udt.UDTSocket;
import static udt.util.Application.verbose;

/**
 * helper application for receiving files via UDT
 *
 * main method USAGE: java -cp .. udt.util.ServerReceiveFiles <server_port>
 */
public class ServerReceiveFiles extends Application {

    private final int serverPort;

    //TODO configure pool size
    private final ExecutorService threadPool = Executors.newFixedThreadPool(3);

    public ServerReceiveFiles(int serverPort) {
        this.serverPort = serverPort;

    }

    @Override
    public void configure() {
        super.configure();
    }

    public void run() {
        configure();
        try {
            UDTReceiver.connectionExpiryDisabled = true;
            InetAddress myHost = localIP != null ? InetAddress.getByName(localIP) : InetAddress.getLocalHost();
            UDTServerSocket server = new UDTServerSocket(myHost, serverPort);
            while (true) {
                UDTSocket socket = server.accept();
                Thread.sleep(1000);
                threadPool.execute(new RequestRunner(socket));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * main() method for invoking as a commandline application
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] fullArgs) throws Exception {

        String[] args = parseOptions(fullArgs);

        int serverPort = 65321;
        try {
            serverPort = Integer.parseInt(args[0]);
        } catch (Exception ex) {
            usage();
            System.exit(1);
        }
        ServerReceiveFiles sf = new ServerReceiveFiles(serverPort);
        sf.run();
    }

    public static void usage() {
        System.out.println("Usage: java -cp ... udt.util.ServerReceiveFiles <server_port> "
                + "[--verbose] [--localPort=<port>] [--localIP=<ip>]");
    }

    public static class RequestRunner implements Runnable {

        private final static Logger logger = Logger.getLogger(RequestRunner.class.getName());

        private final UDTSocket socket;

        private final NumberFormat format = NumberFormat.getNumberInstance();

        public RequestRunner(UDTSocket socket) {
            this.socket = socket;
            format.setMaximumFractionDigits(3);
        }

        public void run() {
            try {
                logger.info("Handling request from " + socket.getSession().getDestination());
                UDTInputStream uIn = socket.getInputStream();
                UDTOutputStream uOut = socket.getOutputStream();

                String filename = Util.readLine(uIn).trim();
                System.out.println("filename: " + filename);

                String filelength = Util.readLine(uIn).trim();
                System.out.println("filelength: " + filelength);

                Long length = Long.parseLong(filelength);

                uIn.read();

                FileOutputStream fos = new FileOutputStream(filename);
                OutputStream os = new BufferedOutputStream(fos, 1024 * 1024);
                try {
                    Util.copy(uIn, os, length, false);
                } finally {
                    fos.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            socket.getSender().stop();
        }
    }

}
