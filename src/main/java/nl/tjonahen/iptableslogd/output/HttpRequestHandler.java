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
package nl.tjonahen.iptableslogd.output;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import java.util.logging.Logger;

import nl.tjonahen.iptableslogd.jmx.Configuration;
import nl.tjonahen.iptableslogd.domain.LogEntry;
import nl.tjonahen.iptableslogd.domain.LogEntryCollector;
import nl.tjonahen.iptableslogd.domain.LogEntryStatistics;
import nl.tjonahen.iptableslogd.domain.LogEntryStatistics.Counter;

/**
 * Request handler, handles a single get.
 *
 */
public final class HttpRequestHandler implements Runnable {

    private static final String CRLF = "\r\n";
    private static final String SERVERLINE = "Server: iptableslogd httpServer";
    private final OutputStream output;
    private final Configuration config;
    private final LogEntryCollector logEntryCollector;
    private final LogEntryStatistics logEntryStatistics;

    private static final Logger LOGGER = Logger.getLogger(HttpRequestHandler.class.getName());

    public HttpRequestHandler(final Configuration config, final OutputStream output, final LogEntryCollector logEntryCollector, final LogEntryStatistics logEntryStatistics) {
        this.config = config;
        this.output = output;
        this.logEntryCollector = logEntryCollector;
        this.logEntryStatistics = logEntryStatistics;
    }

    @Override
    public void run() {
        LOGGER.fine("Create response page.");
        try {
            LOGGER.fine("Building result page.");
            final StringBuilder entityBody = new StringBuilder("");
            entityBody.append("<HTML><HEAD><TITLE>IPTables LogD</TITLE>")
                    .append(addMetaData())
                    .append(addStyle())
                    .append("</HEAD>")
                    .append("<BODY><center><h1> I P T A B L E S  L O G </h1></center><hr/>")
                    .append(buildBody())
                    .append("</BODY></HTML>");

            // Construct the response message.
            final String statusLine = "HTTP/1.0 200 OK";
            final String contentTypeLine = "Content-type: text/html";
            final String contentLengthLine = "Content-Length: ";

            final StringBuilder htmlPage = new StringBuilder("");
            htmlPage.append(statusLine)
                    .append(CRLF)
                    .append(SERVERLINE)
                    .append(contentTypeLine)
                    .append(CRLF)
                    .append(contentLengthLine)
                    .append((Integer.valueOf(entityBody.length())).toString())
                    .append(CRLF)
                    .append(CRLF)
                    .append(entityBody);
            // Send the entity body.
            output.write(htmlPage.toString().getBytes());
            output.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create/write response ", e);
        }
    }

    private String addMetaData() {
        return "<META HTTP-EQUIV='Refresh' CONTENT='10; URL=/'>";
    }

    private String addStyle() {
        return "<style>"
                //                + "table tr.special td { border-bottom: 1px solid #ff0000;  }"
                + "body { font-family: Courier, monospace; font-size: 8pt; background-image:url('data:image/jpeg;base64,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') }"
                + "table.special { border: 2px solid black; }"
                + "</style>";
//		 + "body { font-family: Courier, monospace; font-size: 8pt; margin: 0px; padding: 0px; background: blue; color: yellow;}</style>";
    }

    private String buildBody() {
        final StringBuilder data = new StringBuilder("");
        data.append("<table width='100%'><tr><td valign='top' width='60%'>");
        data.append(buildAllEntryTable());
        data.append(buildErrorEntryTable());
        data.append(buildPortScanTable());
        data.append("</td><td valign='top' width='40%'>");
        data.append(buildStatistics());
        data.append("</td></tr></table>");
        return data.toString();
    }

    private String buildStatistics() {
        final StringBuilder data = new StringBuilder("");
        data.append(buildGlobalStatistics());
        data.append(buildStatisticsTable("IN statistics:", logEntryStatistics.getInInterfaces()));
        data.append(buildStatisticsTable("Protocol statistics:", logEntryStatistics.getProtocol()));
        data.append(buildStatisticsTable("Port statistics:", logEntryStatistics.getPorts()));
        data.append(buildStatisticsTable("Host statistics:", logEntryStatistics.getHosts()));
        return data.toString();
    }

