package edu.unh.cs.ai.realtimesearch.environment

/**
 * An environment of the experiments. In contrast to domains, which are used
 * to model transitions only, an environment is stateful
 */
interface Environment<StateType : State<StateType>> {

    /**
     * Performs a step according to action a and current (maintained state)
     * @param action is the action taken
     */
    fun step(action: Action)

    /**
     * Returns current state
     * @return the current state of the environment
     */
    fun getState(): StateType

    fun getGoal(): List<StateType>

    /**
     * Returns whether world has been solved
     *
     * @return true if goal has been reached
     */
    fun isGoal(): Boolean

    /**
     * Resets the environment for a new experiment. Any data or changes
     * made during steps are to be cleared and reset to initial.
     */
    fun reset()
}