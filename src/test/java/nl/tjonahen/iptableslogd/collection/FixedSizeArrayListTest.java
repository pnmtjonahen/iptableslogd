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
