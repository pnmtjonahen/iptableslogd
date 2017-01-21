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
