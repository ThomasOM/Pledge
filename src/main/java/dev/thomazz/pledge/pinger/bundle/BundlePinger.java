package dev.thomazz.pledge.pinger.bundle;

import dev.thomazz.pledge.pinger.Pinger;

/**
 * Modern solution using bundle packets to ensure client synchronization.
 * Several events can be listened to with a {@link BundlePingerListener}, such as when pings are sent and received.
 */
public interface BundlePinger extends Pinger {
	/**
	 * Attaches a bundle listener to this {@link BundlePinger}
	 * <p>
	 * @param listener - Listener to attach
	 */
	void attach(BundlePingerListener listener);
}
