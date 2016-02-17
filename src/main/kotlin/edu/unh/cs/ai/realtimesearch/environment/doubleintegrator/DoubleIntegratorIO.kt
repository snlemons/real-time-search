package edu.unh.cs.ai.realtimesearch.environment.doubleintegrator

import edu.unh.cs.ai.realtimesearch.environment.location.Location
import java.io.InputStream
import java.util.*

object DoubleIntegratorIO {

    fun parseFromStream(input: InputStream): DoubleIntegratorInstance {
        val inputScanner = Scanner(input)

        val rowCount: Int
        val columnCount: Int

        try {
            columnCount = inputScanner.nextLine().toInt()
            rowCount = inputScanner.nextLine().toInt()
        } catch (e: NoSuchElementException) {
            throw InvalidDoubleIntegratorException("DoubleIntegrator's first or second line is missing.", e)
        } catch (e: NumberFormatException) {
            throw InvalidDoubleIntegratorException("DoubleIntegrator's first and second line must be a number.", e)
        }

        val blockedCells = arrayListOf<Location>()
        var startLocation: Location? = null
        var endLocation: Location? = null

        try {
            for (y in 0..rowCount - 1) {
                val line = inputScanner.nextLine()

                for (x in 0..columnCount - 1) {
                    when (line[x]) {
                        '#' -> blockedCells.add(Location(x, y))
                        '*' -> endLocation = Location(x, y)
                        '@' -> startLocation = Location(x, y)
                    }
                }
            }
        } catch (e: NoSuchElementException) {
            throw InvalidDoubleIntegratorException("DoubleIntegrator is not complete.", e)
        }

        if (startLocation == null) {
            throw InvalidDoubleIntegratorException("Unknown start location. Start location has was not defined.")
        }

        if (endLocation == null) {
            throw InvalidDoubleIntegratorException("Unknown end location. End location has was not defined.")
        }

        val doubleIntegrator = edu.unh.cs.ai.realtimesearch.environment.doubleintegrator.DoubleIntegrator(columnCount, rowCount, blockedCells.toHashSet(), endLocation)
        val startState = DoubleIntegratorState(startLocation.x.toDouble(), startLocation.y.toDouble(), 0.0, 0.0)
        return DoubleIntegratorInstance(doubleIntegrator, startState)
    }

}

data class DoubleIntegratorInstance(val domain: DoubleIntegrator, val initialState: DoubleIntegratorState)

class InvalidDoubleIntegratorException(message: String, e: Exception? = null) : RuntimeException(message, e)
