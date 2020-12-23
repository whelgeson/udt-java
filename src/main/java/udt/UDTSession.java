/*********************************************************************************
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
 *********************************************************************************/

package udt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import udt.packets.Destination;
import udt.util.UDTStatistics;

import java.net.DatagramPacket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public abstract class UDTSession {

    //state constants
    public static final int start = 0;
    public static final int handshaking = 1;
    public static final int ready = 2;
    public static final int keepalive = 3;
    public static final int shutdown = 4;
    public static final int invalid = 99;
    public static final int DEFAULT_DATAGRAM_SIZE = UDPEndPoint.DATAGRAM_SIZE;
    /**
     * key for a system property defining the CC class to be used
     *
     * @see CongestionControl
     */
    public static final String CC_CLASS = "udt.congestioncontrol.class";
    private static final Logger log = LogManager.getLogger();
    private final static AtomicLong nextSocketID = new AtomicLong(20 + new Random().nextInt(5000));
    protected final UDTStatistics statistics;
    protected final CongestionControl cc;
    /**
     * remote UDT entity (address and socket ID)
     */
    protected final Destination destination;
    protected final long mySocketID;
    //cache dgPacket (peer stays the same always)
    private final DatagramPacket dgPacket;
    protected int mode;
    protected volatile boolean active;
    protected volatile UDTPacket lastPacket;
    protected volatile UDTSocket socket;
    protected int receiveBufferSize = 64 * 32768;
    /**
     * flow window size, i.e. how many data packets are
     * in-flight at a single time
     */
    protected int flowWindowSize = 1024 * 10;
    /**
     * local port
     */
    protected int localPort;
    /**
     * Buffer size (i.e. datagram size)
     * This is negotiated during connection setup
     */
    protected int datagramSize = DEFAULT_DATAGRAM_SIZE;
    protected Long initialSequenceNumber = null;
    private volatile int state = start;

    public UDTSession(String description, Destination destination) {
        statistics = new UDTStatistics(description);
        mySocketID = nextSocketID.incrementAndGet();
        this.destination = destination;
        this.dgPacket = new DatagramPacket(new byte[0], 0, destination.getAddress(), destination.getPort());
        String clazzP = System.getProperty(CC_CLASS, UDTCongestionControl.class.getName());
        Object ccObject;
        try {
            Class<?> clazz = Class.forName(clazzP);
            ccObject = clazz.getDeclaredConstructor(UDTSession.class).newInstance(this);
        } catch (Exception e) {
            log.warn("Can't setup congestion control class <" + clazzP + ">, using default.", e);
            ccObject = new UDTCongestionControl(this);
        }
        cc = (CongestionControl) ccObject;
        log.info("Using " + cc.getClass().getName());
    }

    public abstract void received(UDTPacket packet, Destination peer);

    public UDTSocket getSocket() {
        return socket;
    }

    public void setSocket(UDTSocket socket) {
        this.socket = socket;
    }

    public CongestionControl getCongestionControl() {
        return cc;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        log.info(toString() + " connection state CHANGED to <" + state + ">");
        this.state = state;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isReady() {
        return state == ready;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isShutdown() {
        return state == shutdown || state == invalid;
    }

    public Destination getDestination() {
        return destination;
    }

    public int getDatagramSize() {
        return datagramSize;
    }

    public void setDatagramSize(int datagramSize) {
        this.datagramSize = datagramSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int bufferSize) {
        this.receiveBufferSize = bufferSize;
    }

    public int getFlowWindowSize() {
        return flowWindowSize;
    }

    public void setFlowWindowSize(int flowWindowSize) {
        this.flowWindowSize = flowWindowSize;
    }

    public UDTStatistics getStatistics() {
        return statistics;
    }

    public long getSocketID() {
        return mySocketID;
    }


    public synchronized long getInitialSequenceNumber() {
        if (initialSequenceNumber == null) {
            initialSequenceNumber = 1L; //TODO must be random?
        }
        return initialSequenceNumber;
    }

    public synchronized void setInitialSequenceNumber(long initialSequenceNumber) {
        this.initialSequenceNumber = initialSequenceNumber;
    }

    public DatagramPacket getDatagram() {
        return dgPacket;
    }

    public String toString() {
        return super.toString() +
                       " [" +
                       "socketID=" + this.mySocketID +
                       " ]";
    }

}
