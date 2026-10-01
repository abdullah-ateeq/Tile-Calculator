package com.example.tilecalculator.model

/**
 * Validated calculation input parameters.
 */
data class TileCalculationInput(
    val surfaceType: SurfaceType,
    val length: Double,
    val width: Double,
    val areaUnit: MeasurementUnit,
    val tileLength: Double,
    val tileWidth: Double,
    val tileUnit: MeasurementUnit,
    val wastePercent: Double,
    val tilesPerBox: Int? = null,
    val pricePerTile: Double? = null,
    val pricePerBox: Double? = null
)
