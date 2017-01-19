package nl.tjonahen.iptableslogd;

import nl.tjonahen.iptableslogd.jmx.Configuration;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import nl.tjonahen.iptableslogd.domain.LogEntryCollector;
import nl.tjonahen.iptableslogd.domain.LogEntryStatistics;

import nl.tjonahen.iptableslogd.output.HttpRequestHandler;
import nl.tjonahen.iptableslogd.output.RequestThreadPool;

@Singleton
public final class StatisticsPageServer implements Observer {

    private static final Logger LOGGER = Logger.getLogger(StatisticsPageServer.class.getName());

    @Inject
    private LogEntryCollector logEntryCollector;

    @Inject
    private LogEntryStatistics logEntryStatistics;

    @Inject
    private Configuration config;

    /**
     * The thread pool instance.
     */
    @Inject
    private RequestThreadPool pool;

    @PostConstruct
    public void setup() {
        this.config.addObserver(this);
    }

    public void run() {

        LOGGER.info("Starting.");
        try {
            final ServerSocket serverSocket = new ServerSocket(config.getPort());
            // print out the port number for user
            LOGGER.fine(() -> String.format("httpServer running on port %s with context /", serverSocket.getLocalPort()));

            LOGGER.info("HttpServer up and running, serving pages.");
            // server infinite loop
            while (config.canContinue()) {
                try {
                    serverSocket.setSoTimeout(10);

                    Socket socket = serverSocket.accept();
                    LOGGER.fine(() -> String.format("New connection accepted %s:%s", socket.getInetAddress(), socket.getPort()));

                    // Construct handler to process the HTTP request message.
                    // Create a new thread to process the request.
                    pool.execute(new HttpRequestHandler(config, socket, logEntryCollector, logEntryStatistics));
                } catch (SocketTimeoutException e) {
                    // ignore time outs
                } catch (IOException e) {
                    LOGGER.severe(() -> "Fire worker thread:" + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        LOGGER.info("Exit.");
    }

    @Override
    public void update(Observable o, Object arg) {
    }
}
