package eastwind.ramd.handler;

import eastwind.ramd.channel.ExchangePair;
import eastwind.ramd.channel.InboundChannel;
import eastwind.ramd.channel.OutboundChannel;
import eastwind.ramd.model.Vote;
import eastwind.ramd.server.ElectEngine;

public class VoteHandler implements Handler<Vote> {

	private ElectEngine electEngine;

	public VoteHandler(ElectEngine electEngine) {
		this.electEngine = electEngine;
	}

	@Override
	public Object handleFromInboundChannel(InboundChannel channel, Vote t, ExchangePair pair) {
		return electEngine.recvVote(channel.getServer(), t);
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, Vote t, ExchangePair pair) {
		completeExchange(channel, t, pair.respondTo);
		return null;
	}

}
