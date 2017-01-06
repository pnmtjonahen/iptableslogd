package nl.tjonahen.iptableslogd.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.tjonahen.iptableslogd.domain.LogEntryCollector;

public final class IPTablesLogHandler implements Runnable {

    private String ulog = "";
    private static final Logger LOGGER = Logger.getLogger(IPTablesLogHandler.class.getName());

    public IPTablesLogHandler(String ulog) {
        this.ulog = ulog;
    }

    @Override
    public void run() {
        LOGGER.log(Level.FINE, "Start reading log {0}", ulog);
        while (true) {
            try (final BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(ulog)))) {
                processFile(bf);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,"IOException {0}, sleep and retry...", new Object[]{e.getMessage()});
                try {
                    // wait a full second before retry 
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                }
            }
        }

    }

    private void processFile(final BufferedReader bf) throws IOException {
        while (true) {
            try {
                String line = bf.readLine();
                if (line != null) {
                    LOGGER.log(Level.FINE, "input:{0}", line);
                    LogEntryCollector.instance().addLogLine(line);
                } else {
                    Thread.sleep(10);
                }
            } catch (InterruptedException | ParseException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }
    }

}
