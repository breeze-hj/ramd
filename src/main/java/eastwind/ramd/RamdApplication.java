package eastwind.ramd;

import eastwind.ramd.server.BootstrapServer;
import eastwind.ramd.server.RamdClientFactory;

public class RamdApplication {

	private String addressStr;
	private RamdClientFactory ramdClientFactory;
	
	public static RamdApplication create(String addressStr, String allAddressesStr) {
		RamdApplication ramdApplication = new RamdApplication();
		ramdApplication.addressStr = addressStr;
		BootstrapServer bootstrapServer = new BootstrapServer(addressStr, allAddressesStr);
		bootstrapServer.start();
		RamdClientFactory ramdClientFactory = new RamdClientFactory(bootstrapServer);
		ramdApplication.ramdClientFactory = ramdClientFactory;
		return ramdApplication;
	}
	
	public RamdClient newRamdClient(String name) {
		return ramdClientFactory.newRamdClient(name);
	}
	
	public String getAddressStr() {
		return addressStr;
	}
}
