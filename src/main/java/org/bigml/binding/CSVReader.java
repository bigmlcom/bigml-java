/*
  Utility class for reading CSV input files and parsing them as rows
  of a dataset.
 */

package org.bigml.binding;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import org.bigml.binding.utils.Utils;

/**
 * Reading input values from a CSV.
 */
public class CSVReader implements Iterator<Map<String, Object>> {

    public CSVReader(String filename, JSONObject fields) throws IOException,
            FileNotFoundException {
        this(filename, fields, null);
    }

    public CSVReader(String filename, JSONObject fields, String[] cols)
            throws IOException, FileNotFoundException {
        this(filename, fields, cols, CsvPreference.STANDARD_PREFERENCE);
    }

    public CSVReader(String filename, JSONObject fields, String[] names,
            CsvPreference prefs) throws IOException, FileNotFoundException {
        try {
            this.reader = new CsvMapReader(new FileReader(filename), prefs);

            if (names == null) {
                this.header = reader.getHeader(true);
            } else {
                this.header = names;
            }
            createProcessors(fields);
            readNext();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public void close() throws IOException {
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public Map<String, Object> next() {
        if (this.nextLine == null)
            return null;
        Map<String, Object> result = nextLine;
        readNext();
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
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
            this.processors[i] = createProcessor(field);
        }
    }

    private static CellProcessor createProcessor(final JSONObject field) {
        String datatype = (String) field.get("datatype");
        if (datatype == null) {
            String msg = "Invalid field: " + field;
            throw new IllegalArgumentException(msg);
        }
        if (datatype.equals("int8") || datatype.equals("int16")
                || datatype.equals("int32") || datatype.equals("int64")
                || datatype.equals("integer")) {
            return new Optional(new ParseLong());
        } else if (datatype.equals("float") || datatype.equals("double")) {
            return new Optional(new ParseDouble());
        }

        return new Optional();
    }

    private Map<String, Object> readLine() throws IOException {
        try {
            return this.reader.read(this.header, this.processors);
        } catch (SuperCsvCellProcessorException e) {
            return new HashMap<String, Object>();
        }
    }

    private void readNext() {
        this.nextLine = null;
        Map<String, Object> line = null;
        try {
            while ((line = readLine()) != null) {
                if (line.size() == this.header.length) {
                    this.nextLine = line;
                    return;
                }
            }
            if (line == null)
                this.reader.close();
        } catch (IOException e) {
            this.nextLine = null;
        }
    }

    private ICsvMapReader reader;
    private CellProcessor[] processors;
    private String[] header;
    private Map<String, Object> nextLine;
}
