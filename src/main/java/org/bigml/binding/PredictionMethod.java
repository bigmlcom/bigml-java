package org.bigml.binding;

/**
 * A numeric key to the following combination methods in classifications/regressions:
 *   PLURALITY_CODE: 0 - majority vote (plurality)/ average
 *   CONFIDENCE_CODE: 1 - confidence weighted majority vote / error weighted
 *   PROBABILITY_CODE: 2 - probability weighted majority vote / average
 *   THRESHOLD_CODE: 3 - threshold filtered vote / doesn't apply
 */
public enum PredictionMethod {
    PLURALITY(0), CONFIDENCE(1), PROBABILITY(2), THRESHOLD(3);


    private final int code;

    private PredictionMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PredictionMethod valueOf(int code) {
        if( code == 0 ) {
            return PLURALITY;
        } else if( code == 1 ) {
            return CONFIDENCE;
        } else if( code == 2 ) {
            return PROBABILITY;
        } else if( code == 3 ) {
            return THRESHOLD;
        }

        return null;
    }
}
