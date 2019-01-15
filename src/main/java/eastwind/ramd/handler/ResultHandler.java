package eastwind.ramd.handler;

import eastwind.ramd.channel.ExchangePair;
import eastwind.ramd.channel.InboundChannel;
import eastwind.ramd.channel.OutboundChannel;
import eastwind.ramd.support.Result;

public class ResultHandler implements Handler<Result> {

	@Override
	public Object handleFromInboundChannel(InboundChannel channel, Result t, ExchangePair pair) {
		return null;
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, Result t, ExchangePair pair) {
		completeExchange(channel, t, pair.respondTo);
		return null;
	}

}
