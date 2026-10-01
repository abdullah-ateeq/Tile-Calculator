package com.example.tilecalculator.util

import com.example.tilecalculator.model.MeasurementUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Centralized unit conversion and formatting utility.
 * All internal calculations normalize dimensions to meters and area to square meters.
 */
object UnitConverter {

    const val MILLIMETERS_PER_METER = 1000.0
    const val CENTIMETERS_PER_METER = 100.0
    const val METERS_PER_METER = 1.0
    const val METERS_PER_INCH = 0.0254
    const val METERS_PER_FOOT = 0.3048

    /**
     * Converts a length measurement from the specified unit into meters.
     */
    fun convertToMeters(value: Double, unit: MeasurementUnit): Double {
        if (value <= 0.0) return 0.0
        return when (unit) {
            MeasurementUnit.MILLIMETER -> value / MILLIMETERS_PER_METER
            MeasurementUnit.CENTIMETER -> value / CENTIMETERS_PER_METER
            MeasurementUnit.METER -> value * METERS_PER_METER
            MeasurementUnit.INCH -> value * METERS_PER_INCH
            MeasurementUnit.FOOT -> value * METERS_PER_FOOT
        }
    }

    /**
     * Converts a length in meters into the specified unit.
     */
    fun convertFromMeters(meters: Double, unit: MeasurementUnit): Double {
        if (meters <= 0.0) return 0.0
        return when (unit) {
            MeasurementUnit.MILLIMETER -> meters * MILLIMETERS_PER_METER
            MeasurementUnit.CENTIMETER -> meters * CENTIMETERS_PER_METER
            MeasurementUnit.METER -> meters / METERS_PER_METER
            MeasurementUnit.INCH -> meters / METERS_PER_INCH
            MeasurementUnit.FOOT -> meters / METERS_PER_FOOT
        }
    }

    /**
     * Calculates the rectangular area in square meters.
     * Dimensions are converted to meters before calculating area.
     */
    fun calculateAreaSquareMeters(length: Double, width: Double, unit: MeasurementUnit): Double {
        if (length <= 0.0 || width <= 0.0) return 0.0
        val lengthMeters = convertToMeters(length, unit)
        val widthMeters = convertToMeters(width, unit)
        return lengthMeters * widthMeters
    }

    /**
     * Formats an area in square meters for display (e.g., "20.00 m²").
     */
    fun formatSquareMeters(areaSqMeters: Double, decimals: Int = 2): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (decimals == 4) "#,##0.0000" else "#,##0.00"
        val df = DecimalFormat(pattern, symbols)
        return "${df.format(areaSqMeters)} m²"
    }

    /**
     * Formats an area in the user's selected unit (e.g., "20.00 m²", "215.28 ft²").
     */
    fun formatAreaInUnit(length: Double, width: Double, unit: MeasurementUnit): String {
        val area = length * width
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.00", symbols)
        val unitLabel = when (unit) {
            MeasurementUnit.MILLIMETER -> "mm²"
            MeasurementUnit.CENTIMETER -> "cm²"
            MeasurementUnit.METER -> "m²"
            MeasurementUnit.INCH -> "in²"
            MeasurementUnit.FOOT -> "ft²"
        }
        return "${df.format(area)} $unitLabel"
    }

    /**
     * Formats dimensions string (e.g., "60 × 60 cm").
     */
    fun formatDimensions(length: Double, width: Double, unit: MeasurementUnit): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.##", symbols)
        return "${df.format(length)} × ${df.format(width)} ${unit.symbol}"
    }

    /**
     * Formats currency or numerical cost value.
     */
    fun formatCurrency(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("$#,##0.00", symbols)
        return df.format(amount)
    }

    /**
     * Formats a clean decimal string without trailing zeroes where appropriate.
     */
    fun formatDecimal(value: Double, maxDecimals: Int = 2): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = when (maxDecimals) {
            4 -> "#,##0.####"
            else -> "#,##0.##"
        }
        val df = DecimalFormat(pattern, symbols)
        return df.format(value)
    }
}
