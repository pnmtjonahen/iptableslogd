package nl.tjonahen.iptableslogd.domain;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import nl.tjonahen.iptableslogd.collection.AggregatingFixedSizeList;
import nl.tjonahen.iptableslogd.collection.FixedSizeList;

/**
 * LogEntry collector. Collects logentry objects aggragates them calculates statistics etc.
 *
 * This is a shared object. It is shared between the IPTablesLogHandler and HttpRequestHandler threads.
 *
 * @author Philippe Tjon-A-Hen
 *
 */
public final class LogEntryCollector {

    private static LogEntryCollector instance = null;

    public static synchronized LogEntryCollector instance() {
        if (instance == null) {
            instance = new LogEntryCollector();
        }
        return instance;
    }

    private List<LogEntry> all = Collections
            .synchronizedList(new FixedSizeList<LogEntry>(10));
    private List<LogEntry> portScans = Collections
            .synchronizedList(new AggregatingFixedSizeList<LogEntry>(5,
                            new SourceExtractor<LogEntry>()));
    /**
     * helper list to aggregate ip adresses (source) Synchronisation is done on the portScans list (piggybacking)
     */
    private AggregatingFixedSizeList<LogEntry> portScanSlots = new AggregatingFixedSizeList<LogEntry>(
            5, new SourceExtractor<LogEntry>());

    private List<LogEntry> error = new AggregatingFixedSizeList<LogEntry>(10,
            new DestinationAndPortExtractor<LogEntry>());

    private LogEntryStatistics ipTablesStatistics = new LogEntryStatistics();

    private long now;
    private static final long DAY = 24 * 60 * 60 * 1000L;
    private static final long PORTSCANTIMESLOT = 5 * 1000L;

    private LogEntryCollector() {
        now = System.currentTimeMillis();
    }

    /**
     * Adds a new logentry line to the collector.
     *
     * @param logLine
     * @throws ParseException
     */
    public synchronized void addLogLine(String logLine) throws ParseException {
        LogEntry lastEntry = new LogEntry(logLine);
        if (lastEntry.getDate().getTime() > (now - DAY)
                && !detectPortScan(lastEntry)) {
			// if a port scan was detected do not bother with statistics and
            // reporting
            // of individual dropped packages
            addToAllList(lastEntry);
            addToErrorList(lastEntry);
            updateStatistics(lastEntry);

        }
    }

    private boolean detectPortScan(LogEntry entry) {
        /*
         * if we see a source more then 2 within a fixed time slot for different
         * destination ports a port scan might be in progress
         */
        if (entry.canIgnore()) {
            return false;
        }
        portScanSlots.add(entry);
        if (portScanSlots.getAggregateCount(entry) > 2) {
            for (LogEntry p : portScanSlots) {
                if (entry.getSource().equals(p.getSource())
                        && (entry.getDate().getTime() - p.getDate().getTime()) < PORTSCANTIMESLOT) {
                    // raise portscan for source;
                    portScans.add(entry);
                    return true;
                }
            }
        }
        return false;

    }

    private void addToErrorList(LogEntry entry) {
        if (!entry.canIgnore()) {
            error.add(entry);
        }
    }

    private void addToAllList(LogEntry entry) {
        all.add(entry);
    }

    private void updateStatistics(LogEntry entry) {
        ipTablesStatistics.updateGlobal(entry, now);
        ipTablesStatistics.addHost(entry.getSource());
        ipTablesStatistics.addProtocol(entry.getProtocol());
        ipTablesStatistics.addPort(entry.portDestinationName());
    }

    public LogEntryStatistics getIpTablesStatistics() {
        return ipTablesStatistics;
    }

    public List<LogEntry> getErrorLogLines() {
        return error;
    }

    public List<LogEntry> getAllLogLines() {
        return all;
    }

    public List<LogEntry> getPortScans() {
        return portScans;
    }

    public int getAggregateErrorCount(LogEntry line) {
        return ((AggregatingFixedSizeList<LogEntry>) error)
                .getAggregateCount(line);
    }

}
