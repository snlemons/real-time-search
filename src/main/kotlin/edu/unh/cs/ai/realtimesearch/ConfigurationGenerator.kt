package edu.unh.cs.ai.realtimesearch

import edu.unh.cs.ai.realtimesearch.environment.Domains
import edu.unh.cs.ai.realtimesearch.environment.Domains.*
import edu.unh.cs.ai.realtimesearch.environment.acrobot.AcrobotLink
import edu.unh.cs.ai.realtimesearch.environment.acrobot.configuration.AcrobotConfiguration
import edu.unh.cs.ai.realtimesearch.environment.acrobot.configuration.AcrobotStateConfiguration
import edu.unh.cs.ai.realtimesearch.experiment.configuration.Configurations
import edu.unh.cs.ai.realtimesearch.experiment.configuration.realtime.TimeBoundType
import edu.unh.cs.ai.realtimesearch.experiment.configuration.realtime.TimeBoundType.DYNAMIC
import edu.unh.cs.ai.realtimesearch.experiment.configuration.realtime.TimeBoundType.STATIC
import edu.unh.cs.ai.realtimesearch.planner.CommitmentStrategy.MULTIPLE
import edu.unh.cs.ai.realtimesearch.planner.CommitmentStrategy.SINGLE
import edu.unh.cs.ai.realtimesearch.planner.Planners
import edu.unh.cs.ai.realtimesearch.planner.Planners.*
import groovy.json.JsonOutput
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*
import java.util.concurrent.TimeUnit

val terminationType = "time"
val timeLimit = TimeUnit.NANOSECONDS.convert(300, TimeUnit.SECONDS)
val actionDurations = listOf(6000000, 10000000, 20000000, 40000000, 80000000, 160000000, 320000000, 640000000)
val lookaheadLimits = 1..6

fun main(args: Array<String>) {
    val configurations = mutableListOf<MutableMap<String, Any?>>()

    for (domain in Domains.values()) {
        if (domain == VACUUM_WORLD)
            continue

        for (planner in Planners.values()) {
            for (actionDuration in actionDurations) {
                // Skip impossible Acrobot configurations
                if (domain == ACROBOT) {
                    // Goal unreachable for these action durations
                    if (actionDuration <= 40000000) {
                        continue
                    }
                }

                val partialConfiguration = mutableMapOf<String, Any?>(
                        Configurations.DOMAIN_NAME.toString() to domain,
                        Configurations.ALGORITHM_NAME.toString() to planner,
                        Configurations.ACTION_DURATION.toString() to actionDuration,
                        Configurations.TIME_LIMIT.toString() to timeLimit,
                        Configurations.TERMINATION_TYPE.toString() to terminationType
                )

                val realTimePlannerConfigurations = getPlannerConfigurations(planner)
                val domainConfigurations = getDomainConfigurations(domain)

                for (realTimePlannerConfiguration in realTimePlannerConfigurations) {
                    for (domainConfiguration in domainConfigurations) {

                        val completeConfiguration = mutableMapOf<String, Any?>()

                        completeConfiguration.putAll(partialConfiguration)
                        completeConfiguration.putAll(realTimePlannerConfiguration)
                        completeConfiguration.putAll(domainConfiguration)

                        // Skip impossible (Java heap space) Acrobot configurations
                        if (domain == ACROBOT) {
                            val instance = domainConfiguration["domainInstanceName"] ?: continue
                            if (actionDuration <= 80000000) {
                                if (!instance.equals("0.3-0.3"))
                                    continue
                            } else if (actionDuration <= 160000000) {
                                if (instance.equals("0.07-0.07") || instance.equals("0.08-0.08"))
                                    continue
                            }
                        }

                        configurations.add(completeConfiguration)
                    }
                }
            }
        }
    }

    //    for (configuration in configurations) {
    //        println(ExperimentData(configuration).toIndentedJson())
    //    }

    println("${configurations.size} configurations were generated.")
    uploadConfigurations(configurations)
}

