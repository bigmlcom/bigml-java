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
            Map<Object, Object> r = reader.next();
            assertEquals(r.toString(), r.size(), 5);
            n++;
        }
        assertEquals(150, n);
    }
}
