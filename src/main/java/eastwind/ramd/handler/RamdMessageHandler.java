package eastwind.ramd.handler;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.ramd.channel.ExchangePair;
import eastwind.ramd.channel.InboundChannel;
import eastwind.ramd.channel.OutboundChannel;
import eastwind.ramd.model.ObjectWrapper;
import eastwind.ramd.model.RamdMessage;
import eastwind.ramd.ramd.DataService;
import eastwind.ramd.server.BootstrapServer;
import eastwind.ramd.server.RamdGroup;
import eastwind.ramd.server.RamdServer;
import eastwind.ramd.server.Role;
import eastwind.ramd.server.Server;
import io.netty.util.concurrent.CompleteFuture;

public class RamdMessageHandler implements Handler<RamdMessage> {

	private static Logger LOGGER = LoggerFactory.getLogger(RamdMessageHandler.class);

	private RamdGroup ramdGroup;
	private DataService dataService;

	public RamdMessageHandler(RamdGroup ramdGroup, DataService dataService) {
		this.ramdGroup = ramdGroup;
		this.dataService = dataService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object handleFromInboundChannel(InboundChannel channel, RamdMessage t, ExchangePair pair) {
		RamdServer ramdServer = channel.getServer();
		if (t.type == RamdMessage.APPLY) {
			BootstrapServer mySelf = ramdGroup.getMyself();
			if (mySelf.getRole() == Role.LEADER) {
				if (dataService.putStub(t.name, t.data, ramdServer)) {
					long logId = dataService.incrementLogId();
					return RamdMessage.ok(logId, null);
				} else {
					Server server = dataService.getStub(t.name, t.data);
					return RamdMessage.redirect(server.getAddressStr());
				}
			} else {
				// TODO
			}
		} else if (t.type == RamdMessage.PUT) {
			String name = t.name;
			Object key = t.data;
			LOGGER.info("{} applied {}:{}", ramdServer, name, key);
			dataService.putStub(name, key, ramdServer);
		} else if (t.type == RamdMessage.GET) {
			String name = t.name;
			Object key = t.data;
			Object obj = dataService.get(name, key);
			if (obj == null) {
				Server server = dataService.getStub(name, key);
				if (server == null) {
					return RamdMessage.ok(-1L, null);
				} else {
					return RamdMessage.redirect(server.getAddressStr());
				}
			} else {
				if (obj instanceof CompleteFuture) {
					@SuppressWarnings("rawtypes")
					CompletableFuture cf = (CompletableFuture) obj;
					cf.thenAccept(r -> {
						RamdMessage back = RamdMessage.ok(0L, r);
						channel.send(ObjectWrapper.wrap(pair.id, back), null, null);
					});
				} else {
					return RamdMessage.ok(0L, obj);
				}
			}
		}
		return null;
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, RamdMessage t, ExchangePair pair) {
		completeExchange(channel, t, pair.respondTo);
		return null;
	}

}
