package eastwind.ramd.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import eastwind.ramd.RamdApplication;
import eastwind.ramd.RamdClient;

public class TestRamd {

	public static void main(String[] args) throws InterruptedException {
		String[] addresses = { ":18727", ":18728", ":18729" };
		String allAddressesStr = String.join(",", addresses);

		// start 3 applications at ports 18727,18728,18729
		RamdApplication[] applications = new RamdApplication[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			applications[i] = RamdApplication.create(addresses[i], allAddressesStr);
		}

		String name = "test1"; // namespace
		String key = "key1";
		// only one application can put value of test1:key1
		putIfAbsent(applications[0], name, key);
		putIfAbsent(applications[1], name, key);

		TimeUnit.MILLISECONDS.sleep(1000); // after some time
		// any other application can get the target value
		RamdClient ramdClient = applications[2].newRamdClient(name);
		ramdClient.get(key).thenAccept(v -> System.out.println(v));
	}

	private static void putIfAbsent(RamdApplication application, String name, String key) {
		RamdClient ramdClient = application.newRamdClient(name);
		CompletableFuture<Boolean> cf = ramdClient.putIfAbsent(key, k -> "value of " + k);
		cf.thenAccept(t -> {
			String message = String.format("application of %s put %s", application.getAddressStr(), t);
			System.out.println(message);
		});
	}

}
