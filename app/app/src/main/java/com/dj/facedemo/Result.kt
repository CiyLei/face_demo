package com.dj.facedemo

/**
 * Create by ChenLei on 2021/7/13
 * Describe:
 */
data class Result<T>(val code: Int, val data: T, val msg: String)

data class DetectResult(
    val encodes: ArrayList<ArrayList<Double>>,
    val locations: ArrayList<DetectLocationResult>
)

data class DetectLocationResult(
    val bottom: Float,
    val left: Float,
    val right: Float,
    val top: Float
)

data class DistanceResult(
    val distances: ArrayList<Double>,
    val locations: ArrayList<DetectLocationResult>
)