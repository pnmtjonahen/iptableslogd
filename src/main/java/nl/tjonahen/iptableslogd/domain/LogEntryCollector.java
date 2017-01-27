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
import java.util.List;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import nl.tjonahen.iptableslogd.collection.AggregatingFixedSizeList;
import nl.tjonahen.iptableslogd.collection.FixedSizeList;

/**
 * LogEntry collector. Collects LogEntry objects and aggregates them.
 * Uses the Observer pattern to wait for LogEntry events.
 *
 * @author Philippe Tjon-A-Hen
 *
 */
@Singleton
public final class LogEntryCollector {

    private final List<LogEntry> all = new FixedSizeList<>(10);
    private final List<LogEntry> portScans = new AggregatingFixedSizeList<>(5, (t) -> t.getSource());
    private final AggregatingFixedSizeList<LogEntry> portScanSlots = new AggregatingFixedSizeList<>(5, (t) -> t.getSource());
    private final AggregatingFixedSizeList<LogEntry> error = new AggregatingFixedSizeList<>(10, (t) -> t.getDestination() + t.getDestinationPort());

    
    @Inject 
    private PortNumbers portNumbers;

    private static final long PORTSCANTIMESLOT = 5 * 1000L;


    /**
     * Adds a new logentry line to the collector.
     *
     * @param lastEntry
     */
    public void addLogLine(final @Observes LogEntry lastEntry) {
        
        if (!detectPortScan(lastEntry)) {
            // if a port scan was detected do not bother with statistics and
            // reporting
            // of individual dropped packages
            if (!isFromDocker(lastEntry)) {
                addToAllList(lastEntry);
                addToErrorList(lastEntry);
            }
        }
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

    
//================== private methods =====================================================
    
    private boolean detectPortScan(LogEntry entry) {
        /*
         * if we see a source more then 2 within a fixed time slot for different
         * destination ports a port scan might be in progress
         */
        if (canIgnore(entry.getDestinationPort())) {
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
        if (!canIgnore(entry.getDestinationPort())) {
            error.add(entry);
        }
    }

    private void addToAllList(LogEntry entry) {
        all.add(entry);
    }

    private boolean isFromDocker(final LogEntry lastEntry) {
        return lastEntry.getInInterface().startsWith("docker");
    }

    private boolean canIgnore(String destinationPort) {
        return portNumbers.canIgnorePort(destinationPort);
    }

}
