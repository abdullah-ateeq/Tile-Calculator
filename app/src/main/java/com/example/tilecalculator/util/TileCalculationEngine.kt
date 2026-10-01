package com.example.tilecalculator.util

import com.example.tilecalculator.model.MeasurementUnit
import com.example.tilecalculator.model.TileCalculationInput
import com.example.tilecalculator.model.TileCalculationResult
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil

/**
 * Pure calculation engine for Tile Calculator.
 * All arithmetic uses high precision and ceiling rounding for quantities.
 */
object TileCalculationEngine {

    private const val EPSILON = 1e-9

    /**
     * Safely computes the ceiling of a division to prevent precision errors
     * (e.g., 10.0 / 1.0 resulting in 10.000000000000002 being rounded up to 11).
     */
    fun safeCeil(numerator: Double, denominator: Double): Int {
        if (denominator <= 0.0 || numerator <= 0.0) return 0
        val raw = numerator / denominator
        // Subtract tiny epsilon to ensure exact divisions don't round up to next integer
        val adjusted = (raw - EPSILON).coerceAtLeast(0.0)
        return ceil(adjusted).toInt().coerceAtLeast(1)
    }

    /**
     * Safely computes ceiling of a single double value.
     */
    fun safeCeilValue(value: Double): Int {
        if (value <= 0.0) return 0
        val adjusted = (value - EPSILON).coerceAtLeast(0.0)
        return ceil(adjusted).toInt()
    }

    /**
     * Validates input dimensions and parameters.
     */
    fun validateInput(input: TileCalculationInput): CalculationValidationResult {
        val errors = mutableMapOf<String, String>()

        if (input.length <= 0.0) {
            errors["length"] = "Length must be greater than zero."
        }
        if (input.width <= 0.0) {
            errors["width"] = "Width must be greater than zero."
        }
        if (input.tileLength <= 0.0) {
            errors["tileLength"] = "Tile length must be greater than zero."
        }
        if (input.tileWidth <= 0.0) {
            errors["tileWidth"] = "Tile width must be greater than zero."
        }
        if (input.wastePercent < 0.0 || input.wastePercent > 100.0) {
            errors["waste"] = "Waste percentage must be between 0 and 100."
        }
        input.tilesPerBox?.let {
            if (it <= 0) {
                errors["tilesPerBox"] = "Tiles per box must be greater than zero."
            }
        }
        input.pricePerTile?.let {
            if (it < 0.0) {
                errors["pricePerTile"] = "Price cannot be negative."
            }
        }
        input.pricePerBox?.let {
            if (it < 0.0) {
                errors["pricePerBox"] = "Price cannot be negative."
            }
        }

        return CalculationValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Executes the tile calculation flow according to the specification:
     * 1. Validate room/area dimensions
     * 2. Validate tile dimensions
     * 3. Convert dimensions to meters
     * 4. Calculate area (m²)
     * 5. Calculate tile area (m²)
     * 6. Calculate base tile quantity (ceil)
     * 7. Apply waste percentage
     * 8. Round required waste & total quantity upward (ceil)
     * 9. If boxes are provided:
     *    - Calculate boxes (ceil)
     *    - Calculate purchased tiles
     *    - Calculate extra tiles
     * 10. Calculate optional cost
     */
    fun calculateTiles(input: TileCalculationInput): TileCalculationResult? {
        val validation = validateInput(input)
        if (!validation.isValid) {
            return null
        }

        val areaMeters = UnitConverter.calculateAreaSquareMeters(
            input.length,
            input.width,
            input.areaUnit
        )

        val tileAreaMeters = UnitConverter.calculateAreaSquareMeters(
            input.tileLength,
            input.tileWidth,
            input.tileUnit
        )

        if (areaMeters <= 0.0 || tileAreaMeters <= 0.0) {
            return null
        }

        // Base Tiles = Ceiling(area ÷ tileArea)
        val baseTiles = safeCeil(areaMeters, tileAreaMeters)

        // Waste Tiles = Ceiling(baseTiles × wastePercentage ÷ 100)
        val wasteTiles = if (input.wastePercent > 0.0) {
            safeCeilValue(baseTiles * (input.wastePercent / 100.0))
        } else {
            0
        }

        // Total Tiles = Base Tiles + Waste Tiles
        val totalTiles = baseTiles + wasteTiles

        // Box Calculations
        val tilesPerBox = input.tilesPerBox?.takeIf { it > 0 }
        val requiredBoxes = tilesPerBox?.let { boxSize ->
            safeCeil(totalTiles.toDouble(), boxSize.toDouble())
        }
        val purchasedTiles = if (requiredBoxes != null) {
            requiredBoxes * tilesPerBox!!
        } else {
            null
        }
        val extraTiles = if (purchasedTiles != null) {
            (purchasedTiles - totalTiles).coerceAtLeast(0)
        } else {
            null
        }

        // Cost Calculations (using BigDecimal for financial safety)
        val estimatedTileCost = input.pricePerTile?.takeIf { it >= 0.0 }?.let { price ->
            BigDecimal.valueOf(totalTiles.toLong())
                .multiply(BigDecimal.valueOf(price))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        }

        val estimatedBoxCost = if (requiredBoxes != null && input.pricePerBox != null && input.pricePerBox >= 0.0) {
            BigDecimal.valueOf(requiredBoxes.toLong())
                .multiply(BigDecimal.valueOf(input.pricePerBox))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            null
        }

        val estimatedCost = when {
            estimatedBoxCost != null && estimatedTileCost != null -> estimatedBoxCost // Prefer box cost when buying boxes
            estimatedBoxCost != null -> estimatedBoxCost
            estimatedTileCost != null -> estimatedTileCost
            else -> null
        }

        return TileCalculationResult(
            areaSquareMeters = areaMeters,
            tileAreaSquareMeters = tileAreaMeters,
            baseTiles = baseTiles,
            wasteTiles = wasteTiles,
            totalTiles = totalTiles,
            requiredBoxes = requiredBoxes,
            purchasedTiles = purchasedTiles,
            extraTiles = extraTiles,
            estimatedCost = estimatedCost,
            estimatedTileCost = estimatedTileCost,
            estimatedBoxCost = estimatedBoxCost
        )
    }
}

data class CalculationValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String>
)
