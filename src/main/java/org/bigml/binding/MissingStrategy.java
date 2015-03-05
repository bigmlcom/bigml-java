package org.bigml.binding;

/**
 * There are two possible strategies to predict when the value for the
 * splitting field is missing:
 *
 *      0 - LAST_PREDICTION: the last issued prediction is returned.
 *      1 - PROPORTIONAL: as we cannot choose between the two branches
 *          in the tree that stem from this split, we consider both.
 *          The  algorithm goes on until the final leaves are reached
 *          and all their predictions are used to decide the final
 *          prediction.
 */
public enum MissingStrategy {

    LAST_PREDICTION, PROPORTIONAL

}
