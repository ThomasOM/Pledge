package dev.thomazz.pledge.pinger;

/**
 * Utility to send pings automatically and the start and end of a tick.
 */
public interface Pinger {
	/**
	 * Start of the ID range for pings used by this instance.
	 * <p>
	 * @return - Start ID
	 */
	int startId();

	/**
	 * End of the ID range for pings used by this instance.
	 * <p>
	 * @return - End ID
	 */
	int endId();

	/**
	 * Destroys the instance.
	 */
	void destroy();
}
