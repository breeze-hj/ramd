package eastwind.ramd.support;

public class RamdMagic {

	public static final byte[] MAGIC = new byte[6];

	static {
		MAGIC[0] = 0x00;
		System.arraycopy("ramd".getBytes(), 0, MAGIC, 1, 4);
		MAGIC[4] = (byte) 0xff;
	}

}
