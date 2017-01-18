package nl.tjonahen.iptableslogd.collection;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * FixedSizeList but it will count the entries if they already exists.
 *
 * @author Philippe Tjon-A-Hen
 *
 * @param <T>
 */
public final class AggregatingFixedSizeList<T> extends FixedSizeList<T> {

    /**
     *
     */
    private static final long serialVersionUID = -3749312424601064397L;
    private final Map<String, Integer> counter = new TreeMap<>();
    private final Function<T, String> identityExtractor;

    /**
     *
     * @param size max size of this aggregator.
     * @param identityExtractor Functor to extract the identiy of the aggregate.
     */
    public AggregatingFixedSizeList(final int size, final Function<T, String> identityExtractor) {
        super(size);
        this.identityExtractor = identityExtractor;
    }

    @Override
    public synchronized boolean add(T entry) {
        if (counter.containsKey(identityExtractor.apply(entry))) {
            int count = counter.remove(identityExtractor.apply(entry));
            counter.put(identityExtractor.apply(entry), ++count);
        } else {
            super.add(entry);
            counter.put(identityExtractor.apply(entry), 1);
            if (super.size() > getMaxSize()) {
                T removed = super.remove(0);
                counter.remove(identityExtractor.apply(removed));
            }
        }
        return true;
    }

    public synchronized int getAggregateCount(T entry) {
        if (counter.containsKey(identityExtractor.apply(entry))) {
            return counter.get(identityExtractor.apply(entry));
        }
        return 1;
    }

}
