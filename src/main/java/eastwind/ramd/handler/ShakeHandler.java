package eastwind.ramd.handler;

import eastwind.ramd.channel.ExchangePair;
import eastwind.ramd.channel.InboundChannel;
import eastwind.ramd.channel.OutboundChannel;
import eastwind.ramd.model.Shake;
import eastwind.ramd.server.BootstrapServer;
import eastwind.ramd.server.BootstrapServiceable;
import eastwind.ramd.server.RamdGroup;
import eastwind.ramd.server.RamdServer;

public class ShakeHandler extends BootstrapServiceable implements Handler<Shake> {

	public ShakeHandler(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	@Override
	public Object handleFromInboundChannel(InboundChannel channel, Shake t, ExchangePair pair) {
		RamdGroup ramdGroup = bootstrapServer.getRamdGroup();
		RamdServer ramdServer = (RamdServer) ramdGroup.get(t.addressStr);
		channel.setServer(ramdServer);
		bootstrapServer.getServerOpener().open(ramdServer);
		return bootstrapServer.shakeBuilder().build();
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, Shake t, ExchangePair pair) {
		RamdServer ramdServer = channel.getServer();
		ramdServer.setUuid(t.uuid);
		ramdServer.setStartTime(t.startTime);
		ramdServer.setRole(t.role);
		ramdServer.setTerm(t.term);
		channel.shaked();
		return null;
	}

}
