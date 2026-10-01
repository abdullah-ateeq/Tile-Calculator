package com.example.tilecalculator.model

/**
 * Tile calculation output results.
 * Required tile quantities and boxes are always rounded upward with ceiling.
 */
data class TileCalculationResult(
    val areaSquareMeters: Double,
    val tileAreaSquareMeters: Double,
    val baseTiles: Int,
    val wasteTiles: Int,
    val totalTiles: Int,
    val requiredBoxes: Int? = null,
    val purchasedTiles: Int? = null,
    val extraTiles: Int? = null,
    val estimatedCost: Double? = null,
    val estimatedTileCost: Double? = null,
    val estimatedBoxCost: Double? = null
)
