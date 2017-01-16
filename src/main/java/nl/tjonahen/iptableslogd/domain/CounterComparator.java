package nl.tjonahen.iptableslogd.domain;

import java.io.Serializable;
import java.util.Comparator;

import nl.tjonahen.iptableslogd.domain.LogEntryStatistics.Counter;

public final class CounterComparator implements Comparator<Counter>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Counter o1, Counter o2) {
        if (o2.getCount() == o1.getCount()) {
            if (o2.getLastseen() == o1.getLastseen()) {
                return 0;
            } else if (o2.getLastseen() < o1.getLastseen()) {
                return -1;
            }
        } else if (o2.getCount() < o1.getCount()) {
            return -1;
        }
        return 1;
    }

}
