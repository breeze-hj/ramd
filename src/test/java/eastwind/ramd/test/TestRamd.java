package eastwind.ramd.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import eastwind.ramd.RamdApplication;
import eastwind.ramd.RamdClient;

public class TestRamd {

	public static void main(String[] args) throws InterruptedException {
		String[] addresses = { ":18727", ":18728", ":18729" };
		String allAddressesStr = String.join(",", addresses);

		RamdApplication[] applications = new RamdApplication[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			applications[i] = RamdApplication.create(addresses[i], allAddressesStr);
		}

		String name = "test1";
		putIfAbsent(applications[0], name);
		putIfAbsent(applications[1], name);

		TimeUnit.MILLISECONDS.sleep(20);
		RamdClient ramdClient = applications[2].newRamdClient(name);
		ramdClient.get("key1").thenAccept(v -> System.out.println(v));
	}

	private static void putIfAbsent(RamdApplication application, String name) {
		RamdClient ramdClient = application.newRamdClient(name);
		CompletableFuture<Boolean> cf = ramdClient.putIfAbsent("key1", k -> "value of " + k);
		cf.thenAccept(t -> {
			String message = String.format("application of %s put %s", application.getAddressStr(), t);
			System.out.println(message);
		});
	}

}
