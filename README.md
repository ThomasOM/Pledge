# Pledge
Bukkit transaction API for predicting when a server packet arrives at a client.
Mainly intended for use in Minecraft anticheats.

# How to use

First off, if you want to use the transaction listener, please read both requirements below. This is not necessary if you are already listening to transaction packets yourself.

Make sure your Bukkit plugin loads on startup. This is very important because we want to be able to inject as quickly as possible after the server starts.
Also make sure you have late-bind disabled in your server configuration, because we can not inject if the server connection is created after plugins are.

Create a static Pledge instance in your plugin class by using the method Pledge#build
This way, the instance is instantly created once your plugin is initialized so we can inject as fast as possible.
Again, if you are not using the transaction listener, you do not have to do this.

Events can be turned on or off depending on if you need the transaction listener or if you already listen to packets yourself.
There are some other options you can change using the builder pattern, read the documentation in the API class.
```
public static Pledge PLEDGE = Pledge.build(true);
```

After creating the instance, you can start the transaction task in your Plugin#onEnable method by calling Pledge#start
```
public void onEnable() {
    PLEDGE.start(this);
}
```
