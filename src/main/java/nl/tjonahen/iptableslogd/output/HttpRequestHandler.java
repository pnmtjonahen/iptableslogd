package nl.tjonahen.iptableslogd.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.tjonahen.iptableslogd.HttpServerConfiguration;
import nl.tjonahen.iptableslogd.domain.LogEntry;
import nl.tjonahen.iptableslogd.domain.LogEntryCollector;
import nl.tjonahen.iptableslogd.domain.LogEntryStatistics.Counter;

/**
 * Request handler, handles a single get.
 *
 */
public final class HttpRequestHandler implements Runnable {

    private static final String CRLF = "\r\n";
    private static final String SERVERLINE = "Server: iptableslogd httpServer";
    private final Socket socket;
    private final OutputStream output;
    private final BufferedReader br;
    private final HttpServerConfiguration config;
    private final Logger logger = Logger.getLogger(HttpRequestHandler.class.getName());

    // Constructor
    public HttpRequestHandler(final HttpServerConfiguration config, final Socket socket)
            throws IOException {
        this.config = config;
        this.socket = socket;
        this.output = socket.getOutputStream();
        this.br = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));

    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private void processRequest() {
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "Building result page.");
            }
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
            // ignore io errors
        } finally {
            try {
                output.close();
                br.close();
                socket.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    private String addMetaData() {
        return "<META HTTP-EQUIV='Refresh' CONTENT='10; URL=" + config.getContext() + "'>";
    }

    private String addStyle() {
        return "<style>table tr.special td { border-bottom: 1px solid #ff0000;  }"
                + "body { font-family: Courier, monospace; font-size: 8pt; }</style>";
//		 "body { font-family: Courier, monospace; font-size: 8pt; margin: 0px; padding: 0px; background: blue; color: yellow;}</style>";
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
        data.append(buildStatisticsTable("Protocol statistics:", LogEntryCollector.instance().getIpTablesStatistics().getProtocol()));
        data.append(buildStatisticsTable("Port statistics:", LogEntryCollector.instance().getIpTablesStatistics().getPorts()));
        data.append(buildStatisticsTable("Host statistics:", LogEntryCollector.instance().getIpTablesStatistics().getHosts()));
        return data.toString();
    }

    private String buildGlobalStatistics() {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>Global statistics:</h3>");
        data.append("<table width='100%'>");
        data.append("<tr>");
        data.append("<td width='20%'>First</td>");
        data.append("<td width='80%'>" + new Date(LogEntryCollector.instance().getIpTablesStatistics().getStart()) + "</td>");
        data.append("</tr>");
        data.append("<tr>");
        data.append("<td width='20%'>Last</td>");
        data.append("<td width='80%'>" + new Date(LogEntryCollector.instance().getIpTablesStatistics().getEnd()) + "</td>");
        data.append("</tr>");
        data.append("</table>");
        data.append("<table width='100%'>");
        data.append("<tr>");
        data.append("<td width='80%'>Count</td>");
        data.append("<td width='20%'>" + LogEntryCollector.instance().getIpTablesStatistics().getNumber() + "</td>");
        data.append("</tr>");
        data.append("</table>");
        return data.toString();
    }

    private String buildStatisticsTable(String name, List<Counter> lst) {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>").append(name).append("</h3>");
        data.append("<table width='100%'>");
        for (Counter es : lst) {
            data.append("<tr>");
            data.append("<td width='90%'>" + es.getData() + "</td>");
            data.append("<td width='10%'>" + es.getCount() + "</td>");
            data.append("</tr>");
        }
        data.append("</table>");
        return data.toString();
    }

    private String buildErrorEntryTable() {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>Last suspicious packages dropped:</h3>");
        data.append("<table width='100%'>");
        data.append("<tr><td nowrap width='10%'>Date/Time</td><td width='25%'>source</td><td>proto</td><td width='100%'>port</td></tr>");
        List<LogEntry> lines = LogEntryCollector.instance().getErrorLogLines();
        for (LogEntry line : lines) {
            data.append(addLogEntry(line, 0));
        }
        data.append("</table>");
        return data.toString();
    }

    private String buildPortScanTable() {
        final StringBuffer data = new StringBuffer("");
        data.append("<h3>Possible portscan sources</h3>");
        data.append("<table width='100%'>");
        data.append("<tr><td nowrap width='10%'>Date/Time</td><td width='25%'>source</td></tr>");
        List<LogEntry> lines = Collections.synchronizedList(LogEntryCollector.instance().getPortScans());
        for (LogEntry line : lines) {
            if (line != null) {
                data.append("<tr>");
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
                data.append("</td>");
                data.append("</tr>");
            }
        }
        data.append("</table>");
        return data.toString();
    }

    private String buildAllEntryTable() {
        final StringBuilder data = new StringBuilder("");
        data.append("<h3>Last packages dropped:</h3>");
        data.append("<table width='100%'>");
        data.append("<tr><td nowrap width='10%'>Date/Time</td><td width='25%'>source</td><td>proto</td><td width='100%'>port</td></tr>");
        List<LogEntry> lines = LogEntryCollector.instance().getAllLogLines();
        for (LogEntry line : lines) {
            data.append(addLogEntry(line, -1));
        }
        data.append("</table>");
        return data.toString();
    }

    private String addLogEntry(final LogEntry line, final int count) {
        final StringBuilder data = new StringBuilder("");
        if (line != null) {
            if (line.isAttack()) {
                data.append("<tr class='special'>");
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