package eastwind.ramd.channel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import eastwind.ramd.handler.Handler;
import eastwind.ramd.model.ObjectWrapper;
import eastwind.ramd.model.TcpObject;
import eastwind.ramd.model.TcpObjectBuilder;
import eastwind.ramd.model.TcpObjectType;
import eastwind.ramd.server.RamdServer;
import io.netty.channel.Channel;

public abstract class TcpChannel extends AbstractChannel implements InetChannel<TcpObject> {

	protected RamdServer server;
	protected HandlerProvider handlerProvider;

	public void send(Object message, Consumer<TcpObject> preSend, CompletableFuture<TcpObject> flushFuture) {
		Channel channel = getNettyChannel();
		if (channel.isActive()) {
			TcpObject sent = TcpObjectBuilder.newTcpObject();
			if (message instanceof Integer) {
				sent.type = (byte) message;
			} else if (message instanceof ObjectWrapper) {
				ObjectWrapper wrapper = (ObjectWrapper) message;
				sent.type = TcpObjectType.DATA;
				sent.args = wrapper.args;
				sent.data = wrapper.data;
				sent.respondTo = wrapper.respondTo;
			} else {
				sent.type = TcpObjectType.DATA;
				sent.args = 1;
				sent.data = message;
			}
			if (preSend != null) {
				preSend.accept(sent);
			}
			if (flushFuture == null) {
				channel.writeAndFlush(sent);
			} else {
				channel.writeAndFlush(sent).addListener(cf -> {
					if (cf.isSuccess()) {
						flushFuture.complete(sent);
					} else if (cf.cause() != null) {
						flushFuture.completeExceptionally(cf.cause());
						cf.cause().printStackTrace();
					}
				});
			}
		} else {
			if (flushFuture != null) {
				flushFuture.complete(null);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void recv(TcpObject tcpObject) {
		byte type = tcpObject.type;
		if (type == 9) {
			ExchangePair exchangePair = ExchangePair.from(tcpObject);
			int args = tcpObject.args;
			if (args == 1) {
				Handler<Object> handler = (Handler<Object>) handlerProvider.getHandler(tcpObject.data.getClass());
				Object back = handle(handler, tcpObject.data, exchangePair);
				if (back != null) {
					send(ObjectWrapper.wrap(exchangePair.id, back), null, null);
				}
			} else {
				Object[] data = (Object[]) tcpObject.data;
				for (Object t : data) {
					Handler<Object> apply = (Handler<Object>) handlerProvider.getHandler(t.getClass());
					handle(apply, t, exchangePair);
				}
			}
		}
	}

	protected abstract Object handle(Handler<Object> apply, Object data, ExchangePair exchangePair);

	public RamdServer getServer() {
		return server;
	}

	public void setServer(RamdServer server) {
		this.server = server;
	}

	public void setHandlerProvider(HandlerProvider handlerProvider) {
		this.handlerProvider = handlerProvider;
	}
}
