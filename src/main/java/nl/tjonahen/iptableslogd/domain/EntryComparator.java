/*
 * Copyright (C) 2017 Philippe Tjon - A - Hen philippe@tjonahen.nl
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

package nl.tjonahen.iptableslogd.domain;

import java.util.Comparator;
import java.util.Map.Entry;
import nl.tjonahen.iptableslogd.domain.LogEntryStatistics.Counter;

/**
 *
 * @author Philippe Tjon - A - Hen
 */
public class EntryComparator implements Comparator<Entry<String, Counter>>{

    private final CounterComparator comparator = new CounterComparator();
    
    @Override
    public int compare(Entry<String, Counter> o1, Entry<String, Counter> o2) {
        return comparator.compare(o1.getValue(), o2.getValue());
    }

}
