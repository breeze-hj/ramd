package eastwind.ramd.model;

import eastwind.ramd.support.LongSequence;
import eastwind.ramd.support.MillisX10Sequence;

public class TcpObjectBuilder {

	private static LongSequence SEQUENCER = new MillisX10Sequence();
	
	public static TcpObject newTcpObject() {
		TcpObject tcpObject = new TcpObject();
		tcpObject.id = SEQUENCER.get();
		return tcpObject;
	}
	
}
