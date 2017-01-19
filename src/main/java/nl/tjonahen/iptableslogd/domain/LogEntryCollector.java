package nl.tjonahen.iptableslogd.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import nl.tjonahen.iptableslogd.collection.AggregatingFixedSizeList;
import nl.tjonahen.iptableslogd.collection.FixedSizeList;

/**
 * LogEntry collector. Collects logentry objects aggragates them calculates
 * statistics etc.
 *
 * This is a shared object. It is shared between the IPTablesLogHandler and
 * HttpRequestHandler threads.
 *
 * @author Philippe Tjon-A-Hen
 *
 */
@Singleton
public final class LogEntryCollector {

    private final List<LogEntry> all = Collections.synchronizedList(new FixedSizeList<>(10));
    private final List<LogEntry> portScans = Collections.synchronizedList(new AggregatingFixedSizeList<>(5, (t) -> t.getSource()));
    /**
     * helper list to aggregate ip adresses (source) Synchronisation is done on
     * the portScans list (piggybacking)
     */
    private final AggregatingFixedSizeList<LogEntry> portScanSlots = new AggregatingFixedSizeList<>(5, (t) -> t.getSource());

    private final AggregatingFixedSizeList<LogEntry> error = new AggregatingFixedSizeList<>(10, (t) -> t.getDestination() + t.getDestinationPort());

    @Inject
    private LogEntryStatistics logEntryStatistics;
    
    @Inject 
    private PortNumbers portNumbers;

    private final long now;
    private static final long PORTSCANTIMESLOT = 5 * 1000L;

    public LogEntryCollector() {
        now = System.currentTimeMillis();
    }

    /**
     * Adds a new logentry line to the collector.
     *
     * @param logLine
     */
    public synchronized void addLogLine(String logLine) {
        LogEntry lastEntry = new LogEntry(logLine, portNumbers);
        if (!detectPortScan(lastEntry)) {
            // if a port scan was detected do not bother with statistics and
            // reporting
            // of individual dropped packages
            if (!isFromDocker(lastEntry)) {
                addToAllList(lastEntry);
                addToErrorList(lastEntry);
            }
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
        logEntryStatistics.updateGlobal(entry, now);
        logEntryStatistics.addHost(entry.getSource());
        logEntryStatistics.addProtocol(entry.getProtocol());
        logEntryStatistics.addPort(entry.portDestinationName());
        logEntryStatistics.addInInterface(entry.getInInterface());
    }

    public List<LogEntry> getErrorLogLines() {
        return new ArrayList<>(error);
    }

    public List<LogEntry> getAllLogLines() {
        return new ArrayList<>(all);
    }

    public List<LogEntry> getPortScans() {
        return new ArrayList<>(portScans);
    }

    public int getAggregateErrorCount(final LogEntry line) {
        return error.getAggregateCount(line);
    }

    private boolean isFromDocker(final LogEntry lastEntry) {
        return lastEntry.getInInterface().startsWith("docker");
    }


}
