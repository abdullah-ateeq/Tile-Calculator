package com.example.tilecalculator.model

/**
 * Supported measurement units for room and tile dimensions.
 */
enum class MeasurementUnit(
    val symbol: String,
    val displayName: String,
    val factorToMeter: Double
) {
    MILLIMETER("mm", "Millimeters", 0.001),
    CENTIMETER("cm", "Centimeters", 0.01),
    METER("m", "Meters", 1.0),
    INCH("in", "Inches", 0.0254),
    FOOT("ft", "Feet", 0.3048);

    companion object {
        fun fromSymbol(symbol: String): MeasurementUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: METER
        }
    }
}
