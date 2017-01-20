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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import nl.tjonahen.iptableslogd.domain.LogEntryCollector;
import nl.tjonahen.iptableslogd.jmx.Configuration;

@Singleton
public final class IPTablesLogHandler implements Observer {

    private static final Logger LOGGER = Logger.getLogger(IPTablesLogHandler.class.getName());

    private String ulog;
    private File file;

    @Inject
    private Configuration config;

    @Inject
    private LogEntryCollector logEntryCollector;

    private long last; // The last time the file was checked for changes
    private long position; // position within the file

    private void setLogFile(String ulog) {
        LOGGER.info(() -> "Start reading log " + ulog);
        this.ulog = ulog;
        this.file = new File(ulog);
    }

    @PostConstruct
    public void setup() {
        config.addObserver(this);
    }

    public void start() {
        new Thread(this::run).start();
    }
    
    public void run() {
        setLogFile(config.getUlog());

        try {
            last = 0;
            position = 0;
            RandomAccessFile reader = openReader();

            while (config.canContinue()) {
                if (isRotated()) {
                    reader = rotateReader(reader);
                    continue;
                } else if (resetFile()) {
                    setLogFile(config.getUlog());
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

    private boolean isRotated() {
        return file.length() < position;
    }

    private boolean resetFile() {
        return !ulog.equals(config.getUlog());
    }
    private void processNewLines(RandomAccessFile reader) throws IOException {
        if (isMoreDataAvailable()) {
            last = System.currentTimeMillis();
            position = readLines(reader);
        } else if (isFileNewer()) {

            /* This can happen if the file is truncated or overwritten
            * with the exact same length of information. In cases like
            * this, the file position needs to be reset
             */
            position = 0;
            reader.seek(position); // cannot be null here

            // Now we can read new lines
            last = System.currentTimeMillis();
            position = readLines(reader);
        }
    }

    private boolean isMoreDataAvailable() {
        return file.length() > position;
    }

    private RandomAccessFile rotateReader(final RandomAccessFile oldReader) {
        LOGGER.info("File was rotated... ");
        try {
            final RandomAccessFile reader = new RandomAccessFile(file, "r");
            position = 0;
            closeQuietly(oldReader);
            return reader;
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "File not found ", e);
        }
        return oldReader;
    }

    private RandomAccessFile openReader() throws IOException {
        // Open the file
        RandomAccessFile reader = null;
        while (reader == null) {
            try {
                reader = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "File not found ", e);
            }
            if (reader == null) {
                sleepQuietly();
            } else {
                // The current position in the end of the file
                position = file.length();
                last = System.currentTimeMillis();
                reader.seek(position);
            }
        }
        return reader;
    }

    private void sleepQuietly() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            // do nothing. suppress exceptions
        }
    }

    private long readLines(final RandomAccessFile reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            LOGGER.log(Level.FINE, "input: {0}", line);
            logEntryCollector.addLogLine(line);
            line = reader.readLine();
        }
        return reader.getFilePointer();
    }

    private boolean isFileNewer() {
        if (!file.exists()) {
            return false;
        }
        return file.lastModified() > last;
    }

    private void closeQuietly(final RandomAccessFile raFile) {
        if (raFile != null) {
            try {
                raFile.close();
            } catch (IOException e) {
                // do nothing. suppress exceptions
            }
        }
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        if (!ulog.equals(config.getUlog())) {
            setLogFile(config.getUlog());
        }
    }

}
