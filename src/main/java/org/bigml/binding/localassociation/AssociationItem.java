package org.bigml.binding.localassociation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.bigml.binding.utils.Utils;


/**
 * Object encapsulating an Association resource item as described in
 * https://bigml.com/developers/associations
 *
 */
public class AssociationItem {

    //private JSONObject item;
    private int index;
    private Boolean complement = false;
    private Integer complementId;
    private Long count;
    private String name;
    private String description;
    private String fieldId;
    private JSONObject fieldInfo;
    private Double binEnd;
    private Double binStart;


    /**
     * Constructor
     */
    public AssociationItem(final int index, final JSONObject item, final JSONObject fields) {
        super();

        //this.item = item;
        this.index = index;
        if (item.get("complement") != null) {
            this.complement = (Boolean) item.get("complement");
        }
        if (item.get("complement_id") != null) {
            this.complementId = (Integer) item.get("complement_id");
        }
        if (item.get("count") != null) {
            this.count = (Long) item.get("count");
        }
        this.name = (String) item.get("name");
        if (item.get("description") != null) {
            this.description = (String) item.get("description");
        }
        this.fieldId = (String) item.get("field_id");
        if (fields.get(fieldId) != null) {
            this.fieldInfo = (JSONObject) fields.get(fieldId);
        }
        if (item.get("bin_end") != null) {
            this.binEnd = (Double) item.get("bin_end");
        }
        if (item.get("bin_start") != null) {
            this.binStart = (Double) item.get("bin_start");
        }

    }


    public int getIndex() {
        return this.index;
    }

    public Boolean getComplement() {
        return this.complement;
    }

    public Integer getComplementId() {
        return this.complementId;
    }

    public Long getCount() {
        return this.count;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getFieldId() {
        return this.fieldId;
    }

    public Double getBinEnd() {
        return this.binEnd;
    }

    public Double getBinStart() {
        return this.binStart;
    }


    /**
     * Transforming the item to CSV formats
     */
    public List<Object> toCsv() {
        List<Object> output = new ArrayList<Object>();
        output.add(this.complement);
        output.add(this.complementId);
        output.add(this.count);
        output.add(this.description);
        output.add((String) this.fieldInfo.get("name"));
        output.add(this.name);
        output.add(this.binEnd);
        output.add(this.binStart);
        return output;
    }

    /**
     * Transforming the item relevant information to JSON
     */
    public Map<String, Object> toJson() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("complement", complement);
        output.put("complementId", complementId);
        output.put("count", count);
        output.put("name", name);
        output.put("description", description);
        output.put("fieldId", fieldId);
        output.put("binEnd", binEnd);
        output.put("binStart", binStart);
        return output;
    }

    /*
     * Returns the LISP flatline expression to filter this item
     */
    public String toLispRule() {
        StringBuilder flatline = new StringBuilder();

        if (this.name == null) {
            flatline.append(
                String.format("(missing? (f %s))", this.fieldId));
            description.toString();
        }

        String fieldType = (String) this.fieldInfo.get("optype");
        if (fieldType.equals("numeric")) {
            Double start = this.complement ? this.binEnd : this.binStart;
            Double end = this.complement ? this.binStart : this.binEnd;
            if (start == null && end == null) {
                if (start < end) {
                    flatline.append(
                        String.format("(and (< %s (f %s)) (<= (f %s) %s))",
                                      start, this.fieldId, this.fieldId, end));
                } else {
                    flatline.append(
                        String.format("(or (> (f %s) %s) (<= (f %s) %s))",
                                      this.fieldId, start, this.fieldId, end));
                }
            } else {
                if (start != null) {
                    flatline.append(
                        String.format("(> (f %s) %s)", this.fieldId, start));
                } else {
                    flatline.append(
                        String.format("(<= (f %s) %s)", this.fieldId, end));
                }
            }
        }
        if (fieldType.equals("categorical")) {
            String operator = this.complement ? "!=" : "=";
            flatline.append(
                String.format("(%s (f %s) %s)",
                              operator, this.fieldId, this.name));
        }
        if (fieldType.equals("text")) {
            String operator = this.complement ? "=" : ">";
            JSONObject options = (JSONObject)
                this.fieldInfo.get("term_analysis");
            String caseInsensitive = "false";
            if (options.get("case_sensitive") != null) {
                caseInsensitive = (Boolean) options.get("case_sensitive") ? "true" : "false";
            }
            String language = (String) options.get("language");
            language = (language==null ? "" : " "+language);
            flatline.append(
                String.format("(%s (occurrences (f %s) %s %s%s) 0)",
                              operator, this.fieldId, this.name,
                              caseInsensitive, language));
        }
        if (fieldType.equals("items")) {
            String operator = this.complement ? "!" : "";
            flatline.append(
                String.format("(%s (contains-items? %s %s))",
                              operator, this.fieldId, this.name));
        }

        return flatline.toString();
    }


