package com.example.tilecalculator.data

import com.example.tilecalculator.model.CalculationHistoryItem
import com.example.tilecalculator.model.MeasurementUnit
import com.example.tilecalculator.model.SurfaceType
import java.util.UUID

/**
 * Lightweight, safe JSON serializer for calculation history.
 * Runs seamlessly on both Android runtime and JVM unit test environments.
 */
object CalculationHistoryJson {

    fun itemToJson(item: CalculationHistoryItem): String {
        return buildString {
            append("{")
            append("\"id\":\"").append(escape(item.id)).append("\",")
            append("\"timestamp\":").append(item.timestamp).append(",")
            append("\"surfaceType\":\"").append(item.surfaceType.name).append("\",")
            append("\"length\":").append(item.length).append(",")
            append("\"width\":").append(item.width).append(",")
            append("\"areaUnit\":\"").append(item.areaUnit.name).append("\",")
            append("\"areaSquareMeters\":").append(item.areaSquareMeters).append(",")
            append("\"tileLength\":").append(item.tileLength).append(",")
            append("\"tileWidth\":").append(item.tileWidth).append(",")
            append("\"tileUnit\":\"").append(item.tileUnit.name).append("\",")
            append("\"tileAreaSquareMeters\":").append(item.tileAreaSquareMeters).append(",")
            append("\"wastePercent\":").append(item.wastePercent).append(",")
            append("\"baseTiles\":").append(item.baseTiles).append(",")
            append("\"wasteTiles\":").append(item.wasteTiles).append(",")
            append("\"totalTiles\":").append(item.totalTiles).append(",")
            append("\"tilesPerBox\":").append(item.tilesPerBox ?: "null").append(",")
            append("\"requiredBoxes\":").append(item.requiredBoxes ?: "null").append(",")
            append("\"purchasedTiles\":").append(item.purchasedTiles ?: "null").append(",")
            append("\"extraTiles\":").append(item.extraTiles ?: "null").append(",")
            append("\"pricePerTile\":").append(item.pricePerTile ?: "null").append(",")
            append("\"pricePerBox\":").append(item.pricePerBox ?: "null").append(",")
            append("\"estimatedCost\":").append(item.estimatedCost ?: "null")
            append("}")
        }
    }

    fun listToJson(list: List<CalculationHistoryItem>): String {
        return list.joinToString(separator = ",", prefix = "[", postfix = "]") { itemToJson(it) }
    }

    fun listFromJson(json: String): List<CalculationHistoryItem> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()

        val items = mutableListOf<CalculationHistoryItem>()
        // Parse objects inside the array
        var depth = 0
        var startIndex = -1
        var inQuotes = false
        var isEscaped = false

        for (i in trimmed.indices) {
            val c = trimmed[i]
            if (c == '\\' && inQuotes) {
                isEscaped = !isEscaped
                continue
            }
            if (c == '"' && !isEscaped) {
                inQuotes = !inQuotes
            }
            isEscaped = false

            if (!inQuotes) {
                if (c == '{') {
                    if (depth == 0) startIndex = i
                    depth++
                } else if (c == '}') {
                    depth--
                    if (depth == 0 && startIndex != -1) {
                        val objStr = trimmed.substring(startIndex, i + 1)
                        itemFromJson(objStr)?.let { items.add(it) }
                        startIndex = -1
                    }
                }
            }
        }
        return items
    }

    fun itemFromJson(json: String): CalculationHistoryItem? {
        val trimmed = json.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null

        val map = parseSimpleJsonObject(trimmed)
        if (map.isEmpty()) return null

        return try {
            CalculationHistoryItem(
                id = map["id"] ?: UUID.randomUUID().toString(),
                timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                surfaceType = SurfaceType.valueOf(map["surfaceType"] ?: SurfaceType.FLOOR.name),
                length = map["length"]?.toDoubleOrNull() ?: return null,
                width = map["width"]?.toDoubleOrNull() ?: return null,
                areaUnit = MeasurementUnit.valueOf(map["areaUnit"] ?: MeasurementUnit.METER.name),
                areaSquareMeters = map["areaSquareMeters"]?.toDoubleOrNull() ?: return null,
                tileLength = map["tileLength"]?.toDoubleOrNull() ?: return null,
                tileWidth = map["tileWidth"]?.toDoubleOrNull() ?: return null,
                tileUnit = MeasurementUnit.valueOf(map["tileUnit"] ?: MeasurementUnit.CENTIMETER.name),
                tileAreaSquareMeters = map["tileAreaSquareMeters"]?.toDoubleOrNull() ?: return null,
                wastePercent = map["wastePercent"]?.toDoubleOrNull() ?: 10.0,
                baseTiles = map["baseTiles"]?.toIntOrNull() ?: return null,
                wasteTiles = map["wasteTiles"]?.toIntOrNull() ?: 0,
                totalTiles = map["totalTiles"]?.toIntOrNull() ?: return null,
                tilesPerBox = map["tilesPerBox"]?.toIntOrNull(),
                requiredBoxes = map["requiredBoxes"]?.toIntOrNull(),
                purchasedTiles = map["purchasedTiles"]?.toIntOrNull(),
                extraTiles = map["extraTiles"]?.toIntOrNull(),
                pricePerTile = map["pricePerTile"]?.toDoubleOrNull(),
                pricePerBox = map["pricePerBox"]?.toDoubleOrNull(),
                estimatedCost = map["estimatedCost"]?.toDoubleOrNull()
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSimpleJsonObject(json: String): Map<String, String> {
        val content = json.trim().removeSurrounding("{", "}").trim()
        val result = mutableMapOf<String, String>()
        if (content.isEmpty()) return result

        var inQuotes = false
        var isEscaped = false
        val tokens = mutableListOf<String>()
        var currentToken = StringBuilder()

        for (i in content.indices) {
            val c = content[i]
            if (c == '\\' && inQuotes) {
                isEscaped = !isEscaped
                currentToken.append(c)
                continue
            }
            if (c == '"' && !isEscaped) {
                inQuotes = !inQuotes
            }
            isEscaped = false

            if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString().trim())
                currentToken = StringBuilder()
            } else {
                currentToken.append(c)
            }
        }
        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString().trim())
        }

        for (token in tokens) {
            val colonIndex = token.indexOf(':')
            if (colonIndex != -1) {
                val rawKey = token.substring(0, colonIndex).trim().removeSurrounding("\"")
                val rawVal = token.substring(colonIndex + 1).trim()
                val value = if (rawVal == "null") null else rawVal.removeSurrounding("\"")
                if (value != null) {
                    result[rawKey] = value
                }
            }
        }
        return result
    }

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
    }
}
