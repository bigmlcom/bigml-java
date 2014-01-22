/*
  Utility class for reading CSV input files and parsing them as rows
  of a dataset.
*/

package org.bigml.binding;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;

import org.bigml.binding.utils.Utils;

/**
 * Reading input values from a CSV.
 */
public class CSVReader {

    public CSVReader(String filename, JSONObject fields)
        throws IOException, FileNotFoundException {
        this(filename, fields, null);
    }

    public CSVReader(String filename, JSONObject fields, String[] cols)
        throws IOException, FileNotFoundException {
        this(filename, fields, cols, CsvPreference.STANDARD_PREFERENCE);
    }

    public CSVReader(String filename, JSONObject fields,
                     String[] names, CsvPreference prefs)
        throws IOException, FileNotFoundException {
        try {
            this.listreader =
                new CsvListReader(new FileReader(filename), prefs);

            if (names == null) {
                this.header = listreader.getHeader(true);
            } else {
                this.header = names;
            }
            createProcessors(fields);
            readNext();
        } catch (IOException e) {
            if( this.listreader != null ) {
                this.listreader.close();
            }
            throw e;
        }
    }

    public Map<Object, Object> next() {
        if (this.nextLine == null) return null;
        Map<Object, Object> result = new HashMap<Object, Object>();
        for (int i = 0; i < this.header.length; i++)
            result.put(this.header[i], this.nextLine.get(i));
        readNext();
        return result;
    }

    public boolean hasNext() {
        return this.nextLine != null;
    }

    private void createProcessors(final JSONObject fields) {
        this.processors = new CellProcessor[header.length];
        JSONObject ifields = Utils.invertDictionary(fields);
        for (int i = 0; i < this.header.length; i++) {
            String col = this.header[i];
            JSONObject field = (JSONObject) fields.get(col);
            if (field == null) {
                field = (JSONObject) ifields.get(col);
            }
            if (field == null) {
                String msg = "Cannot find field name or id " + col;
                throw new IllegalArgumentException(msg);
            }
            processors[i] = createProcessor(field);
        }
    }

    private static CellProcessor createProcessor(final JSONObject field) {
        String datatype = (String) field.get("datatype");
        CellProcessor p = null;
        if (datatype == null) {
            String msg = "Invalid field: " + field;
            throw new IllegalArgumentException(msg);
        }
        if (datatype == "int8" || datatype == "int16"
            || datatype == "int32" || datatype == "int64"
            || datatype == "integer") {
            p = new ParseLong();
        } else if (datatype == "float" || datatype == "double") {
            p = new ParseDouble();
        }

        return p;
    }

    private void readNext() {
        this.nextLine = null;
        List<Object> line = null;
        try {
            while ((line = this.listreader.read(this.processors)) != null) {
                if (line.size() == this.header.length) {
                    this.nextLine = line;
                    return;
                }
            }
            if (line == null) this.listreader.close();
        } catch (IOException e) {
            this.nextLine = null;
        }
    }

    private ICsvListReader listreader;
    private CellProcessor[] processors;
    private String[] header;
    private List<Object> nextLine;
}
