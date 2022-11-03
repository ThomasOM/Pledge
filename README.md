# Pledge
A high performance and lightweight Bukkit packet tracking API for predicting when a server packet arrives at a client,
using ping/pong and transaction packets.


# How does it work
Minecraft uses the TCP network protocol to transfer data between the server and the client.
TCP guarantees delivery of data and also guarantees that packets will be delivered in the same order in which they were sent.
The Minecraft client immediately responds to a server ping or transaction packet after synchronizing it with the game thread.

Knowing this we can send a ping or transaction packet before and after some data we want to track.
This guarantees the client has processed the data between the responses of these packets.


# Getting Started
Setting up Pledge is extremely simple.
You create a new API instance with ```Pledge#build``` and start it up by calling ```Pledge#start```
This is usually done when starting up a plugin that is loaded on startup.

Pledge works by sending ping or transaction packets to track data being received by a client.
Tracking individual packets can be intensive and detrimental to bandwidth usage.
Instead, Pledge offers a way to track all packets sent in a single server tick.

To track the packets for a player in the current server tick, you can use ```Pledge#getOrCreateFrame```
This creates a ```PacketFrame``` or returns the existing frame if one was already created earlier for this player.
A ```PacketFrame``` is a pair of ping or transaction packets wrapping the packets sent in the current server tick.

After a ```PacketFrame``` is created it gets sent to the client on the connection tick.
Events in the ```api.event``` package broadcast when a frame is sent to the client and when a response is received from the client.

More detailed descriptions of events and other API interfaces can be found in the documentation of the api package.


# Important notes
Any of the ping / pong or transaction packets sent and received by Pledge will be invisible to the server itself.
Internal Pledge channel handlers filter out these packets to prevent any interference with server or plugin behaviour.

Pledge also optimizes networking by queueing all the packets in the pipeline channel handler before they are sent to the client.
The queued packets are all written to the pipeline and flushed a single time on the connection tick,
minimizing the amount of flush calls and improving overall performance.

Most plugins, even when modifying the netty pipeline, should have no conflicts with Pledge.
Feel free to open an issue if an incompatibility is found. 


# Dependency
If you want to use this in your project, you can add it as a Maven dependency:

````xml
<repositories>
  <repository>
    <id>pledge-repo</id>
    <url>https://raw.github.com/ThomasOM/Pledge/repository/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>dev.thomazz</groupId>
    <artifactId>pledge</artifactId>
    <version>2.0</version>
  </dependency>
</dependencies>
````
