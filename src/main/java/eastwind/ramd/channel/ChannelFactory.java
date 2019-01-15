package eastwind.ramd.channel;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eastwind.ramd.handler.RamdMessageHandler;
import eastwind.ramd.handler.ResultHandler;
import eastwind.ramd.handler.ShakeHandler;
import eastwind.ramd.handler.VoteHandler;
import eastwind.ramd.http.HttpRequestDispatcher;
import eastwind.ramd.http.HttpRequestDispatcherFactory;
import eastwind.ramd.model.Shake;
import eastwind.ramd.ramd.DataService;
import eastwind.ramd.server.BootstrapServer;
import eastwind.ramd.server.BootstrapServiceable;
import eastwind.ramd.server.RamdGroup;
import io.netty.channel.Channel;

public class ChannelFactory extends BootstrapServiceable {

	private HttpRequestDispatcher httpRequestDispatcher;
	private ObjectMapper objectMapper;

	public ChannelFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
		HttpRequestDispatcherFactory factory = new HttpRequestDispatcherFactory(bootstrapServer);
		this.httpRequestDispatcher = factory.newHttpRequestDispather();

		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}

	public MasterChannel newMasterChannel(InetSocketAddress address) {
		MasterChannel channel = new MasterChannel(address, this);
		return channel;
	}

	public InboundChannel newInboundChannel(Channel nettyChannel) {
		InboundChannel channel = new InboundChannel(nettyChannel);
		channel.setHandlerProvider(defaultApplyProvider());
		channel.active();
		return channel;
	}

	public OutboundChannel newOutboundChannel(InetSocketAddress remoteAddress) {
		OutboundChannel channel = new OutboundChannel(remoteAddress);
		channel.setHandlerProvider(defaultApplyProvider());
		channel.onActive(v -> {
			Shake shake = bootstrapServer.shakeBuilder().build();
			channel.send(shake, null, null);
		});
		channel.onShaked(v -> {
			channel.getServer().online();
		});
		channel.onClosed(v -> channel.setOpening(false));
		return channel;
	}

	private HandlerProvider defaultApplyProvider() {
		HandlerProvider handlerProvider = new HandlerProvider();
		handlerProvider.register(new ShakeHandler(bootstrapServer));
		handlerProvider.register(new VoteHandler(bootstrapServer.getElectEngine()));
		handlerProvider.register(new ResultHandler());
		
		RamdGroup ramdGroup = bootstrapServer.getRamdGroup();
		DataService dataService = bootstrapServer.getDataService();
		handlerProvider.register(new RamdMessageHandler(ramdGroup, dataService));
		return handlerProvider;
	}

	public HttpChannel newHttpChannel(Channel nettyChannel) {
		return new HttpChannel(nettyChannel, httpRequestDispatcher, objectMapper);
	}

}
