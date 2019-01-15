package eastwind.ramd.server;

public class RamServerFactory extends BootstrapServiceable {

	public RamServerFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public RamdServer newServer(String addressStr) {
		RamdGroup ramdGroup = bootstrapServer.getRamdGroup();
		RamdServer server = new RamdServer(addressStr);
		ramdGroup.stub(server);
		server.whenOnline(v -> checkElect(server));
		server.whenOffline2(v -> checkElect(server));
		return server;
	}

	private void checkElect(RamdServer newServer) {
		RamdGroup ramdGroup = bootstrapServer.getRamdGroup();
		if (ramdGroup.isAllOnOffLine() && ramdGroup.isHalfOnline()) {
			Server leader = ramdGroup.findLeader();
			if (leader == null || leader.isOffline2()) {
				bootstrapServer.getElectEngine().tryElect(newServer);
			}
		}
	}
}