fun getDomainConfigurations(domain: Domains): MutableList<MutableMap<String, Any?>> {
    val configurations = mutableListOf<MutableMap<String, Any?>>()

    val gridMaps = listOf(
            "input/vacuum/dylan/cups.vw",
            "input/vacuum/dylan/slalom.vw",
            "input/vacuum/dylan/uniform.vw",
            "input/vacuum/dylan/wall.vw"
    )

    val racetrackMaps = listOf(
            "input/racetrack/barto-big.track",
            "input/racetrack/barto-small.track"
    )

    val pointRobotMaps = listOf(
            "input/pointrobot/dylan/cups.pr",
            "input/pointrobot/dylan/slalom.pr",
            "input/pointrobot/dylan/uniform.pr",
            "input/pointrobot/dylan/wall.pr"
    )

    val slidingTile4MapRoot = "input/tiles/korf/4/all/"

    //    val slidingTileHardMaps = listOf(1, 4, 5, 20, 22, 33, 46, 60, 61, 63, 72, 74, 82, 88, 90, 91)
    val slidingTileSolvableMaps = listOf(2, 3, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 23, 24, 25,
            26, 27, 28, 29, 30, 31, 32, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 47, 48, 49, 50,
            51, 52, 53, 54, 55, 56, 57, 58, 59, 62, 64, 65, 66, 67, 68, 69, 70, 71, 73, 75,
            76, 77, 78, 79, 80, 81, 83, 84, 85, 86, 87, 89, 92, 93, 94, 95, 96, 97, 98, 99, 100)

    //    val slidingTile25EasyMapNames = listOf(9, 12, 19, 28, 30, 31, 42, 45, 47, 48, 55, 57, 61, 71, 73, 74, 79, 81, 85, 86, 90, 93, 94, 95, 97)
    //    val slidingTileEasyMapNames = listOf (5, 6, 8, 9, 12, 13, 19, 20, 23, 28, 30, 31, 34, 38, 39, 42, 45, 46, 47, 48, 51, 55, 57, 58, 61, 62, 65, 71, 73, 74, 77, 78, 79, 81, 85, 86, 90, 93, 94, 95, 96, 97)

    when (domain) {
        ACROBOT -> {
            val bounds = listOf(
                    0.3,
                    0.1,
                    0.09,
                    0.08,
                    0.07
            )
            val stateConfiguration = AcrobotStateConfiguration()

            for (bound in bounds) {
                val acrobotConfiguration = AcrobotConfiguration(
                        endLink1LowerBound = AcrobotLink(bound, bound),
                        endLink2LowerBound = AcrobotLink(bound, bound),
                        endLink1UpperBound = AcrobotLink(bound, bound),
                        endLink2UpperBound = AcrobotLink(bound, bound),
                        stateConfiguration = stateConfiguration
                )
                configurations.add(mutableMapOf(
                        Configurations.RAW_DOMAIN.toString() to "${JsonOutput.toJson(acrobotConfiguration)}",
                        Configurations.DOMAIN_INSTANCE_NAME.toString() to "$bound-$bound"
                ))
            }
        }
        GRID_WORLD, VACUUM_WORLD -> {
            for (map in gridMaps) {
                val input = GRID_WORLD.javaClass.classLoader.getResourceAsStream(map)
                configurations.add(mutableMapOf(
                        Configurations.RAW_DOMAIN.toString() to Scanner(input).useDelimiter("\\Z").next(),
                        Configurations.DOMAIN_INSTANCE_NAME.toString() to map
                ))
            }
        }
        POINT_ROBOT, POINT_ROBOT_WITH_INERTIA -> {
            for (map in pointRobotMaps) {
                val input = GRID_WORLD.javaClass.classLoader.getResourceAsStream(map)
                configurations.add(mutableMapOf(
                        Configurations.RAW_DOMAIN.toString() to Scanner(input).useDelimiter("\\Z").next(),
                        Configurations.DOMAIN_INSTANCE_NAME.toString() to map
                ))
            }
        }
        RACETRACK -> {
            for (map in racetrackMaps) {
                val input = GRID_WORLD.javaClass.classLoader.getResourceAsStream(map)
                configurations.add(mutableMapOf(
                        Configurations.RAW_DOMAIN.toString() to Scanner(input).useDelimiter("\\Z").next(),
                        Configurations.DOMAIN_INSTANCE_NAME.toString() to map
                ))
            }
        }
        SLIDING_TILE_PUZZLE_4 -> {
            //            for (instanceName in 1..100) {
            for (instanceName in slidingTileSolvableMaps) {
                val map = "$slidingTile4MapRoot$instanceName"
                val input = GRID_WORLD.javaClass.classLoader.getResourceAsStream(map)
                configurations.add(mutableMapOf(
                        Configurations.RAW_DOMAIN.toString() to Scanner(input).useDelimiter("\\Z").next(),
                        Configurations.DOMAIN_INSTANCE_NAME.toString() to map
                ))
            }
        }
        POINT_ROBOT, POINT_ROBOT_WITH_INERTIA -> {
            for (map in pointRobotMaps) {
                val input = GRID_WORLD.javaClass.classLoader.getResourceAsStream(map)
                configurations.add(mutableMapOf(
                        Configurations.RAW_DOMAIN.toString() to Scanner(input).useDelimiter("\\Z").next(),
                        Configurations.DOMAIN_INSTANCE_NAME.toString() to map
                ))
            }
        }
        POINT_ROBOT_LOST -> {

        }
    }

    return configurations
}