    /*
     * Human-readable description of a item_dict
     */
    public String describe() {
        StringBuilder description = new StringBuilder();

        String fieldName = (String) this.fieldInfo.get("name");
        String fieldType = (String) this.fieldInfo.get("optype");

        if (this.name == null) {
            description.append(
                String.format("%s is %smissing",
                              fieldName,
                              this.complement ? "not ": ""));
            description.toString();
        }

        if (fieldType.equals("numeric")) {
            Double start = this.complement ? this.binEnd : this.binStart;
            Double end = this.complement ? this.binStart : this.binEnd;

            if (start == null && end == null) {
                if (start < end) {
                    description.append(
                        String.format("%s < %s <= %s", start, fieldName, end));
                } else {
                    description.append(
                        String.format("%s > %s or <= %s", fieldName, start, end));
                }
            } else {
                if (start != null) {
                    description.append(
                        String.format("%s > %s", fieldName, start));
                } else {
                    description.append(
                        String.format("%s <= %s", fieldName, end));
                }
            }
        }
        if (fieldType.equals("categorical")) {
            String operator = this.complement ? "!=" : "=";
            description.append(
                    String.format("%s %s %s", fieldName, operator, this.name));
        }
        if (fieldType.equals("text") || fieldType.equals("items")) {
            String operator = this.complement ? "excludes" : "includes";
            description.append(
                String.format("%s %s %s", fieldName, operator, this.name));
        }

        if (!fieldType.equals("numeric") && !fieldType.equals("categorical") &&
            !fieldType.equals("text") && !fieldType.equals("items")) {
            description.append(this.name);
        }

        return description.toString();
    }


    /*
     * Checks whether the value is in a range for numeric fields or matches a
     * category for categorical fields.
     */
    public boolean matches(Object value) {
        String fieldType = (String) this.fieldInfo.get("optype");

        if (value == null) {
            return this.name == null;
        }

        Boolean result = null;
        if (fieldType.equals("numeric") && (this.binEnd!=null || this.binStart!=null)) {
            if (this.binEnd!=null && this.binStart!=null) {
                result = (this.binStart <= (Double) value &&
                          (Double)value <= this.binEnd);
            } else {
                if (this.binEnd!=null) {
                    result = (Double) value <= this.binEnd;
                } else {
                    result = (Double) value >= this.binStart;
                }
            }
        }
        if (fieldType.equals("categorical")) {
            result = this.name.equals((String) value);
        }

        if (fieldType.equals("text")) {
            // for text fields, the item.name or the related term_forms
            // should be in the considered value
            JSONObject allForms = (JSONObject) Utils.getJSONObject(
                (JSONObject) this.fieldInfo.get(this.fieldId),
                    "summary.term_forms", new JSONObject());
            JSONArray termForms = (JSONArray) allForms.get(this.name);
            termForms = (termForms == null ? new JSONArray() : termForms);

            List<String> terms = new ArrayList<String>();
            terms.add(this.name);
            terms.addAll(termForms);

            JSONObject options = (JSONObject) Utils.getJSONObject(
                (JSONObject) this.fieldInfo.get(this.fieldId),
                    "term_analysis");
            result = Utils.termMatches((String) value, terms, options) > 0;
        }

        if (fieldType.equals("items")) {
            // for item fields, the item.name should be in the
            // considered value surrounded by separators or regexp
            JSONObject options = (JSONObject)
                this.fieldInfo.get("item_analysis");
            result = itemsMatches((String) value, this.name, options) > 0;
        }

        if (result!=null && this.complement) {
            result = !result;
        }

        return result;
    }


    /**
     * Counts the number of occurences of the item in the text
     *
     * The matching considers the separator or the separating regular expression.
     */
    private int itemsMatches(String text, String item, JSONObject options) {
        String separator = " ";
        if (options.get("separator") != null) {
            separator = (String) options.get("separator");
        }
        String regexp = separator;
        if (options.get("separator_regexp") != null) {
            regexp = (String) options.get("separator_regexp");
        }
        return countItemsMatches(text, item, regexp);
    }

    /**
     * Counts the number of occurences of the item in the text
     */
    private int countItemsMatches(String text, String item, String regexp) {
        String expression = String.format("(^|%s)%s($|%s)", regexp, item, regexp);
        Pattern pattern = Pattern.compile(expression,
            (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        Matcher matcher = pattern.matcher(text);
        return (matcher.find() ? matcher.groupCount() : 0);
    }

}