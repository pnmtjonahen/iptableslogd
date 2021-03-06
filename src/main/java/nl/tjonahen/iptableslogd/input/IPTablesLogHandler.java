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
package nl.tjonahen.iptableslogd.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import nl.tjonahen.cdi.value.Value;
import nl.tjonahen.iptableslogd.domain.LogEntry;

import nl.tjonahen.iptableslogd.jmx.Configuration;

/**
 * Auto started or self started CDI bean, Uses CDI events to notify all
 * observers of new LogEntry events.
 *
 * @author Philippe Tjon - A - Hen philippe@tjonahen.nl
 */
@ApplicationScoped
public class IPTablesLogHandler {

    private static final Logger LOGGER = Logger.getLogger(IPTablesLogHandler.class.getName());

    @Inject
    @Value(key = "ulog", value = "/var/log/ulogd.syslogemu")
    private String configUlog;

    private String ulog;
    private File file;

    @Inject
    private Event<LogEntry> logEntryEvent;

    private long last; // The last time the file was checked for changes
    private long position; // position within the file
    private boolean forceStop = false;

    /**
     * This method is called when this applicationScoped bean is initialized It
     * performs post construct initialization and starts the reader thread.
     *
     * @param init -
     */
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        new Thread(this::run).start();
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        forceStop = true;
    }

    private void run() {
        setLogFile(configUlog);

        try {
            last = 0;
            position = 0;
            BufferedReader reader = openReader();
            while (canContinue()) {
                if (isRotated()) {
                    reader = rotateReader(reader);
                    continue;
                } else if (resetFile()) {
                    setLogFile(configUlog);
                    reader = openReader();
                } else {
                    processNewLines(reader);
                }
                sleepQuietly();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Unable to read log " + ulog, e);
        }
        LOGGER.info(() -> "Stop reading log " + ulog);
    }

    private void setLogFile(String ulog) {
        LOGGER.info(() -> "Start reading log " + ulog);
        this.ulog = ulog;
        this.file = new File(ulog);
    }

    private boolean isRotated() {
        return file.length() < position;
    }

    private boolean resetFile() {
        return !ulog.equals(configUlog);
    }

    private void processNewLines(BufferedReader reader) throws IOException {
        if (isMoreDataAvailable()) {
            last = System.currentTimeMillis();
            position = readLines(reader);
        } else if (isFileNewer()) {

            /* This can happen if the file is truncated or overwritten
            * with the exact same length of information. In cases like
            * this, the file position needs to be reset
             */
            position = 0;

            // Now we can read new lines
            last = System.currentTimeMillis();
            position = readLines(reader);
        }
    }

    private boolean isMoreDataAvailable() {
        return file.length() > position;
    }

    private BufferedReader rotateReader(final BufferedReader oldReader) {
        LOGGER.info("File was rotated... ");
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            position = 0;
            closeQuietly(oldReader);
            return reader;
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "File not found ", e);
        }
        return oldReader;
    }

    private BufferedReader openReader() throws IOException {
        // Open the file
        BufferedReader reader = null;
        while (reader == null) {
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "File not found ", e);
            }
            if (reader == null) {
                sleepQuietly();
            } else {
                // The current position in the file (aka  start)
                position = 0;
                last = System.currentTimeMillis();
            }
        }
        return reader;
    }

    private void sleepQuietly() {
        if (canContinue()) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                // do nothing. suppress exceptions
            }
        }
    }

    private long readLines(final BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            LOGGER.log(Level.FINE, "input: {0}", line);
            logEntryEvent.fire(new LogEntry(line));
            if (!canContinue()) {
                return file.length();
            }
            line = reader.readLine();
        }
        return file.length();
    }

    private boolean isFileNewer() {
        if (!file.exists()) {
            return false;
        }
        return file.lastModified() > last;
    }

    private void closeQuietly(final BufferedReader raFile) {
        if (raFile != null) {
            try {
                raFile.close();
            } catch (IOException e) {
                // do nothing. suppress exceptions
            }
        }
    }

    public void update(final @Observes Configuration c) {
        LOGGER.info(() -> String.format("Update received from %s", c.getClass().getName()));

        configUlog = c.getUlog();
        forceStop = !c.canContinue();
    }

    private boolean canContinue() {
        return !forceStop;
    }

}
