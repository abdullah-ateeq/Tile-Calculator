package com.example.tilecalculator.model

import java.util.UUID

/**
 * Historical saved calculation record.
 */
data class CalculationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val surfaceType: SurfaceType,
    val length: Double,
    val width: Double,
    val areaUnit: MeasurementUnit,
    val areaSquareMeters: Double,
    val tileLength: Double,
    val tileWidth: Double,
    val tileUnit: MeasurementUnit,
    val tileAreaSquareMeters: Double,
    val wastePercent: Double,
    val baseTiles: Int,
    val wasteTiles: Int,
    val totalTiles: Int,
    val tilesPerBox: Int? = null,
    val requiredBoxes: Int? = null,
    val purchasedTiles: Int? = null,
    val extraTiles: Int? = null,
    val pricePerTile: Double? = null,
    val pricePerBox: Double? = null,
    val estimatedCost: Double? = null
)
