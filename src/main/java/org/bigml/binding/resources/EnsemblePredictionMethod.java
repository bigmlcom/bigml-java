package org.bigml.binding.resources;

/**
 * A numeric key to the following combination methods in classifications/regressions:
 *   PLURALITY_CODE: 0 - majority vote (plurality)/ average
 *   CONFIDENCE_CODE: 1 - confidence weighted majority vote / error weighted
 *   PROBABILITY_CODE: 2 - probability weighted majority vote / average
 *   THRESHOLD_CODE: 3 - threshold filtered vote / doesn't apply
 */
public enum EnsemblePredictionMethod {
    PLURALITY(0), CONFIDENCE(1), PROBABILITY(2), THRESHOLD(3);


    private final int code;

    private EnsemblePredictionMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
