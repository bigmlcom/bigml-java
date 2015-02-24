package org.bigml.binding.localmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigml.binding.Constants;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A predicate to be evaluated in a tree's node.
 * 
 */
public class Predicate {

    public static Pattern FULL_TERM_PATTERN_RE = Pattern.compile("^.+\\b.+$", Pattern.UNICODE_CASE);
    public static String TM_TOKENS = "tokens_only";
    public static String TM_FULL_TERM = "full_terms_only";
    public static String TM_ALL = "all";

    private String opType;
    private String operator;
    private String field;
    private String fieldName;
    private Object value;
    private String term;
    private boolean missing = false;

    /**
     * Constructor
     */
    public Predicate(final String opType, final String operator,
            final String field, final String fieldName, final Object value, final String term) {
        super();

        this.opType = opType;
        this.operator = operator;
        this.field = field;
        this.fieldName = fieldName;
        this.value = value;
        this.term = term;

        if( this.operator.endsWith("*") ) {
            this.operator = this.operator.substring(0, this.operator.length() - 1);
            this.missing = true;
        }
    }

    public String getOpType() {
        return opType;
    }

    public String getOperator() {
        return operator;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public String getTerm() {
        return term;
    }

    public boolean isMissing() {
        return missing;
    }

    /**
     * Builds rule string from a predicate
     * 
     */
    public String toRule(final JSONObject fields) {

        String name = ((JSONObject) fields.get(this.field)).get("name").toString();
        boolean fullTerm = isFullTerm(fields);
        String relationMissing = "";
        if( missing ) {
            relationMissing += " || missing";
        }

        if( term != null && term.length() > 0 ) {
            StringBuilder ruleStr = new StringBuilder();
            if( ( operator.equals("<") && ((Number) value).intValue() <= 1) ||
                    ( operator.equals("<=") && ((Number) value).intValue() == 0) ) {
                ruleStr.append("!").append(name).append((fullTerm ? ".equals(\"" : ".contains(\""))
                    .append(term).append("\")");
            } else {
                if( fullTerm) {
                    ruleStr.append(name).append(".equals(\"").append(term).append("\")");
                } else {
                    if( !operator.equals(">") || ((Number) value).intValue() !=  0) {
                        ruleStr.append("containsCount(").append(name).append(", \"").append(term).append("\") ").append(operator).append(" ").append(value.toString());
                    } else {
                        ruleStr.append(name).append(".contains(\"").append(term).append("\")");
                    }
                }
            }

            return ruleStr.append(relationMissing).toString();
        }


        return String.format("%s %s %s%s",
                name, this.operator, (this.value != null ? this.value.toString() : "null"), relationMissing);
    }

    /**
     * Returns a boolean showing if a term is considered as a full_term
     *
     * @param fields the fields definition of the model
     * @return true if the predicate field is full term
     */
    protected boolean isFullTerm(JSONObject fields) {
        if( term != null &&  term.length() > 0 ) {
            String tokenMode = (String) Utils.getJSONObject((JSONObject) fields.get(field),
                    "term_analysis.token_mode");
            if( Predicate.TM_FULL_TERM.equals(tokenMode) ) {
                return true;
            }

            if( Predicate.TM_ALL.equals(tokenMode) ) {
                return Predicate.FULL_TERM_PATTERN_RE.matcher(term).find();
            }
        }

        return false;
    }


    /**
     * Applies the operators defined in the predicate as strings to
     * the provided input data
     *
     * @return if the operator applies or not
     */
    public boolean apply(JSONObject inputData, JSONObject fields) {
        if( !inputData.containsKey(fieldName) ) {
            return missing || ("=".equals(operator) && value == null);
        } else if("!=".equals(operator) && value == null) {
            return true;
        }

        if( term != null ) {
            JSONObject allForms = (JSONObject) Utils.getJSONObject((JSONObject) fields.get(field),
                    "summary.term_forms");
            allForms = (allForms == null ? new JSONObject() : allForms);
            JSONArray termForms = (JSONArray) allForms.get(term);
            termForms = (termForms == null ? new JSONArray() : termForms);

            List<String> terms = new ArrayList<String>();
            terms.add(term);
            terms.addAll(termForms);

            JSONObject options = (JSONObject) Utils.getJSONObject((JSONObject) fields.get(field),
                    "term_analysis");

            return applyOperator(termMatches(inputData.get(fieldName).toString(), terms, options));
        }

        return applyOperator(inputData.get(fieldName));
    }

    protected boolean applyOperator(Object inputValue) {
        if (operator.equals(Constants.OPERATOR_EQ)
                && inputValue.equals(value)) {
            return true;
        }

        if ((operator.equals(Constants.OPERATOR_NE) || operator
                .equals(Constants.OPERATOR_NE2))
                && !inputValue.equals(value)) {
            return true;
        }
        if (operator.equals(Constants.OPERATOR_LT) &&
                ((Number) inputValue).doubleValue() < ((Number) value)
                    .doubleValue()) {
            return true;
        }
        if (operator.equals(Constants.OPERATOR_LE) &&
                ((Number) inputValue).doubleValue() <= ((Number) value)
                    .doubleValue()) {
            return true;
        }
        if (operator.equals(Constants.OPERATOR_GE) &&
                ((Number) inputValue).doubleValue() >= ((Number) value)
                    .doubleValue()) {
            return true;
        }
        if (operator.equals(Constants.OPERATOR_GT) &&
                ((Number) inputValue).doubleValue() > ((Number) value)
                    .doubleValue()) {
            return true;
        }
        if (operator.equals(Constants.OPERATOR_IN) &&
                (inputValue.toString().contains(value.toString())) ) {
            return true;
        }

        return false;
    }


    private int termMatches(String text, List<String> formsList, JSONObject options) {

        // Checking Full Terms Only
        String tokenMode = (String) Utils.getJSONObject(options, "token_mode", TM_TOKENS);
        Boolean caseSensitive = (Boolean) Utils.getJSONObject(options, "case_sensitive", Boolean.TRUE);

        String firstTerm = formsList.get(0);

        if (tokenMode.equals(TM_FULL_TERM)) {
            return fullTermMatch(text, firstTerm, caseSensitive);
        }

        // In token_mode='all' we will match full terms using equals and
        // tokens using contains
        if ( TM_ALL.equals(tokenMode) && formsList.size() == 1 ) {
            if( FULL_TERM_PATTERN_RE.matcher(firstTerm).find() ) {
                return fullTermMatch(text, firstTerm, caseSensitive);
            }
        }

        return termMatchesTokens(text, formsList, caseSensitive);
    }

    /**
     * Counts the match for full terms according to the case_sensitive option
     *
     * @param text
     * @param fullTerm
     * @param caseSensitive
     * @return
     */
    private int fullTermMatch(String text, String fullTerm, boolean caseSensitive) {
        return (caseSensitive ? (text.equals(fullTerm) ? 1 : 0) : (text.equalsIgnoreCase(fullTerm) ? 1 : 0));
    }

    /**
     * Counts the number of occurences of the words in forms_list in the text
     *
     * @param text
     * @param formsList
     * @param caseSensitive
     * @return
     */
    private int termMatchesTokens(String text, List<String> formsList, boolean caseSensitive) {
        String expression = String.format("(\\b|_)%s(\\b|_)", join(formsList, "(\\b|_)|(\\b|_)"));
        Pattern pattern = Pattern.compile(expression, (caseSensitive ? Pattern.UNICODE_CASE :
                (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));
        Matcher matcher = pattern.matcher(text);
        return (matcher.find() ? matcher.groupCount() : 0);
    }


    /**
     * Joins all the string items in the list using the conjunction text
     *
     * @param list
     * @param conjunction
     * @return
     */
    private String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if(first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }
}
