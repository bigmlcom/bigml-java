package org.bigml.binding.localassociation;

/**
 * Interface used to filter Association Rules Items when traversing it.
 */
public interface ItemFilter {

    /**
     * Should be a function that returns a boolean when applied to each item.
     *
     * @param item the item to check
     * @return true if the rule must be filtered or false in otherwise
     */
    boolean filter(AssociationItem item);
}
