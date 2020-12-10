# UDT Java implementation

This is a native Java implementation of the UDT protocol. The UDT protocol has been developed by Yunhong Gu and Robert
Grossmann from the University of Illinois, and provides high-speed data transfer with configurable congestion control.

## Using UDT-Java

UDT-Java can be used as a library for developing your own applications, and it can be used as-is as a commandline tool
for file transfer.

### File transfer applications

We provide "send-file" and "receive-file" scripts that work analogously to their C++ counterparts.

- To start a file "server", bin/send-file <server_port>.
- To download a file from the server, bin/receive-file <server_host> <server_port> <remote_filename> <local_filename>
  where the <server_host> can be a server name or a numerical IP address.
