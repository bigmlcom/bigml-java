package org.bigml.binding.localmodel;

/**
 * Interface used to filter Tree nodes of a Decision Tree when traversing it.
 */
public interface TreeNodeFilter {

    /**
     * Should be a function that returns a boolean when applied to each leaf node.
     *
     * @param node the node to check
     * @return true if the node must be filtered or false in otherwise
     */
    boolean filter(Tree node);
}
