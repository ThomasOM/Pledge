# Pledge
Bukkit transaction API for predicting when a server packet arrives at a client.
Mainly intended for use in Minecraft anticheats.

# How to use

Very straight forward and easy to use. Read the documentation of the API and your questions should be answered.
Just build a Pledge object using the Pledge#build method and use the builder pattern methods to configurate it.
After you're done, use the Pledge#start method to inject and start the transaction task.

```
public void onEnable() {
    Pledge pledge = Pledge.build().events(true).range(Short.MIN_VALUE, -1);
    pledge.start(this);
}
```

A transaction event listener is included, in case you're not already listening to transaction packets yourself.
See TransactionListener in the api package.
