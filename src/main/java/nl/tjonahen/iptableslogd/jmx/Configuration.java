package nl.tjonahen.iptableslogd.jmx;

import java.util.Observable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * HttpServerConfiguration MBean.
 *
 */
@Singleton
public final class Configuration extends Observable implements ConfigurationMBean {

    @Inject
    private MBeanServer platformMBeanServer;
    
    private ObjectName objectName = null;

    @PostConstruct
    public void setup() {
        try {
            objectName = new ObjectName("nl.tjonahen.iptableslogd.Config:type=configuration");
            // Register the HttpServer configuration MBean
            platformMBeanServer.registerMBean(this, objectName);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | MalformedObjectNameException | NotCompliantMBeanException e) {
            throw new IllegalStateException("Unable to register ConfigMBean ", e);
        }
    }

    @PreDestroy
    public void tearDown() {
        try {
            platformMBeanServer.unregisterMBean(this.objectName);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to unregistration ConfigMBean ", e);
        }
    }

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
