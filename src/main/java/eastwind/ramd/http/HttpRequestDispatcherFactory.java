package eastwind.ramd.http;

import eastwind.ramd.server.BootstrapServer;
import eastwind.ramd.server.BootstrapServiceable;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpRequestDispatcherFactory extends BootstrapServiceable {

	public HttpRequestDispatcherFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public HttpRequestDispatcher newHttpRequestDispather() {
		HttpRequestDispatcher dispatcher = new HttpRequestDispatcher();
		dispatcher.onPath("/", this::handelIndex);
		dispatcher.onPath("/favicon.ico", this::handelFavicon);
		return dispatcher;
	}

	private Object handelFavicon(FullHttpRequest request) {
		return HttpResponseStatus.NOT_FOUND;
	}

	private Object handelIndex(FullHttpRequest request) {
		return null;
	}
}
