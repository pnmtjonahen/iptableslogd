package nl.tjonahen.iptableslogd;

import nl.tjonahen.iptableslogd.jmx.Configuration;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import nl.tjonahen.iptableslogd.domain.LogEntryCollector;

import nl.tjonahen.iptableslogd.input.IPTablesLogHandler;
import nl.tjonahen.iptableslogd.output.HttpRequestHandler;
import nl.tjonahen.iptableslogd.jmx.ConfigurationMBean;

@Singleton
public final class StatisticsPageServer implements Observer {
    private static final Logger LOGGER = Logger.getLogger(StatisticsPageServer.class.getName());

    @Inject 
    private LogEntryCollector logEntryCollector;
    
    public void run(String args[]) {
        int port = 4000;
        int poolSize = 5;
        String ulog = "/var/log/ulogd.syslogemu";
        String context = "/";
        // Parse commandline params
        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            if ("--port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            if ("-t".equals(args[i])) {
                poolSize = Integer.parseInt(args[++i]);
            }
            if ("--threads".equals(args[i])) {
                poolSize = Integer.parseInt(args[++i]);
            }
            if ("-i".equals(args[i])) {
                ulog = args[++i];
            }
            if ("--input".equals(args[i])) {
                ulog = args[++i];
            }
            if ("-c".equals(args[i])) {
                context = args[++i];
            }
            if ("--context".equals(args[i])) {
                context = args[++i];
            }
        }

        LOGGER.info("Starting.");

        try {
            this.config = new Configuration();
            this.config.setPoolSize(poolSize);
            
            this.config.addObserver(this);

            initJmx();
            initLogHandler(ulog);

            server(port, context);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | IOException e) {
            LOGGER.severe(e.getMessage());
        }
        LOGGER.info("Exit.");
    }

    /**
     * The thread pool instance.
     */
    private ExecutorService pool;
    private Configuration config;
    
    private void initLogHandler(String ulog) {
        new Thread(new IPTablesLogHandler(ulog, config, logEntryCollector)).start();
    }
    
    private void initJmx() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanRegistrationException, NotCompliantMBeanException {
        // setup HttpServerConfiguratie MBean
        setupJmx(config);
       
    }

    private void server(int port, String context) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        // print out the port number for user
        LOGGER.fine(() ->  String.format("httpServer running on port %s with context %s", serverSocket.getLocalPort(), context) );
        setupPool(config);

        LOGGER.info("HttpServer up and running, serving pages.");
        // server infinite loop
        while (config.canContinue()) {
            try {
                serverSocket.setSoTimeout(10);

                Socket socket = serverSocket.accept();
                LOGGER.fine(() -> String.format("New connection accepted %s:%s", socket.getInetAddress(), socket.getPort()));

                // Construct handler to process the HTTP request message.
                // Create a new thread to process the request.
                pool.execute(new HttpRequestHandler(config, socket, logEntryCollector));
            } catch (SocketTimeoutException e) {
                // ignore time outs
            } catch (IOException e) {
                LOGGER.severe(() -> "Fire worker thread:" + e.getMessage());
            }
        }
        shutdownAndAwaitTermination(pool);

    }

    public void setupPool(ConfigurationMBean config) {
        if (pool != null) {
            shutdownAndAwaitTermination(pool);
        }
        LOGGER.fine("HttpServer setup thread pool.");
        pool = Executors.newFixedThreadPool(config.getPoolSize());
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
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

    private void setupJmx(final ConfigurationMBean config) throws MalformedObjectNameException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {

        LOGGER.info("Setup jmx.");

        // Get the Platform MBean Server
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        // Construct the ObjectName for the MBean we will register
        final ObjectName name = new ObjectName("nl.tjonahen.iptableslogd.Config:type=configuration");
        // Register the HttpServer configuration MBean
        mbs.registerMBean(config, name);
    }

    @Override
    public void update(Observable o, Object arg) {
        setupPool(config);
    }
}
