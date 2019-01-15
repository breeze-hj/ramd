package eastwind.ramd;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import eastwind.ramd.model.RamdMessage;
import eastwind.ramd.ramd.DataService;
import eastwind.ramd.server.ExchangeContext;
import eastwind.ramd.server.RamdGroup;
import eastwind.ramd.server.RamdGroupState;
import eastwind.ramd.server.RamdServer;
import eastwind.ramd.server.Server;

public class RamdClient {

	private static Logger LOGGER = LoggerFactory.getLogger(RamdClient.class);

	private String name;
	private RamdGroup ramdGroup;
	private DataService dataService;

	public RamdClient(String name, RamdGroup ramdGroup, DataService dataService) {
		this.name = name;
		this.ramdGroup = ramdGroup;
		this.dataService = dataService;
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Object> get(Object key) {
		Object obj = dataService.get(name, key);
		if (obj == null) {
			Server server = dataService.getStub(name, key);
			if (server == null) {
				return CompletableFuture.completedFuture(null);
			} else {
				CompletableFuture<Object> cf = new CompletableFuture<Object>();
				RamdServer ramdServer = (RamdServer) server;
				RamdMessage ramdMessage = RamdMessage.get(name, key);
				ExchangeContext ec = ramdServer.exchange(ramdMessage, 0);
				ec.onSuccess(t -> {
					LOGGER.info("get {}:{} from {}", name, key, ramdServer);
					RamdMessage back = (RamdMessage) t;
					cf.complete(back.data);
				});
				return cf;
			}
		} else {
			if (obj instanceof CompletableFuture) {
				return (CompletableFuture<Object>) obj;
			}
			return CompletableFuture.completedFuture(obj);
		}
	}

	public CompletableFuture<Boolean> putIfAbsent(Object key, Function<Object, Object> function) {
		final CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();
		if (dataService.get(name, key) != null || dataService.getStub(name, key) != null) {
			cf.complete(false);
		}
		if (ramdGroup.getState() != RamdGroupState.INITIAL) {
			putIfAbsent0(key, function, cf);
		} else {
			ramdGroup.onStateOnce(t -> putIfAbsent0(key, function, cf), RamdGroupState.ALL);
		}
		return cf;
	}

	private void putIfAbsent0(Object key, Function<Object, Object> function, final CompletableFuture<Boolean> cf) {
		if (ramdGroup.isServiceable()) {
			Server leader = ramdGroup.findLeader();
			if (leader.isMyself()) {
				executeValueAsync(key, function, cf);
			} else {
				RamdMessage ramdMessage = RamdMessage.apply(name, key);
				ExchangeContext ec = ((RamdServer) leader).exchange(ramdMessage, 0);
				ec.onSuccess(t -> {
					RamdMessage back = (RamdMessage) t;
					if (back.type == RamdMessage.OK) {
						executeValueAsync(key, function, cf);
					} else if (back.type == RamdMessage.REDIRECT) {
						RamdServer ramdServer = (RamdServer) ramdGroup.get((String) back.data);
						dataService.putStub(name, key, ramdServer);
						cf.complete(false);
					}
				});
			}
		} else {
			executeValueAsync(key, function, cf);
		}
	}

	private void executeValueAsync(Object key, Function<Object, Object> function, final CompletableFuture<Boolean> cf) {
		CompletableFuture<Object> valueFun = new CompletableFuture<Object>();
		LOGGER.info("apply {}:{}", name, key);
		dataService.put(name, key, valueFun);
		dataService.putStub(name, key, ramdGroup.getMyself());
		RamdMessage put = RamdMessage.put(name, key);
		ramdGroup.enqueue(put);
		ForkJoinPool.commonPool().execute(() -> {
			Object r = function.apply(key);
			valueFun.complete(r);
			dataService.put(name, key, r);
			cf.complete(true);
		});
	}

}
