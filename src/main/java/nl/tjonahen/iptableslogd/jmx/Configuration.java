package nl.tjonahen.iptableslogd.jmx;

import java.util.Observable;

/**
 * HttpServerConfiguration MBean.
 *
 */
public final class Configuration extends Observable implements ConfigurationMBean {

    private int poolSize = 5;
    private boolean useReverseLookup = true;
    private boolean shutdown = false;
    

    @Override
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        this.notifyObservers();
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }


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

}
