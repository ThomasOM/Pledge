# Pledge
Bukkit transaction API for predicting when a server packet arrives at a client.
Mainly intended for use in Minecraft anticheats, or if you really want to track whether a player has received packets or not.
Now supporting 1.7 - 1.16.5, still working on implementing ping/pong packets for 1.17

# How does it work

Given below is a very simplified explanation how this works and why we should use it.
If you are interested in a detailed explanation, you're not gonna get it here.

As most of you who are here already know, Minecraft Java Edition uses the TCP protocol to send data between the client and the server.
This protocol guarantees us packet order, even if data is dropped or duplicated.
We can use this to our advantage by sending a transaction packet and the start and end of a tick.
If we receive a response for these two packets from the client, we would logically know that every packet inbetween these packets has also been received.

Pledge simply sends a transaction packet when the tick starts, and one when the tick ends to every player.
This way, every packet that is sent in this tick can be tracked the same way as explained above.

# How to use

Build a Pledge object using the Pledge#build method and use the builder pattern methods to configurate it.
After you're done, use the Pledge#start method to inject and start the transaction task.
Most of your questions should be answered by reading the documentation of the API.

```
public void onEnable() {
    Pledge pledge = Pledge.build().events(true).range(Short.MIN_VALUE, -1);
    pledge.start(this);
}
```

A transaction event listener is included, in case you're not already listening to transaction packets yourself.
Please make sure that you set the 'events' setting to true, because it is not used by default.
Simply implement the TransactionListener interface and create your own TransactionListener implementation.
After that, you can register it using the Pledge#addListener method.


You're free to copy, share and use this code however you want. Credits are still appreciated though.
