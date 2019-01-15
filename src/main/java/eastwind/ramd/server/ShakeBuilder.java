package eastwind.ramd.server;

import eastwind.ramd.model.Shake;

public class ShakeBuilder extends BootstrapServiceable {

	public ShakeBuilder(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public Shake build() {
		Shake shake = new Shake();
		shake.addressStr = bootstrapServer.getAddressStr();
		shake.allAddressesStr = bootstrapServer.getAllAddressesStr();
		shake.uuid = bootstrapServer.getUuid();
		shake.startTime = bootstrapServer.getStartTime();
		
		shake.role = bootstrapServer.getRole();
		shake.term = bootstrapServer.getCurrentTerm();
		return shake;
	}
	
}
