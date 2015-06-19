package nl.tjonahen.iptableslogd.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * LogEntry statistics, counts the number of ports, hosts and protocol.
 * 
 * This is shared between the IPTablesLogHandler thread via the LogEntryCollector and the HttpRequestHandler thread.
 *  
 * @author Philippe Tjon-A-Hen
 *
 */
public final class LogEntryStatistics {

	private long start = 0;
	private long end = 0;
	private long number = 0;
	
	public final class Counter {
		
		private int count;
		private long lastseen;
		private final String data;
		
		public Counter(String data) {
			this.count = 1;
			this.lastseen = System.currentTimeMillis();
			this.data = data;
		}
		
		public synchronized void increment() {
			count++;
			lastseen = System.currentTimeMillis();
		}
		public synchronized int getCount() {
			return count;
		}
		public synchronized long getLastseen() {
			return lastseen;
		}
		public String getData() {
			return data;
		}
		
		
	}
	private Map<String, Counter> hosts = new TreeMap<String, Counter>();
	private Map<String, Counter> protocol = new TreeMap<String, Counter>();
	private Map<String, Counter> ports = new TreeMap<String, Counter>();

	private List<Counter> getMapAsSortedList(Map<String, Counter> map) {
		List<Counter> lst = new ArrayList<Counter>();
		lst.addAll(map.values());
		Collections.sort(lst, new CounterComparator());
		return Collections.synchronizedList(lst);
	}
	
	/**
	 * Get the host counter list. The list is ordered by number of occurrences.
	 * @return
	 */
	public synchronized List<Counter> getHosts() {
		return getMapAsSortedList(hosts);
	}

	/**
	 * Get the protocol counter list. The list is ordered by number of occurrences.
	 * @return
	 */
	public synchronized List<Counter> getProtocol() {
		return getMapAsSortedList(protocol);
	}

	/**
	 * Get the port counter list. The list is ordered by number of occurrences.
	 * @return
	 */
	public synchronized List<Counter> getPorts() {
		return getMapAsSortedList(ports);
	}

	public synchronized long getStart() {
		return start;
	}

	public synchronized long getEnd() {
		return end;
	}

	public synchronized long getNumber() {
		return number;
	}

	/**
	 * Add a host name to the host counter.
	 * @param host 
	 */
	public synchronized void addHost(String host) {
		if (hosts.containsKey(host)) {
			hosts.get(host).increment();
		} else {
			hosts.put(host, new Counter(host));
		}
		sizeMap(hosts);
	}

	/**
	 * Add a protocol to the protocol counter.
	 * @param proto
	 */
	public synchronized void addProtocol(String proto) {
		if (protocol.containsKey(proto)) {
			protocol.get(proto).increment();
		} else {
			protocol.put(proto, new Counter(proto));
		}
		sizeMap(protocol);
	}

	/**
	 * Add a port to the port counter.
	 * @param port
	 */
	public synchronized void addPort(String port) {
		if (port == null) {
			return;
		}
		if (ports.containsKey(port)) {
			ports.get(port).increment();
		} else {
			ports.put(port, new Counter(port));
		}
		sizeMap(ports);
	}

	public synchronized void updateGlobal(LogEntry le, long now) {
		if (start == 0) {
			start = le.getDate().getTime();
		}
		if (end < le.getDate().getTime()) {
			end = le.getDate().getTime();
		}
		if (le.getDate().getTime() >= now) {
			number++;
		}
	}

	/*
	 * Resize the map to max 10 elements.
	 */
	private void sizeMap(Map<String, Counter> map) {
		Set<String> keys = map.keySet();
		final CounterComparator comp = new CounterComparator();
		if (keys.size() > 10) {
			Counter lowest = new Counter(null);
			lowest.count = -1;
			String lowestKey = null;
			for (String key : keys) {
				if (lowest.count < 0) {
					lowest = map.get(key);
				} else {
					if (comp.compare(map.get(key), lowest) >= 0) {
						lowest = map.get(key);
						lowestKey = key;
					}
				}
			}
			if (lowestKey != null) {
				map.remove(lowestKey);
			}
		}

	}

}
