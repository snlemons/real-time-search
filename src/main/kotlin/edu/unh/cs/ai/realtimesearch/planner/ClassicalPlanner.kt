package edu.unh.cs.ai.realtimesearch.planner

import edu.unh.cs.ai.realtimesearch.environment.*
import edu.unh.cs.ai.realtimesearch.logging.debug
import org.slf4j.LoggerFactory

/**
 * The abstract class for classical planners. Assume fully observable, deterministic nature.
 *
 * Possible derivatives of this class are depthfirst search, A* etc.
 *
 * @param domain is the domain to plan in
 */
abstract class ClassicalPlanner<StateType: State<StateType>>(protected val domain: Domain<StateType>) : Planner {

    private val logger = LoggerFactory.getLogger(ClassicalPlanner::class.java)

    public var generatedNodes = 0
    public var expandedNodes = 0

    data class Node<State>(val parent: Node<State>?, val state: State,
                           val action: Action?, val cost: Double)

    /** Interface3 functions **/

    /**
     * Resets all variables in the planner. Called before a new planning task
     */
    open protected fun initiatePlan() {
        generatedNodes = 0
    }

    /**
     * Checks whether a state has been visited before from current node.
     *
     * @param state is the current state that is being visited
     * @param leave is node at the end of the current path
     * @return whether state has been visited
     */
    protected abstract fun visitedBefore(state: StateType, leave: Node<StateType>): Boolean

    /**
     * Add node to open list
     *
     * @param node to add
     */
    protected abstract fun generateNode(node: Node<StateType>)

    /**
     * Pops a node from the open list
     *
     * @return the next node to expand
     */
    protected abstract fun popFromOpenList(): Node<StateType>

    /**
     * Returns a plan for a given initial state. A plan consists of a list of actions
     *
     * @param state is the initial state
     * @return a list of action compromising the plan
     */
    fun plan(state: StateType): List<Action> {
        // get ready / reset for plan
        reset()

        // main loop
        var currentNode = Node<StateType>(null, state, null, 0.0)
        while (!domain.isGoal(currentNode.state)) {
            currentNode = expandNode(currentNode)
        }

        return extractPlan(currentNode)
    }

    /**
     * Expands single node and generates the sucessor nodes
     *
     * @param node is the node to expand
     * @return the next node of interest
     */
    public fun expandNode(node: Node<StateType>): Node<StateType> {
        expandedNodes += 1
        if (expandedNodes % 100000 == 0) {
            logger.debug { "expanded: $expandedNodes " }
        }

        // expand (only those not visited yet)
        for (successor in domain.successors(node.state)) {
            if (!visitedBefore(successor.state, node)) {
                generatedNodes += 1

                // generate the node with correct cost
                val nodeCost = successor.actionCost + node.cost
                generateNode(Node(node, successor.state,
                        successor.action, nodeCost))
            }
        }

        return popFromOpenList()
    }

    /**
     * @brief Returns the actions necessary to get to node
     *
     * @param leave the current end of the path
     * @return list of actions to get to leave
     */
    protected fun extractPlan(leave: Node<StateType>): List<Action> {
        val actions: MutableList<Action> = arrayListOf()

        var node = leave
        // root will have null action. So as long as the parent
        // is not null, we can take it's action and assume all is good
        while (node.parent != null) {
            actions.add(node.action!!)
            node = node.parent!!
        }

        return actions.reversed() // we are adding actions in wrong order, to return the reverser
    }

    /**
     * Forcefully resets the planner to initial state, with no prior search
     * history or generated node.
     */
    public fun reset() {
        generatedNodes = 0
        expandedNodes = 0
        initiatePlan()
    }
}