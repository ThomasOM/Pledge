package dev.thomazz.pledge.transaction.recycle;

import dev.thomazz.pledge.util.PacketUtil;
import io.netty.util.Recycler;

public class RecyclableTransaction {
	private final TransactionRecycler recycler;
	private final Object packet;
	private final Recycler.Handle<RecyclableTransaction> handle;

	RecyclableTransaction(TransactionRecycler recycler, Object packet, Recycler.Handle<RecyclableTransaction> handle) {
		this.recycler = recycler;
		this.packet = packet;
		this.handle = handle;
	}

	public void setupIndex(int index) throws Throwable {
		switch (PacketUtil.MODE) {
			default:
			case TRANSACTION:
				PacketUtil.OUT_WINDOW_FIELD_SET.invoke(this.packet, PacketUtil.TRANSACTION_WINDOW_ID);
				PacketUtil.OUT_ACCEPT_FIELD_SET.invoke(this.packet, PacketUtil.TRANSACTION_ACCEPT_STATE);
				PacketUtil.OUT_ACTION_FIELD_SET.invoke(this.packet, (short) index);
				break;
			case PING_PONG:
				PacketUtil.PING_ID_FIELD_SET.invoke(this.packet, index);
				break;
		}
	}

	public Object getRawPacket() {
		return this.packet;
	}

	public void recycle() {
		// Deprecated usage due to older netty versions
		this.recycler.recycle(this, this.handle);
	}
}
