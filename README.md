# Pledge
A high performance and lightweight Bukkit packet tracking API for predicting when a server packet arrives at a client using transactions.
Mainly intended for use in Minecraft anticheats, or if you really want to track whether a player has received packets or not.
Supporting 1.8 - 1.17.1

# How does it work

Given below is a very simplified explanation how Pledge works and what it should be used for.

As most of you who are here already know, Minecraft Java Edition uses the TCP protocol to send data between the client and the server.
This protocol guarantees us packet order, even if data is dropped or duplicated.
We can use this to our advantage by sending a transaction packet and the start and end of a tick.
If we receive a response for these two packets from the client, we would logically know that every packet between these packets has also been received.

Pledge simply sends a transaction packet when the tick starts and one when the tick ends to every player.
Doing this, every packet that is sent in this tick can be tracked the same way as explained above.

# How to use

Build a Pledge object using the ```Pledge#build``` method and use the builder pattern methods to configure it.
After you're done, use the ```Pledge#start``` method to inject and start the transaction task.
Most of your questions should be answered by reading the documentation in the API package.

```java
public void onEnable() {
    Pledge pledge = Pledge.build();
    pledge.start(this);
}
```

A transaction packet listener is included, in case you're not already listening to transaction packets yourself.
Please make sure that you set the 'events' setting to true, because it is not used by default.
Simply implement the ```TransactionListener``` interface and create your own implementation.
After that, you can register it using the ```Pledge#addListener``` method.
Below is an example of using Pledge with a transaction listener, where ```MyTransactionListener``` is your implementation.

```java
public void onEnable() {
    Pledge pledge = Pledge.build().events(true);
    pledge.start(this);
    
    pledge.addListener(new MyTransactionListener());
}
```

Another important detail is that the player channel is injected when joining the world, so only play packets are tracked.
Because we need the player connection object to be set, we miss the server spawn packet and a few other packets sent when the player joins the server.
This shouldn't matter for most use cases, but please be aware of this.

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
    <version>1.0-SNAPSHOT</version>
  </dependency>
</dependencies>
````

You're free to copy, share and use this code however you want. Credits are appreciated.
