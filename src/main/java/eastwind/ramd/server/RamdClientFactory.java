package eastwind.ramd.server;

import eastwind.ramd.RamdClient;
import eastwind.ramd.ramd.DataService;

public class RamdClientFactory {

	private RamdGroup ramdGroup;
	private DataService dataService;

	public RamdClientFactory(BootstrapServer bootstrapServer) {
		this.ramdGroup = bootstrapServer.getRamdGroup();
		this.dataService = bootstrapServer.getDataService();
	}

	public RamdClient newRamdClient(String name) {
		return new RamdClient(name, ramdGroup, dataService);
	}
}
