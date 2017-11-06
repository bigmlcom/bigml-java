package org.bigml.binding.localassociation;

/**
 * Interface used to filter Association Rules when traversing it.
 */
public interface RuleFilter {

    /**
     * Should be a function that returns a boolean when applied to each rule.
     *
     * @param rule the rule to check
     * @return true if the rule must be filtered or false in otherwise
     */
    boolean filter(AssociationRule rule);
}
