package eastwind.ramd.ramd;

import java.util.concurrent.CompletableFuture;

import eastwind.ramd.model.RamdMessage;
import eastwind.ramd.server.RamdGroup;
import eastwind.ramd.server.RamdServer;

public class FollowerPutApplyer {

	private RamdGroup ramdGroup;
	
	public CompletableFuture<RamdMessage> apply(String name, Object key) {
		CompletableFuture<RamdMessage> cf = new CompletableFuture<RamdMessage>();
		RamdServer leader = (RamdServer) ramdGroup.findLeader();
		RamdMessage ramdMessage = RamdMessage.apply(name, key);
		leader.exchange(ramdMessage, 0);
		return cf;
	}

}
