package eastwind.ramd.channel;

import eastwind.ramd.handler.Handler;
import io.netty.channel.Channel;

public class InboundChannel extends TcpChannel {

	public InboundChannel(Channel channel) {
		super.channel = channel;
	}

	@Override
	protected Object handle(Handler<Object> apply, Object data, ExchangePair exchangePair) {
		return apply.handleFromInboundChannel(this, data, exchangePair);
	}

}
