package nl.tjonahen.iptableslogd.domain;

import java.io.Serializable;
import java.util.Comparator;

import nl.tjonahen.iptableslogd.domain.LogEntryStatistics.Counter;

public final class CounterComparator implements Comparator<Counter>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public int compare(Counter o1, Counter o2) {
        return compareTo(o2, o1);
    }

    private int compareTo(Counter ths, Counter other) {
        if (ths.getCount() == other.getCount()) {
            if (ths.getLastseen() == other.getLastseen()) {
                return 0;
            } else if (ths.getLastseen() < other.getLastseen()) {
                return -1;
            }
        } else if (ths.getCount() < other.getCount()) {
            return -1;
        }
        return 1;
    }

}
