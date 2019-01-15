package eastwind.ramd.ramd;

import com.google.common.collect.HashBasedTable;

import eastwind.ramd.server.Server;

public class DataService {
	
	private long term;
	private long logId;
	private HashBasedTable<String, Object, Object> localValueTable = HashBasedTable.create();
	private HashBasedTable<String, Object, Server> stubTable = HashBasedTable.create();
	
	public Object get(String name, Object key) {
		return localValueTable.get(name, key);
	}
	
	public void put(String name, Object key, Object value) {
		localValueTable.put(name, key, value);
	}
	
	public Server getStub(String name, Object key) {
		return stubTable.get(name, key);
	}
	
	public boolean putStub(String name, Object key, Server server) {
		if (stubTable.contains(name, key)) {
			return false;
		}
		stubTable.put(name, key, server);
		return true;
	}
	
	public long getCurrentTerm() {
		return term;
	}
	
	public void setCurrentTerm(long term) {
		this.term = term;
	}
	
	public long incrementTerm() {
		long term = getCurrentTerm() + 1;
		setCurrentTerm(term);
		return term;
	}
	
	public long getLogId() {
		return logId;
	}
	
	public void setLogId(long logId) {
		this.logId = logId;
	}
	
	public long incrementLogId() {
		long logId = getLogId() + 1;
		setLogId(logId);
		return logId;
	}
	
}
