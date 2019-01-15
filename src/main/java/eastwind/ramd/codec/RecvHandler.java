package eastwind.ramd.codec;

import io.netty.channel.ChannelHandler.Sharable;
import eastwind.ramd.channel.InetChannel;
import eastwind.ramd.channel.NettyChannelBinder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class RecvHandler extends SimpleChannelInboundHandler<Object> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		InetChannel channel = NettyChannelBinder.getBinder(ctx.channel());
		channel.recv(msg);
	}

}
