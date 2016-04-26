package edu.unh.cs.ai.realtimesearch.environment.slidingtilepuzzle

import edu.unh.cs.ai.realtimesearch.environment.Domain
import edu.unh.cs.ai.realtimesearch.environment.SuccessorBundle
import java.lang.Math.abs

/**
 * The sliding tile puzzle is a toy domain in which numbered tiles and a blank
 * space are shifted (swapping the blank with any of the four adjacent numbered
 * tiles), with the goal of placing the numbered tiles in numerical order. 
 * Assumed to be square.
 *
 * @param size is both the width and height of the puzzle (giving n^2 - 1 tiles and a blank.)
 */
class SlidingTilePuzzle(val size: Int, val actionDuration: Long) : Domain<SlidingTilePuzzle4State> {
    val goalState: SlidingTilePuzzle4State by lazy {
        val state = SlidingTilePuzzle4State(0, 0, 0.0)
        for (i in 0..15) {
            state[i] = i.toByte()
        }

        assert(initialHeuristic(state) == 0.0)

        state
    }

    /**
     * Part of the Domain interface.
     */
    override fun successors(state: SlidingTilePuzzle4State): List<SuccessorBundle<SlidingTilePuzzle4State>> {
        val successorBundles: MutableList<SuccessorBundle<SlidingTilePuzzle4State>> = arrayListOf()

        for (action in SlidingTilePuzzleAction.values()) {
            val successorState = successorState(state, action.relativeX, action.relativeY)

            if (successorState != null) {
                successorBundles.add(SuccessorBundle(successorState, action, actionDuration))
            }
        }

        return successorBundles
    }

    private fun successorState(state: SlidingTilePuzzle4State, relativeX: Int, relativeY: Int): SlidingTilePuzzle4State? {
        val newZeroIndex = state.zeroIndex + state.getIndex(relativeX, relativeY)
        val savedTiles = state.tiles

        if (newZeroIndex >= 0 && newZeroIndex < size * size) {
            state[state.zeroIndex] = state[newZeroIndex]
            state[newZeroIndex] = 0

            val modifiedTiles = state.tiles
            val heuristic = initialHeuristic(state)

            state.tiles = savedTiles

            return SlidingTilePuzzle4State(newZeroIndex, modifiedTiles, heuristic)
        }

        return null
    }

    /**
     * Returns the state's heuristic value multiplied by action duration.
     *
     * @param state is the state to provide a heuristic for
     * @return the heuristic value multiplied by action duration
     */
    override fun heuristic(state: SlidingTilePuzzle4State): Double {
        return state.heuristic * actionDuration
    }

    /**
     * Returns a heuristic for a sliding tile puzzle state: the sum of Manhattan distance of each tile from its goal position. This function calculates a heuristic value from one given state to another given state, rather than the standard sliding tile puzzle goal.
     *
     * @param startState is the current state
     * @param endState is the goal state
     * @return the sum of Manhattan distances for all numbered tiles
     */
    override fun heuristic(startState: SlidingTilePuzzle4State, endState: SlidingTilePuzzle4State): Double {
        var manhattanSum = 0.0
        var zero: Byte = 0

        for (xStart in 0..size - 1) {
            for (yStart in 0..size - 1) {
                val value = startState[startState.getIndex(xStart, yStart)]
                if (value == zero) continue

                for (endIndex in 0..size * size - 1) {
                    if (endState[endIndex] != value) {
                        continue
                    }
                    val endX = endIndex / size
                    val endY = endIndex % size

                    manhattanSum += abs(endX - yStart) + abs(endY - xStart)
                    break
                }
            }
        }

        return manhattanSum
    }

    /**
     * Returns a heuristic for a sliding tile puzzle state: the sum of Manhattan distance of each tile from its goal position. This function calculates a heuristic value from the given state to the standard sliding tile goal state.
     *
     * @param state is the state to provide a heuristic for
     * @return the sum of Manhattan distances for all numbered tiles
     */
    fun initialHeuristic(state: SlidingTilePuzzle4State): Double {
        var manhattanSum = 0.0
        var zero: Byte = 0

        for (x in 0..size - 1) {
            for (y in 0..size - 1) {
                val value = state[state.getIndex(x, y)]
                if (value == zero) continue

                manhattanSum += abs(value / size - y) + abs(value % size - x)
            }
        }

        return manhattanSum
    }

    /**
     * Returns the distance from a given state to the goal. Always the same as heuristic value.
     *
     * @param state is the state to provide a heuristic for
     * @return distance to goal, always the same as heuristic
     */
    override fun distance(state: SlidingTilePuzzle4State) = state.heuristic

    /**
     * Returns a boolean indicating whether the given state is a goal state.
     *
     * @param state is the state to provide a heuristic for
     * @return true if state is a goal state, otherwise false
     */
    override fun isGoal(state: SlidingTilePuzzle4State) = state.heuristic == 0.0

    override fun print(state: SlidingTilePuzzle4State): String {
        throw UnsupportedOperationException()
    }

    override fun randomState(): SlidingTilePuzzle4State {
        throw UnsupportedOperationException()
    }

    override fun getGoal(): List<SlidingTilePuzzle4State> = listOf(goalState)

    override fun predecessors(state: SlidingTilePuzzle4State) = successors(state)
}