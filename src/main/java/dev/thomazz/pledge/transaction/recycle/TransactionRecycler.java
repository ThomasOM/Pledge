package dev.thomazz.pledge.transaction.recycle;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.util.PacketUtil;
import io.netty.util.Recycler;

public class TransactionRecycler extends Recycler<RecyclableTransaction> {
	@Override
	protected RecyclableTransaction newObject(Handle<RecyclableTransaction> handle) {
		try {
			Object rawPacket;

			switch (PacketUtil.MODE) {
				default:
				case TRANSACTION:
					rawPacket = PacketUtil.OUT_TRANSACTION_CONSTRUCTOR.invoke();
					break;
				case PING_PONG:
					rawPacket = PacketUtil.PING_CONSTRUCTOR.invoke(0);
					break;
			}

			return new RecyclableTransaction(this, rawPacket, handle);
		} catch (Throwable throwable) {
			PledgeImpl.LOGGER.severe("Could not create new recyclable transaction!");
			throwable.printStackTrace();
			return null;
		}
	}
}
