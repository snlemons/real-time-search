package edu.unh.cs.ai.realtimesearch.environment.pointrobot

import edu.unh.cs.ai.realtimesearch.environment.Domain
import edu.unh.cs.ai.realtimesearch.environment.SuccessorBundle
import edu.unh.cs.ai.realtimesearch.environment.location.DoubleLocation
import edu.unh.cs.ai.realtimesearch.environment.location.Location
import java.util.*

/**
 * Double Integrator Domain
 */
class PointRobot(val width: Int, val height: Int, val blockedCells: Set<Location>,
                 val endLocation: DoubleLocation, val goalRadius: Double, val actionDuration: Long) : Domain<PointRobotState> {

    //    private val logger = LoggerFactory.getLogger(DoubleIntegrator::class.java)
    private var actions = getAllActions()

    fun getAllActions(): ArrayList<PointRobotAction> {
        var actions = ArrayList<PointRobotAction>()
        for (x in 0..6) {
            for (y in 0..6) {
                var xdot = ((x) - 3.0);
                var ydot = ((y) - 3.0);
                //                println("" + xdot + " " + ydot)
                actions.add(PointRobotAction(xdot, ydot))
            }
        }
        return actions
    }

    override fun successors(state: PointRobotState): List<SuccessorBundle<PointRobotState>> {
        // to return
        val successors: MutableList<SuccessorBundle<PointRobotState>> = arrayListOf()

        for (it in actions) {
            val dt = 0.1
            val nSteps = 10
            var valid = true

            for (i in 1..nSteps) {
                var x = state.x + (it.xdot * (dt * i));
                var y = state.y + (it.ydot * (dt * i));
                //                x += it.xdot * dt;
                //                y += it.ydot * dt;

                if (!isLegalLocation(x, y)) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                successors.add(SuccessorBundle(
                        PointRobotState(state.x + it.xdot, state.y + it.ydot),
                        PointRobotAction(it.xdot, it.ydot),
                        actionDuration));
            }
        }
        return successors
    }

    /**
     * Returns whether location within boundaries and not a blocked cell.
     *
     * @param x coordinate of the location
     * @param y coordinate of the location
     * @return true if location is legal
     */
    fun isLegalLocation(x: Double, y: Double): Boolean {
        return x >= 0 && y >= 0 && x < width &&
                y < height && Location(x.toInt(), y.toInt()) !in blockedCells
    }

    /*
    * eight way - octile distance
    * max(min(dx), min(dy))/3
    * euclidiean distance
    * */
    override fun heuristic(state: PointRobotState): Double {
        //Distance Formula
        return (distance(state) / 3) * actionDuration
    }

    override fun heuristic(startState: PointRobotState, endState: PointRobotState): Double {
        //Distance Formula
        return ((Math.sqrt(
                Math.pow((endState.x) - startState.x, 2.0)
                        + Math.pow((endState.y) - startState.y, 2.0)) - goalRadius) / 3) * actionDuration
    }

    override fun distance(state: PointRobotState): Double {
        //Distance Formula
        return (Math.sqrt(
                Math.pow((endLocation.x) - state.x, 2.0)
                        + Math.pow((endLocation.y) - state.y, 2.0)) - goalRadius)
    }

    override fun isGoal(state: PointRobotState): Boolean {
        //        val curXLoc = (state.x * 2).toInt() / 2.0
        //        val curYLoc = (state.y * 2).toInt() / 2.0
        //
        //        //        println("" + state.x + " " + curXLoc + " " + state.y + " " + curYLoc)
        //
        //
        //
        //        return (endLocation.x + 0.5) == curXLoc && (endLocation.y + 0.5) == curYLoc
        //        return endLocation.x == state.x && (endLocation.y + 0.5) == curYLoc
        return distance(state) <= 0;
    }

    override fun print(state: PointRobotState): String {
        val description = StringBuilder()

        description.append("State: at (")
        description.append(state.x)
        description.append(", ")
        description.append(state.y)
        description.append(")")

        return description.toString()
    }

    override fun randomState(): PointRobotState {
        throw UnsupportedOperationException()
    }

    override fun getGoal(): List<PointRobotState> {
        return listOf(PointRobotState(endLocation.x, endLocation.y))
    }

    override fun predecessors(state: PointRobotState): List<SuccessorBundle<PointRobotState>> {
        val predecessors: MutableList<SuccessorBundle<PointRobotState>> = arrayListOf()

        for (it in actions) {
            val dt = 0.1
            val nSteps = 10
            var valid = true

            for (i in 1..nSteps) {
                var x = state.x - (it.xdot * (dt * i));
                var y = state.y - (it.ydot * (dt * i));

                if (!isLegalLocation(x, y)) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                predecessors.add(SuccessorBundle(
                        PointRobotState(state.x - it.xdot, state.y - it.ydot),
                        PointRobotAction(it.xdot, it.ydot),
                        actionDuration));
            }
        }
        return predecessors
    }
}

