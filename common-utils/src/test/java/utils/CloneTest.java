package utils;

import org.junit.Test;
import utils.StaticUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CloneTest {

    @Test
    public void testClone() {
//     Java 11
//        Map<Integer, String> toClone = Map.of(1, "Akeu",
//                2, "Coucou",
//                3, "Larigou");
        // Java 8
        Map<Integer, String> toClone = new HashMap<>();
        toClone.put(1, "Akeu");
        toClone.put(2, "Coucou");
        toClone.put(3, "Larigou");
        try {
            Object theClone = StaticUtil.deepCopy(toClone);
            assertTrue("Went well", ((Map<Integer, String>)theClone).size() == 3);
            assertTrue("First Element", ((Map<Integer, String>)theClone).get(1).equals("Akeu"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