fun getPlannerConfigurations(planner: Planners): MutableList<MutableMap<String, Any?>> {
    val configurations = mutableListOf<MutableMap<String, Any?>>()

    val weights = listOf(
            2.0,
            3.0
    )

    val maxCounts = listOf(
            3
    )

    when (planner) {
        DYNAMIC_F_HAT, LSS_LRTA_STAR -> {
            for (timeBoundType in TimeBoundType.values()) {

                when (timeBoundType) {
                    STATIC -> {
                        configurations.add(mutableMapOf<String, Any?>(
                                Configurations.TIME_BOUND_TYPE.toString() to timeBoundType,
                                Configurations.COMMITMENT_STRATEGY.toString() to SINGLE
                        ))
                        configurations.add(mutableMapOf<String, Any?>(
                                Configurations.TIME_BOUND_TYPE.toString() to timeBoundType,
                                Configurations.COMMITMENT_STRATEGY.toString() to MULTIPLE
                        ))
                    }
                    DYNAMIC -> {
                        configurations.add(mutableMapOf<String, Any?>(
                                Configurations.TIME_BOUND_TYPE.toString() to timeBoundType,
                                Configurations.COMMITMENT_STRATEGY.toString() to MULTIPLE
                        ))
                    }
                }

            }
        }
        RTA_STAR -> {
            for (lookaheadDepthLimit in lookaheadLimits) {
                configurations.add(mutableMapOf<String, Any?>(
                        Configurations.LOOKAHEAD_DEPTH_LIMIT.toString() to lookaheadDepthLimit,
                        Configurations.TIME_BOUND_TYPE.toString() to STATIC,
                        Configurations.COMMITMENT_STRATEGY.toString() to SINGLE
                ))
            }
        }
        ARA_STAR -> {
            for (maxCount in maxCounts) {
                configurations.add(mutableMapOf<String, Any?>(
                        Configurations.ANYTIME_MAX_COUNT.toString() to maxCount
                ))
            }
        }
        WEIGHTED_A_STAR -> {
            for (weight in weights) {
                configurations.add(mutableMapOf(
                        Configurations.WEIGHT.toString() to weight
                ))
            }
        }
        else -> configurations.add(mutableMapOf())
    }

    return configurations
}

fun uploadConfigurations(configurations: MutableList<MutableMap<String, Any?>>) {
    val restTemplate = RestTemplate()
    val serverUrl = "http://aerials.cs.unh.edu:3824/configurations"
    //    var serverUrl = "http://localhost:3824/configurations"

    println("${configurations.size} configurations has been generated.")

    print("Upload configurations (y/n)? ")
    val input = readLine()
    when (input?.toLowerCase()) {
        "y", "yes" -> {
            try {
                val responseEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, HttpEntity(configurations), Nothing::class.java)
                if (responseEntity.statusCode == HttpStatus.OK) {
                    println("Upload completed! ${configurations.size}")
                } else {
                    println("Upload failed!")
                }
            } catch (e: RestClientException) {
                println("Upload failed!")
            }
        }
        else -> {
            println("Not uploading")
        }
    }
}