    private String buildGlobalStatistics() {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>Global statistics:</h3>");
        data.append("<table class='special' width='100%'>");
        data.append("<tr>");
        data.append("<td width='20%'>First</td>");
        data.append("<td width='80%'>").append(new Date(logEntryStatistics.getStart())).append("</td>");
        data.append("</tr>");
        data.append("<tr>");
        data.append("<td width='20%'>Last</td>");
        data.append("<td width='80%'>").append(new Date(logEntryStatistics.getEnd())).append("</td>");
        data.append("</tr>");
        data.append("</table>");
        data.append("<table width='100%'>");
        data.append("<tr>");
        data.append("<td width='80%'>Count</td>");
        data.append("<td width='20%'>").append(logEntryStatistics.getNumber()).append("</td>");
        data.append("</tr>");
        data.append("</table>");
        return data.toString();
    }

    private String buildStatisticsTable(String name, List<Counter> lst) {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>").append(name).append("</h3>");
        data.append("<table class='special' width='100%'>");
        lst.stream().map((es) -> {
            data.append("<tr>");
            data.append("<td width='90%'>").append(es.getData()).append("</td>");
            return es;
        }).map((es) -> {
            data.append("<td width='10%'>").append(es.getCount()).append("</td>");
            return es;
        }).forEachOrdered((_item) -> {
            data.append("</tr>");
        });
        data.append("</table>");
        return data.toString();
    }

    private String buildErrorEntryTable() {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>Last suspicious packages dropped:</h3>");
        data.append("<table  class='special' width='100%'>");
        data.append("<tr><td nowrap width='10%'>Date/Time</td><td width='25%'>source</td><td>proto</td><td width='100%'>port</td></tr>");
        List<LogEntry> lines = logEntryCollector.getErrorLogLines();
        lines.forEach((line) -> {
            data.append(addLogEntry(line, 0));
        });
        data.append("</table>");
        return data.toString();
    }

    private String buildPortScanTable() {
        final StringBuffer data = new StringBuffer("");
        data.append("<h3>Possible portscan sources</h3>");
        data.append("<table  class='special' width='100%'>");
        data.append("<tr><td nowrap width='10%'>Date/Time</td><td width='25%'>source</td></tr>");
        Collections.synchronizedList(logEntryCollector.getPortScans())
                .stream()
                .filter((line) -> (line != null))
                .map((line) -> {
                    data.append("<tr>");
                    data.append("<td nowrap>").append(line.getDateTime()).append("</td>");
                    return line;
                })
                .map((line) -> {
                    data.append("<td>");
                    if (config.getUseReverseLookup()) {
                        try {
                            final InetAddress cacheInetAddress = InetAddress.getByName(line.getSource());
                            data.append(cacheInetAddress.getHostName());
                        } catch (UnknownHostException e) {
                            data.append(line.getSource());
                        }
                    } else {
                        data.append(line.getSource());
                    }
                    data.append("</td>");
                    return line;
                })
                .map((line) -> {
                    data.append("</tr>");
                    return line;
                });
        data.append("</table>");
        return data.toString();
    }

    private String buildAllEntryTable() {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>Last packages dropped:</h3>");
        data.append("<table  class='special' width='100%'>");
        data.append("<tr><td nowrap width='10%'>Date/Time</td><td width='25%'>source</td><td>proto</td><td width='100%'>port</td></tr>");
        List<LogEntry> lines = logEntryCollector.getAllLogLines();
        lines.forEach((line) -> {
            data.append(addLogEntry(line, -1));
        });
        data.append("</table>");
        return data.toString();
    }

    private String addLogEntry(final LogEntry line, final int count) {
        final StringBuilder data = new StringBuilder("");
        if (line != null) {
            if (line.isAttack()) {
                data.append("<tr>");
            } else {
                data.append("<tr>");
            }
            data.append("<td nowrap>").append(line.getDateTime()).append("</td>");
            data.append("<td>");
            if (config.getUseReverseLookup()) {
                try {
                    InetAddress cacheInetAddress = InetAddress.getByName(line.getSource());
                    data.append(cacheInetAddress.getHostName());
                } catch (UnknownHostException e) {
                    data.append(line.getSource());
                }
            } else {
                data.append(line.getSource());
            }
            if (count > 0) {
                data.append("(").append(count).append(")");
            }
            data.append("</td>");
            data.append("<td>").append(line.getProtocol()).append("</td>");
            data.append("<td>").append(line.portDestinationName()).append("</td>");
            data.append("</tr>");
        }
        return data.toString();
    }

}
