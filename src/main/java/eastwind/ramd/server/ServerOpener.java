package eastwind.ramd.server;

import java.net.InetSocketAddress;
import java.util.List;

import eastwind.ramd.channel.ChannelFactory;
import eastwind.ramd.channel.ChannelOpener;
import eastwind.ramd.channel.OutboundChannel;
import eastwind.ramd.support.RamdUtils;

public class ServerOpener {

	private ChannelFactory channelFactory;
	private ChannelOpener channelOpener;

	public ServerOpener(ChannelFactory channelFactory, ChannelOpener channelOpener) {
		this.channelFactory = channelFactory;
		this.channelOpener = channelOpener;
	}

	public void open(RamdServer server) {
		List<OutboundChannel> outboundChannels = server.getOutboundChannels();
		if (outboundChannels.size() == 0) {
			InetSocketAddress remoteAddress = RamdUtils.parseAddress(server.getAddressStr());
			OutboundChannel channel = channelFactory.newOutboundChannel(remoteAddress);
			server.addChannel(channel);
			channel.setServer(server);
			channelOpener.open(channel);
		} else {
			for (OutboundChannel channel : outboundChannels) {
				if (channel.isClosed() && !channel.isOpening()) {
					channelOpener.open(channel);
				}
			}
		}
	}
}
