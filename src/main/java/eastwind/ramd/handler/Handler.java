package eastwind.ramd.handler;

import eastwind.ramd.channel.ExchangePair;
import eastwind.ramd.channel.InboundChannel;
import eastwind.ramd.channel.OutboundChannel;
import eastwind.ramd.server.ExchangeContext;
import eastwind.ramd.server.RamdServer;

public interface Handler<T> {

	Object handleFromInboundChannel(InboundChannel channel, T t, ExchangePair pair);

	Object handleFromOutboundChannel(OutboundChannel channel, T t, ExchangePair pair);

	default void completeExchange(OutboundChannel channel, T t, Long respondId) {
		RamdServer server = channel.getServer();
		ExchangeContext context = server.removeExchange(respondId);
		context.success(t);
	}

}
