package eastwind.ramd.model;

public class RamdMessage {

	public static final int APPLY = 0;
	public static final int GET = 1;
	public static final int PUT = 2;
	public static final int NONE = 3;
	public static final int REDIRECT = 8;
	public static final int OK = 9;

	public String name;
	// 0 apply, 1 get, 2 put, 9 ok
	public int type;
	public Long logId;
	public Object data;

	public static RamdMessage apply(String name, Object key) {
		RamdMessage message = new RamdMessage();
		message.name = name;
		message.type = APPLY;
		message.data = key;
		return message;
	}

	public static RamdMessage get(String name, Object key) {
		RamdMessage message = new RamdMessage();
		message.name = name;
		message.type = GET;
		message.data = key;
		return message;
	}
	
	public static RamdMessage put(String name, Object data) {
		RamdMessage message = new RamdMessage();
		message.name = name;
		message.type = PUT;
		message.data = data;
		return message;
	}

	public static RamdMessage redirect(String target) {
		RamdMessage message = new RamdMessage();
		message.type = REDIRECT;
		message.data = target;
		return message;
	}
	
	public static RamdMessage none() {
		RamdMessage message = new RamdMessage();
		message.type = NONE;
		return message;
	}
	
	public static RamdMessage ok(Long logId, Object data) {
		RamdMessage message = new RamdMessage();
		message.type = OK;
		message.data = logId;
		message.data = data;
		return message;
	}
}
