package eastwind.ramd.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eastwind.ramd.support.StateFul;

public class RamdGroup extends StateFul<RamdGroupState> {

	private BootstrapServer myself;
	private Map<String, RamdServer> address2Servers = new HashMap<>();

	public RamdGroup(BootstrapServer myself) {
		this.myself = myself;
		super.state = RamdGroupState.INITIAL;
	}

	public GroupExchangeConsumer exchange(Object message, int timeout) {
		List<ExchangeContext> exchanges = new ArrayList<>();
		for (RamdServer server : getAll()) {
			exchanges.add(server.exchange(message, timeout));
		}
		return new GroupExchangeConsumer(exchanges);
	}

	public void enqueue(Object message) {
		// TODO
		for (RamdServer server : getAll()) {
			server.send(message);
		}
	}
	
	public Server findLeader() {
		if (myself.getRole() == Role.LEADER) {
			return myself;
		}
		for (RamdServer server : address2Servers.values()) {
			if (server.getUuid() != null) {
				if (server.getRole() == Role.LEADER) {
					return server;
				}
			}
		}
		return null;
	}

	public boolean isEqAll(int n) {
		return n == getSize();
	}

	public boolean isGtThenHalf(int n) {
		return n > getSize() - n;
	}

	public boolean isHalfOnline() {
		int all = getSize();
		int onlines = 1;
		for (RamdServer server : address2Servers.values()) {
			if (server.isOnline()) {
				onlines++;
			}
		}
		return onlines > all - onlines;
	}

	public boolean isAllOnOffLine() {
		for (RamdServer server : address2Servers.values()) {
			if (server.isOnline() || server.isOffline2()) {
				continue;
			}
			return false;
		}
		return true;
	}

	public Server get(String addressStr) {
		if (myself.getAddressStr().equals(addressStr)) {
			return myself;
		}
		return address2Servers.get(addressStr);
	}

	public void stub(RamdServer server) {
		address2Servers.put(server.getAddressStr(), server);
		server.whenOffline2(v -> {
			if (!isHalfOnline()) {
				changeState(RamdGroupState.UNSERVICEABLE, null);
			}
		});
	}

	public int getSize() {
		return address2Servers.size() + 1;
	}

	public BootstrapServer getMyself() {
		return myself;
	}

	public void service() {
		changeState(RamdGroupState.SERVICEABLE, null);
	}
	
	public boolean isServiceable() {
		return getState() == RamdGroupState.SERVICEABLE;
	}
	
	public Iterable<RamdServer> getAll() {
		return address2Servers.values();
	}
}
