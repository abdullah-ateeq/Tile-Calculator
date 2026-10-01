package com.example.tilecalculator.model

/**
 * Surface mode for calculation: Floor or Wall.
 * The calculation is identical, but terminology and visual context adapts.
 */
enum class SurfaceType(val displayName: String, val areaLabel: String) {
    FLOOR("Floor", "Floor Area"),
    WALL("Wall", "Wall Area")
}
