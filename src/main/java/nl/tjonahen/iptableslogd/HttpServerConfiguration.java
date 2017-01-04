package nl.tjonahen.iptableslogd;

/**
 * HttpServerConfiguration MBean.
 *
 */
public final class HttpServerConfiguration implements HttpServerConfigurationMBean {

    private int poolSize;
    private boolean useReverseLookup = false;
    private String context;
    private final HttpServer httpServer;

    public HttpServerConfiguration(final HttpServer httpServer, final int poolSize, final String context) {
        this.httpServer = httpServer;
        this.poolSize = poolSize;
        this.context = context;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        httpServer.setupPool(this);
    }

    public int getPoolSize() {
        return poolSize;
    }

    private boolean shutdown = false;

    public void shutdown() {
        shutdown = true;
    }

    public boolean canContinue() {
        return !shutdown;
    }

    public boolean getUseReverseLookup() {
        return useReverseLookup;
    }

    public void setUseReverseLookup(boolean b) {
        useReverseLookup = b;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

}
