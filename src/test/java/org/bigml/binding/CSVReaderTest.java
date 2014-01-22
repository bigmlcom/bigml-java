package org.bigml.binding;

import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

public class CSVReaderTest extends TestCase {

    public CSVReaderTest(String testName) {
        super(testName);
    }

    private static JSONObject irisFields;

    protected void setUp() throws Exception {
        super.setUp();
        if (irisFields == null) {
            String fn =
                CSVReaderTest.class.getResource("/iris-fields.json").getFile();
            FileReader r = new FileReader(new File(fn));
            irisFields = (JSONObject) JSONValue.parse(r);
            assertNotNull(irisFields);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNames() throws Exception {
        String iris = this.getClass().getResource("/iris.csv").getFile();
        CSVReader reader = new CSVReader(iris, irisFields);
        assertTrue(reader.hasNext());
        int n = 0;
        while (reader.hasNext()) {
            Map<String, Object> r = reader.next();
            assertEquals(r.toString(), 5, r.size());
            n++;
        }
        assertEquals(150, n);
    }

    public void testIds() throws Exception {
        String iris = this.getClass().getResource("/iris.csv").getFile();
        String ids[] = {"000000", "000001", "000002", "000003", "000004"};
        CSVReader reader = new CSVReader(iris, irisFields, ids);
        int n = 0;
        while (reader.hasNext()) {
            Map<String, Object> r = reader.next();
            assertEquals(r.toString(), 5, r.size());
            for (int i = 0; i < ids.length; i++) {
                Object v = r.get(ids[i]);
                assertNotNull(ids[i], v);
                if (i < 4) {
                    assertEquals("java.lang.Double", v.getClass().getName());
                } else {
                    assertEquals("java.lang.String", v.getClass().getName());
                }
            }
            n++;
        }
        assertEquals(150, n);
    }
}
