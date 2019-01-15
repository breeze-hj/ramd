package eastwind.ramd.server;

import eastwind.ramd.support.State;

public class RamdGroupState extends State {
	
	public static final RamdGroupState INITIAL = new RamdGroupState("INITIAL", 0);
	
    public static final RamdGroupState UNSERVICEABLE = new RamdGroupState("UNSERVICEABLE", 1);
    
    public static final RamdGroupState SERVICEABLE = new RamdGroupState("SERVICEABLE", 1);
    
    public static final RamdGroupState[] ALL = {SERVICEABLE, UNSERVICEABLE};
	
	public RamdGroupState(String state, int level) {
		super(state, level);
	}

}
