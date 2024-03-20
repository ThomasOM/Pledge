# Pledge
A high performance and lightweight Bukkit packet tracking API for predicting when a server packet arrives at a client,
using ping/pong and transaction packets.


# How does it work
Minecraft uses the TCP network protocol to transfer data between the server and the client.
TCP guarantees delivery of data and also guarantees that packets will be delivered in the same order in which they were sent.
The Minecraft client immediately responds to a server ping or transaction packet after synchronizing it with the game thread.
Note that this is different behaviour from keep-alive packets which are not synchronized with the game thread before being responded to.

Knowing this we can send a ping or transaction packet before and after some data we want to track.
This guarantees the client has processed the data between the responses for these packets.


# Getting Started
Pledge works by sending ping or transaction packets to track data being received by a client.
Several ways of achieving this are provided by Pledge.

You can manually send pings to and receive pings from a client, schedule pings to be sent every tick,
and even use a managed solution that uses frames to limit the amount of pings sent.

For extra information, please check out the javadoc added to the API interfaces.

# Examples
Sending pings
```java
public class ExamplePlugin extends JavaPlugin implements Listener {
    private Pledge pledge;

    @Override
    public void onEnable() {
        this.pledge = Pledge.getOrCreate(this); // Create or get when already registered to another plugin
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.pledge.sendPing(event.getPlayer(), -1); // Send ping on player join
    }

    @EventHandler
    public void onPingSend(PingSendEvent event) {
        Bukkit.broadcastMessage("Sent ping: " + event.getId());
    }

    @EventHandler
    public void onPongReceive(PongReceiveEvent event) {
        Bukkit.broadcastMessage("Received pong: " + event.getId());
    }
}
```

Client Pinger
```java
public class ExamplePlugin extends JavaPlugin implements ClientPingerListener {
    private Pledge pledge;

    @Override
    public void onEnable() {
        this.pledge = Pledge.getOrCreate(this); // Create or get when already registered to another plugin
        ClientPinger pinger = this.pledge.createPinger(-1, -200); // Ping ids range from -1 to -200
        pinger.attach(this); // Attach listener to pinger
    }

    @Override
    public void onPingSendStart(Player player, int id) {
        Bukkit.broadcastMessage("Sent first ping in tick: " + id);
    }

    @Override
    public void onPingSendEnd(Player player, int id) {
        Bukkit.broadcastMessage("Sent second ping in tick: " + id);
    }

    @Override
    public void onPongReceiveStart(Player player, int id) {
        Bukkit.broadcastMessage("Received first pong of tick: " + id);
    }

    @Override
    public void onPongReceiveEnd(Player player, int id) {
        Bukkit.broadcastMessage("Received second pong of tick: " + id);
    }
}
```


Frame Client Pinger
```java
public class ExamplePlugin extends JavaPlugin implements FrameClientPingerListener {
    private Pledge pledge;

    @Override
    public void onEnable() {
        this.pledge = Pledge.getOrCreate(this); // Create or get when already registered to another plugin
        FrameClientPinger pinger = this.pledge.createFramePinger(-1, -200); // Ping ids range from -1 to -200
        pinger.attach(this); // Attach listener to pinger
        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(pinger::getOrCreate), 20L, 20L); // Create a frame every second
    }

    @Override
    public void onFrameSend(Player player, Frame frame) {
        Bukkit.broadcastMessage("Sent frame: " + frame);
    }

    @Override
    public void onFrameReceiveStart(Player player, Frame frame) {
        Bukkit.broadcastMessage("Received first pong for frame: " + frame);
    }

    @Override
    public void onFrameReceiveEnd(Player player, Frame frame) {
        Bukkit.broadcastMessage("Received second pong for frame: " + frame);
    }
}
```


# Important notes
Pledge only tracks packets when in play state.
This is because ping or transaction packets are only available while in this state.

Most plugins, even when modifying the netty pipeline, should have no conflicts with Pledge.
Feel free to open an issue if an incompatibility is found.


# Dependency
If you want to use this in your project, you can add it as a Maven dependency:

```xml
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
    <version>3.0</version>
  </dependency>
</dependencies>
```
