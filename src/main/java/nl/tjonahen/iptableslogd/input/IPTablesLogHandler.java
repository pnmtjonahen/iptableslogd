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
    private Logger logger = Logger.getLogger(IPTablesLogHandler.class.getName());

    public IPTablesLogHandler(String ulog) {
        this.ulog = ulog;
    }

    public void run() {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Start reading log " + ulog);
        }
        BufferedReader bf;
        try {
            bf = new BufferedReader(new InputStreamReader(
                    new FileInputStream(ulog)));
        } catch (FileNotFoundException e1) {
            logger.log(Level.SEVERE, e1.getMessage());
            return;
        }
        while (true) {
            try {
                String line = bf.readLine();
                if (line != null) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.log(Level.INFO, "input:" + line);
                    }
                    LogEntryCollector.instance().addLogLine(line);
                } else {
                    Thread.sleep(10);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage());
            } catch (ParseException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }

    }

}
