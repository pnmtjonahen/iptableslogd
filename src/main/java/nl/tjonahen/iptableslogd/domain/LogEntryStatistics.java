/*
 * Copyright (C) 2017 Philippe Tjon - A - Hen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tjonahen.iptableslogd.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * LogEntry statistics, counts the number of ports, hosts and protocol.
 *
 * This is shared between the IPTablesLogHandler thread via the
 * LogEntryCollector and the HttpRequestHandler thread. Th IPTablesLogHandler is
 * a single thread that updates the data, the HttpRequestHandler can be on
 * multiple thread reading this data.
 *
 * @author Philippe Tjon-A-Hen
 *
 */
@Singleton
public final class LogEntryStatistics {
    
    @Inject 
    private PortNumbers portNumbers;

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

        public void increment() {
            count++;
            lastseen = System.currentTimeMillis();
        }

        public int getCount() {
            return count;
        }

        public long getLastseen() {
            return lastseen;
        }

        public String getData() {
            return data;
        }

    }
    private final Map<String, Counter> hosts = new TreeMap<>();
    private final Map<String, Counter> protocol = new TreeMap<>();
    private final Map<String, Counter> ports = new TreeMap<>();
    private final Map<String, Counter> inInterfaces = new TreeMap<>();

    private List<Counter> getMapAsSortedList(Map<String, Counter> map) {
        synchronized (map) {
            return map.values().stream().sorted(new CounterComparator()).collect(Collectors.toList());
        }
    }

    /**
     * Get the host counter list. The list is ordered by number of occurrences.
     *
     * @return
     */
    public List<Counter> getHosts() {
        return getMapAsSortedList(hosts);
    }

    /**
     * Get the protocol counter list. The list is ordered by number of
     * occurrences.
     *
     * @return
     */
    public List<Counter> getProtocol() {
        return getMapAsSortedList(protocol);
    }

    /**
     * Get the port counter list. The list is ordered by number of occurrences.
     *
     * @return
     */
    public List<Counter> getPorts() {
        return getMapAsSortedList(ports);
    }

    /**
     * Get the inInterface counter list. The list is ordered by number of
     * occurrences.
     *
     * @return
     */
    public List<Counter> getInInterfaces() {
        return getMapAsSortedList(inInterfaces);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getNumber() {
        return number;
    }

    public void updateStatistics(final @Observes LogEntry entry) {
        updateGlobal(entry);
        addHost(entry.getSource());
        addProtocol(entry.getProtocol());
        addPort(portNumbers.getDescription(entry.getDestinationPort(), entry.getProtocol()));
        addInInterface(entry.getInInterface());
    }
    
    /**
     * Add a host name to the host counter.
     *
     * @param host
     */
    private void addHost(String host) {
        synchronized (host) {
            if (hosts.containsKey(host)) {
                hosts.get(host).increment();
            } else {
                hosts.put(host, new Counter(host));
            }
            sizeMap(hosts);
        }
    }

    /**
     * Add a protocol to the protocol counter.
     *
     * @param proto
     */
    private void addProtocol(String proto) {
        synchronized (protocol) {
            if (protocol.containsKey(proto)) {
                protocol.get(proto).increment();
            } else {
                protocol.put(proto, new Counter(proto));
            }
            sizeMap(protocol);
        }
    }

    /**
     * Add a port to the port counter.
     *
     * @param port
     */
    private void addPort(String port) {
        if (port == null || "".equals(port)) {
            return;
        }
        synchronized (ports) {
            if (ports.containsKey(port)) {
                ports.get(port).increment();
            } else {
                ports.put(port, new Counter(port));
            }
            sizeMap(ports);
        }
    }

    /**
     * Add a inInterface to the inInterface counter.
     *
     * @param inInterface
     */
    private void addInInterface(String inInterface) {
        if (inInterface == null || "".equals(inInterface)) {
            return;
        }
        synchronized (inInterfaces) {
            if (inInterfaces.containsKey(inInterface)) {
                inInterfaces.get(inInterface).increment();
            } else {
                inInterfaces.put(inInterface, new Counter(inInterface));
            }
            sizeMap(inInterfaces);
        }
    }

    private void updateGlobal(LogEntry le) {
        if (start == 0) {
            start = le.getDate().getTime();
        }
        if (end < le.getDate().getTime()) {
            end = le.getDate().getTime();
        }
        number++;
    }

    /*
     *  Resize the map to max 10 elements.
     */
    private void sizeMap(Map<String, Counter> map) {
        final CounterComparator comparator = new CounterComparator();

        if (map.keySet().size() > 10) {
            final List<Entry<String, Counter>> entries = new ArrayList<>(map.entrySet());
            Collections.sort(entries, (Entry<String, Counter> o1, Entry<String, Counter> o2) -> comparator.compare(o1.getValue(), o2.getValue()));
            map.clear();
            entries.stream().limit(10).forEach((e) -> map.put(e.getKey(), e.getValue()));
        }

    }

}
