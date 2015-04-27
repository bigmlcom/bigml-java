package org.bigml.binding.localmodel;

import java.util.ArrayList;
import java.util.Collection;
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
//    private String fieldName;
    private Object value;
    private String term;
    private boolean missing = false;

    public enum RuleLanguage {
        PSEUDOCODE, JAVA, PYTHON, TABLEAU
    }

    /**
     * Constructor
     */
    public Predicate(final String opType, final String operator,
            final String field, final Object value, final String term) {
        super();

        this.opType = opType;
        this.operator = operator;
        this.field = field;
//        this.fieldName = fieldName;
        this.value = value;
        this.term = term;

        if( this.operator != null && this.operator.endsWith("*") ) {
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
     * Builds rule string from a predicate using the
     * fields NAME property as the label for the operand
     *
     * @param fields a map that contains all the information
     *               associated to each field ind the model
     */
    public String toRule(final JSONObject fields) {
        return toRule(RuleLanguage.PSEUDOCODE, fields, "name");
    }

    /**
     * Builds rule string from a predicate
     *
     * @param fields a map that contains all the information
     *               associated to each field ind the model
     * @param label which attribute of the field to use as identifier
     *              of the field associated to this predicate
     */
    public String toRule(final JSONObject fields, String label) {
        return toRule(RuleLanguage.PSEUDOCODE, fields, label);
    }

    /**
     * Builds rule string from a predicate
     *
     * @param language the final language we need to generate the rule for
     * @param fields a map that contains all the information
     *               associated to each field ind the model
     * @param label which attribute of the field to use as identifier
     *              of the field associated to this predicate
     */
    public String toRule(final RuleLanguage language, final JSONObject fields, String label) {

        String operandLabel = ((JSONObject) fields.get(this.field)).get(label).toString();
        boolean fullTerm = isFullTerm(fields);
        String relationMissing = "";
        if( missing ) {
            switch (language) {
                case PSEUDOCODE:
                    relationMissing += " or missing";
                    break;

                case JAVA:
                    relationMissing += String.format(" || %s == null", operandLabel);
                    break;

                case PYTHON:
                    relationMissing += String.format(" or %s is None", operandLabel);
                    break;
            }

        }

        if( term != null && term.length() > 0 ) {
            StringBuilder ruleStr = new StringBuilder();
            if( ( Constants.OPERATOR_LT.equals(this.operator) && ((Number) value).intValue() <= 1) ||
                    ( Constants.OPERATOR_LE.equals(this.operator) && ((Number) value).intValue() == 0) ) {
                switch (language) {
                    case JAVA:
                        ruleStr.append("!").append(operandLabel).append((fullTerm ? ".equals(\"" : ".contains(\""))
                                .append(term).append("\")");
                    break;

                    case PSEUDOCODE:
                        ruleStr.append(String.format("%s %s %s", operandLabel, (fullTerm ? "is not equal to" : "does not contains"), term));
                    break;

                    case PYTHON:
                        ruleStr.append(String.format("'%s' %s %s", term, (fullTerm ? "!=" : "not in"), operandLabel));
                    break;
                }
            } else {
                if( fullTerm) {
                    if( language == RuleLanguage.JAVA ) {
                        ruleStr.append(operandLabel).append(".equals(\"").append(term).append("\")");
                    } else if( language == RuleLanguage.PSEUDOCODE ){
                        ruleStr.append(operandLabel).append(" is equal to ").append(term);
                    } else {
                        ruleStr.append(operandLabel).append(" == '").append(term).append("'");
                    }
                } else {
//                    '<=': 'no more than %s %s',
//                            '>=': '%s %s at most',
//                            '>': 'more than %s %s',
//                            '<': 'less than %s %s'

                    if( !Constants.OPERATOR_GT.equals(this.operator) || ((Number) value).intValue() !=  0) {
                        switch (language) {
                            case PSEUDOCODE:
                                if( Constants.OPERATOR_LE.equals(this.operator) ) {
                                    ruleStr.append(String.format("%s is equal to %s no more than %s %s",
                                            operandLabel, this.term,
                                            this.value, Utils.plural("time", ((Number) this.value).intValue())));
                                } else if( Constants.OPERATOR_GE.equals(this.operator) ) {
                                    ruleStr.append(String.format("\"%s is equal to %s %s %s at most",
                                            operandLabel, this.term,
                                            this.value, Utils.plural("time", ((Number) this.value).intValue())));
                                } else if( Constants.OPERATOR_GT.equals(this.operator) ) {
                                    ruleStr.append(String.format("\"%s is equal to %s more than %s %s",
                                            operandLabel, this.term,
                                            this.value, Utils.plural("time", ((Number) this.value).intValue())));
                                } else { // LT
                                    ruleStr.append(String.format("\"%s is equal to %s less than %s %s",
                                            operandLabel, this.term,
                                            this.value, Utils.plural("time", ((Number) this.value).intValue())));
                                }
                                break;

                            case PYTHON:
                                ruleStr.append(String.format("%s.count('%s') %s %s",
                                        operandLabel, this.term, operator, this.value));
                                break;

                            case JAVA:
                                ruleStr.append(String.format("%s.count(\"%s\") %s %s",
                                        operandLabel, this.term, operator, this.value));
                                break;
                        }
                    } else {
                        switch (language) {
                            case PSEUDOCODE:
                                ruleStr.append(String.format("%s contains %s",
                                        operandLabel, this.term));
                                break;

                            case JAVA:
                                ruleStr.append(String.format("%s.contains(\"%s\")",
                                        operandLabel, this.term));
                                break;

                            case PYTHON:
                                ruleStr.append(String.format("%s in %s",
                                        this.term, operandLabel));
                                break;
                        }
                    }
                }
            }

            return ruleStr.append(relationMissing).toString();
        }

        String operator = this.operator;

        // Use "is" or "is not" if the language is Pseudo or if the value
        // is Null and the language is Python (xxx is None or xxx is not None)
        if( (this.value == null &&
                (language == RuleLanguage.PSEUDOCODE || language == RuleLanguage.PYTHON) ) ) {
            if( Constants.OPERATOR_NE.equals(this.operator) ) {
                operator = "is not";
            } else if( Constants.OPERATOR_EQ.equals(this.operator) ) {
                operator = "is";
            }
        }
        // Special treatment of String in Java when value is not null
        else if( language == RuleLanguage.JAVA && this.value != null &&
                !Constants.OPTYPE_NUMERIC.equals(opType)) {

            if( Constants.OPERATOR_NE.equals(this.operator) ) {
                return String.format("!\"%s\".equals(%s)%s",
                        this.value.toString(),
                        operandLabel, relationMissing);
            } else if( Constants.OPERATOR_EQ.equals(this.operator) ) {
                return String.format("\"%s\".equals(%s)%s",
                        this.value.toString(),
                        operandLabel, relationMissing);
            }
        } else if( language != RuleLanguage.PSEUDOCODE ) {
            // Use the correct Java and Python operators for EQ and NE
            if (Constants.OPERATOR_NE.equals(this.operator)) {
                operator = "!=";
            } else if (Constants.OPERATOR_EQ.equals(this.operator)) {
                operator = "==";
            }
        }

        String notMissingCondition = null;
        if( language == RuleLanguage.JAVA && this.value != null) {
            notMissingCondition = String.format("%s != null && ",
                    operandLabel);
        }

        String nullValue = "None";
        switch (language) {
            case JAVA:
                nullValue = "null";
                break;
        }

        if( notMissingCondition != null ) {
            return String.format("(%s%s %s %s)%s",
                    notMissingCondition, operandLabel, operator,
                    (this.value != null ? this.value.toString() : nullValue), relationMissing);
        } else {
            return String.format("%s %s %s%s",
                    operandLabel, operator,
                    (this.value != null ? this.value.toString() : nullValue),
                    relationMissing);
        }
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
        //for missing operators
        if( inputData.get(field) == null ) {
            return missing || (operator.equals(Constants.OPERATOR_EQ) && value == null);
        } else if((operator.equals(Constants.OPERATOR_NE) && value == null)) {
            return true;
        }


        if( term != null ) {
            JSONObject allForms = (JSONObject) Utils.getJSONObject((JSONObject) fields.get(field),
                    "summary.term_forms", new JSONObject());
            JSONArray termForms = (JSONArray) allForms.get(term);
            termForms = (termForms == null ? new JSONArray() : termForms);

            List<String> terms = new ArrayList<String>();
            terms.add(term);
            terms.addAll(termForms);

            JSONObject options = (JSONObject) Utils.getJSONObject((JSONObject) fields.get(field),
                    "term_analysis");

            return applyOperator(termMatches(inputData.get(field).toString(), terms, options));
        }

        return applyOperator(inputData.get(field));
    }

    protected boolean applyOperator(Object inputValue) {
        if (operator.equals(Constants.OPERATOR_EQ) ) {
            if( inputValue instanceof Number ) {
                return ((Number) inputValue).doubleValue() == ((Number) value)
                        .doubleValue();
            } else {
                return inputValue.toString().equals(value);
            }
        }

        if ((operator.equals(Constants.OPERATOR_NE) || operator
                .equals(Constants.OPERATOR_NE2))) {
            if( inputValue instanceof Number ) {
                return ((Number) inputValue).doubleValue() != ((Number) value)
                        .doubleValue();
            } else {
                return !inputValue.toString().equals(value);
            }
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
        if (operator.equals(Constants.OPERATOR_IN) )  {

            if( !(inputValue instanceof Collection) ) {
                List newInputValue = new ArrayList();
                newInputValue.add(inputValue);
                inputValue = newInputValue;
            }

            if( value instanceof Collection ) {
                return ((Collection) inputValue).containsAll((Collection) value);
            } else {
                return ((Collection) inputValue).contains(value);
            }
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
