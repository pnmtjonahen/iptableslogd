package nl.tjonahen.iptableslogd;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import nl.tjonahen.iptableslogd.input.IPTablesLogHandler;
import nl.tjonahen.iptableslogd.output.HttpRequestHandler;

public final class HttpServer {
    private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());

    public static void main(String args[]) {
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

        LOGGER.info("iptableslogd starting.");
        // start the log messages reader thread
        new Thread(new IPTablesLogHandler(ulog)).start();

        try {
            // Start the server.
            new HttpServer().server(port, poolSize, context);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | IOException e) {
            LOGGER.severe(e.getMessage());
        }
        LOGGER.info("iptableslogd exit.");
    }

    /**
     * The thread pool instance.
     */
    private ExecutorService pool;

    private void server(int port, int poolSize, String context) throws MalformedObjectNameException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException, IOException {
        ServerSocket serverSocket;

        // setup HttpServerConfiguratie MBean
        HttpServerConfiguration config = new HttpServerConfiguration(this, poolSize, context);

        setupJmx(config);

        // print out the port number for user
        serverSocket = new ServerSocket(port);
        LOGGER.fine(() ->  String.format("httpServer running on port %s with context %s", serverSocket.getLocalPort(), context) );
        setupPool(config);

        LOGGER.info("httpServer up and running...");
        // server infinite loop
        while (config.canContinue()) {
            try {
                serverSocket.setSoTimeout(10);

                Socket socket = serverSocket.accept();
                LOGGER.fine(() -> String.format("New connection accepted %s:%s", socket.getInetAddress(), socket.getPort()));

                // Construct handler to process the HTTP request message.
                // Create a new thread to process the request.
                pool.execute(new HttpRequestHandler(config, socket));
            } catch (SocketTimeoutException e) {
                // ignore time outs
            } catch (IOException e) {
                LOGGER.severe(() -> "fire worker thread:" + e.getMessage());
            }
        }
        shutdownAndAwaitTermination(pool);

    }

    public void setupPool(HttpServerConfigurationMBean config) {
        if (pool != null) {
            shutdownAndAwaitTermination(pool);
        }
        LOGGER.info("httpServer setup thread pool.");
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

    private void setupJmx(HttpServerConfigurationMBean config) throws MalformedObjectNameException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {

        LOGGER.info("httpServer setup jmx.");

        // Get the Platform MBean Server
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        // Construct the ObjectName for the MBean we will register
        ObjectName name = new ObjectName("nl.tjonahen.iptableslogd.HttpServer:type=configuration");
        // Register the HttpServer configuration MBean
        mbs.registerMBean(config, name);
    }
}
