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

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FixedSizeArrayListTest {

    @Test
    public void testTrimToSize() {
        ArrayList<String> list = new FixedSizeList<>(10);
        list.add("String1");
        list.add("String2");
        list.add("String3");
        assertEquals(3, list.size());
        list.trimToSize();
        assertEquals(3, list.size());
        list.add("String4");
        assertEquals(3, list.size());

    }

    @Test
    public void testAddT() {
        List<String> list = new FixedSizeList<>(2);
        assertEquals(0, list.size());
        list.add("String1");
        assertEquals(1, list.size());
        list.add("String2");
        assertEquals(2, list.size());
        list.add("String3");
        assertEquals(2, list.size());
    }

}
