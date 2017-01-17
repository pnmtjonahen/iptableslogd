package nl.tjonahen.iptableslogd.jmx;

import nl.tjonahen.iptableslogd.HttpServer;

/**
 * HttpServerConfiguration MBean.
 *
 */
public final class IpTablesLogdConfiguration implements IpTablesLogdConfigurationMBean {

    private int poolSize;
    private boolean useReverseLookup = true;
    private String context;
    private final HttpServer httpServer;

    public IpTablesLogdConfiguration(final HttpServer httpServer, final int poolSize, final String context) {
        this.httpServer = httpServer;
        this.poolSize = poolSize;
        this.context = context;
    }

    @Override
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        httpServer.setupPool(this);
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }

    private boolean shutdown = false;

    @Override
    public void shutdown() {
        shutdown = true;
    }

    public boolean canContinue() {
        return !shutdown;
    }

    @Override
    public boolean getUseReverseLookup() {
        return useReverseLookup;
    }

    @Override
    public void setUseReverseLookup(boolean b) {
        useReverseLookup = b;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void setContext(String context) {
        this.context = context;
    }

}
