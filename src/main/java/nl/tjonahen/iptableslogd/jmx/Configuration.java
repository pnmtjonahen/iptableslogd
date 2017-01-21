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
package nl.tjonahen.iptableslogd.jmx;

import java.util.Observable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
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
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    
    @Inject
    private MBeanServer platformMBeanServer;
    
    private ObjectName objectName = null;

    @PostConstruct
    public void setup() {
        LOGGER.info("Initializing JMX.");
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
        shutdown();
        try {
            platformMBeanServer.unregisterMBean(this.objectName);
        } catch (InstanceNotFoundException | MBeanRegistrationException e) {
            throw new IllegalStateException("Unable to unregistration ConfigMBean ", e);
        }
    }

    private int poolSize = 5;
    private boolean useReverseLookup = true;
    private boolean shutdown = false;
    
    private int port;
    private String ulog;

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getUlog() {
        return ulog;
    }

    @Override
    public void setUlog(String ulog) {
        this.ulog = ulog;
        this.notifyObservers();
    }

    
}
