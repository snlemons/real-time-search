package edu.unh.cs.ai.realtimesearch.environment.vacuumworld

import edu.unh.cs.ai.realtimesearch.environment.Action
import edu.unh.cs.ai.realtimesearch.environment.Environment
import edu.unh.cs.ai.realtimesearch.logging.trace
import org.slf4j.LoggerFactory

/**
 * The VacuumWorld environment. Contains the domain and a current state
 *
 * @param domain is the VacuumWorld domain
 * @param initialState is the initial state. Will use random states if not provided
 */
class VacuumWorldEnvironment(private val domain: VacuumWorld, private var initialState: VacuumWorldState? = null) : Environment<VacuumWorldState> {

    private val logger = LoggerFactory.getLogger(VacuumWorldEnvironment::class.java)
    private var currentState: VacuumWorldState = initialState ?: domain.randomState()

    /**
     * Applies the action to the environment
     */
    override fun step(action: Action) {
        // contains successor per possible action
        val successorBundles = domain.successors(currentState)

        // get the state from the successors by filtering on action
        currentState = successorBundles.first { it.action == action }.state
        logger.trace { "Action $action leads to state $currentState" }
    }

    /**
     * Returns current state of the world
     */
    override fun getState() = currentState

    override fun getGoal() = domain.getGoal()

    /**
     * Returns whether current state is the goal
     */
    override fun isGoal(): Boolean {
        val goal = domain.isGoal(currentState)

        logger.trace { "State $currentState is ${if (goal) "" else "not"} a goal" }

        return goal
    }

    /**
     * Resets the current state to either initial (if given at construction), or a random state
     */
    override fun reset() {
        currentState = initialState?.copy() ?: domain.randomState()
    }
}