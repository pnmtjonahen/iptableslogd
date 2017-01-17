package nl.tjonahen.iptableslogd.jmx;

public interface IpTablesLogdConfigurationMBean {

    void setPoolSize(int poolSize);

    int getPoolSize();

    void shutdown();

    void setUseReverseLookup(boolean b);

    boolean getUseReverseLookup();

    void setContext(String context);

    String getContext();
}
