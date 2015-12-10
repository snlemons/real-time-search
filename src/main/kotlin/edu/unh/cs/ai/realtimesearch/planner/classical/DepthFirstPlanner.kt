package edu.unh.cs.ai.realtimesearch.planner.classical

import edu.unh.cs.ai.realtimesearch.domain.Action
import edu.unh.cs.ai.realtimesearch.domain.Domain
import edu.unh.cs.ai.realtimesearch.domain.State
import edu.unh.cs.ai.realtimesearch.domain.SuccessorBundle
import edu.unh.cs.ai.realtimesearch.planner.ClassicalPlanner
import java.util.*

/**
 * The famous depth first planner.
 *
 * @param domain is the domain to plan in
 */
class DepthFirstPlanner(domain: Domain) : ClassicalPlanner(domain) {
    data class Node(val parent: Node?, val successorBundle: SuccessorBundle)

    private var generatedNodes = 0
    private val openList: Deque<Node> = linkedListOf()

    /**
     * Returns a plan (list of actions) by iteratively expanding nodes from the
     * open list and adding expansions from those nodes.
     * Only those nodes that have not been visited yet in the current path are added.
     *
     * Interface of ClassicalPlanner
     * @param state is the
     * @return a list of actions, defining the plan
     */
    override fun plan(state: State): List<Action> {

        // init class members
        // (in case we planned with this planner before)
        openList.clear()
        generatedNodes = 0

        // main loop
        var currentNode = Node(null, SuccessorBundle(state, null, 0.0))
        while (!domain.isGoal(currentNode.successorBundle.successorState)) {

            // expand (only those not visited yet)
            for (successor in domain.successors(currentNode.successorBundle.successorState)) {
                if (!visitedBefore(successor.successorState, currentNode)) {
                    generatedNodes.inc()
                    openList.push(Node(currentNode, successor))
                }
            }

            // check next node
            currentNode = openList.pop()
        }

        return getActions(currentNode)
    }

    /**
     * @brief Checks whether a state has been visited before in current path
     *
     * Will go through node up to root to see if state is in 1 of the nodes
     *
     * @param state is the state to check whether it has been visited before
     * @param leave is current end of the path
     *
     * @return true if state has been visited before
     */
    private fun visitedBefore(state: State, leave: Node): Boolean {
        var node: Node? = leave

        // root will have null action. So as long as the parent
        // is not null, we can take it's action and assume all is good
        while (node != null) {

            if (state == node.successorBundle.successorState)
                return true

            node = node.parent
        }

        return false
    }

    /**
     * @brief Returns the actions necessary to get to node
     *
     * @param leave the current end of the path
     *
     * @return list of actions to get to leave
     */
    private fun getActions(leave: Node): List<Action> {
        val actions: MutableList<Action> = arrayListOf()

        var node = leave
        // root will have null action. So as long as the parent
        // is not null, we can take it's action and assume all is good
        while (node.parent != null) {
            actions.add(node.successorBundle.action!!)
            node = node.parent!!
        }

        return actions.reversed() // we are adding actions in wrong order, to return the reverser
    }
}

