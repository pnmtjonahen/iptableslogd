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

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.tjonahen.iptableslogd.jmx.Configuration;

/**
 *
 * @author Philippe Tjon - A - Hen
 */
@Singleton
public class RequestThreadPool implements Observer {

    private static final Logger LOGGER = Logger.getLogger(RequestThreadPool.class.getName());

    /**
     * The thread pool instance.
     */
    private ExecutorService pool;
    private int currentPoolSize;
    
    @Inject
    private Configuration config;


    @PostConstruct
    public void setup() {
        config.addObserver(this);
        setupPool();
    }
    

    @Override
    public void update(Observable o, Object arg) {
        if (currentPoolSize != config.getPoolSize()) {
            setupPool();
        }
    }

    private void setupPool() {
        if (pool != null) {
            shutdownAndAwaitTermination();
        }
        this.currentPoolSize = config.getPoolSize();
        LOGGER.info(() ->  String.format("Setup thread pool for %d threads", currentPoolSize));
        pool = Executors.newFixedThreadPool(currentPoolSize);
    }

    @PreDestroy
    public void shutdownAndAwaitTermination() {
        LOGGER.info("Shutdown thread pool.");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.severe("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public void execute(HttpRequestHandler httpRequestHandler) {
        pool.execute(httpRequestHandler);
    }

}
