package org.bigml.binding.localanomaly;

import org.bigml.binding.localmodel.Predicate;
import org.json.simple.JSONObject;

/**
 * A Predicate that always is True
 */
public class TruePredicate extends Predicate {

    public TruePredicate() {
        super(null, null, null, null, null);
    }

    protected TruePredicate(String opType, String operator, String field, Object value, String term) {
        super(opType, operator, field, value, term);
    }

    @Override
    public String toRule(JSONObject fields) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isFullTerm(JSONObject fields) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean apply(JSONObject inputData, JSONObject fields) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean applyOperator(Object inputValue) {
        throw new UnsupportedOperationException();
    }
}
