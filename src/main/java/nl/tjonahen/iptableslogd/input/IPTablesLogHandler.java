package nl.tjonahen.iptableslogd.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        BufferedReader bf;
        try {
            bf = new BufferedReader(new InputStreamReader(
                    new FileInputStream(ulog)));
        } catch (FileNotFoundException e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage());
            return;
        }
        while (true) {
            try {
                String line = bf.readLine();
                if (line != null) {
                    LOGGER.log(Level.FINE, "input:{0}", line);
                    LogEntryCollector.instance().addLogLine(line);
                } else {
                    Thread.sleep(10);
                }
            } catch (IOException | InterruptedException | ParseException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }

    }

}
