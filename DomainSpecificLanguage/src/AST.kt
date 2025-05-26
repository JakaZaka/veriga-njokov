class City(val name: String, var blocks: MutableList<Block>) {
    fun toGeoJSON(): String {
        return buildString {
            append("""{
  "type": "FeatureCollection",
  "name": "$name",
  "features": [
""")
            append(blocks.joinToString(",\n") { it.toGeoJSON() })
            append("""
  ]
}""")
        }
    }
}

abstract class Block {
    abstract fun toGeoJSON(): String
}

open class Building(var commands: MutableList<Command>) : Block() {
    override fun toGeoJSON(): String {
        var highlight = true
        commands.forEach { command ->  if (!command.highlighted) highlight = false }
        return if (commands.size == 1) {
            buildString {
                append("""{
  "type": "Feature",
  "properties": {
    "type": "building",
    "highlighted": "$highlight"
  },
  "geometry": ${commands.first().toGeoJSON()}
}""")
            }
        } else {
            buildString {
                append("""{
  "type": "Feature",
  "properties": {
    "type": "building",
    "highlighted": "$highlight"
  },
  "geometry": {
    "type": "GeometryCollection",
    "geometries": [
""")
                append(commands.joinToString(",\n") { it.toGeoJSON() })
                append("""
    ]
  }
}""")
            }
        }
    }
}

class Road(val name: String, var commands: MutableList<Command>) : Block() {
    override fun toGeoJSON(): String {
        var highlight = true
        commands.forEach { command ->  if (!command.highlighted) highlight = false }
        return if (commands.size == 1) {
            buildString {
                append("""{
  "type": "Feature",
  "properties": {
    "type": "road",
    "name": "$name",
    "highlighted": "$highlight"
  },
  "geometry": ${commands.first().toGeoJSON()}
}""")
            }
        } else {
            buildString {
                append("""{
  "type": "Feature",
  "properties": {
    "type": "road",
    "name": "$name",
    "highlighted": "$highlight"
  },
  "geometry": {
    "type": "GeometryCollection",
    "geometries": [
""")
                append(commands.joinToString(",\n") { it.toGeoJSON() })
                append("""
    ]
  }
}""")
            }
        }
    }
}

class User(val name: String, var point: Point) : Block() {
    override fun toGeoJSON(): String {
        var highlight = point.highlighted
        return buildString {
            append("""{
  "type": "Feature",
  "properties": {
    "type": "user",
    "name": "$name",
    "highlighted": "$highlight"
  },
  "geometry": {
    "type": "Point",
    "coordinates": ${point.toGeoJSON()}
  }
}""")
        }
    }
}

class Store(val name: String, val building: Building) : Block() {
    override fun toGeoJSON(): String {
        var highlight = true
        building.commands.forEach { command ->  if (!command.highlighted) highlight = false }
        return if (building.commands.size == 1) {
            buildString {
                append("""{
  "type": "Feature",
  "properties": {
    "type": "store",
    "name": "$name",
    "highlighted": "$highlight"
  },
  "geometry": ${building.commands.first().toGeoJSON()}
}""")
            }
        } else {
            buildString {
                append("""{
  "type": "Feature",
  "properties": {
    "type": "store",
    "name": "$name"
  },
  "geometry": {
    "type": "GeometryCollection",
    "geometries": [
""")
                append(building.commands.joinToString(",\n") { it.toGeoJSON() })
                append("""
    ]
  }
}""")
            }
        }
    }
}

abstract class Command {
    open var highlighted: Boolean = false
    abstract fun toGeoJSON(): String
}

class Bend(var pointA: Point, var pointB: Point, var angle: Double) : Command() {
    var points: List<Point> = calculateArcPoints()
    override var highlighted: Boolean = false
    fun calculateArcPoints(segments: Int = 20): List<Point> {
        val midX = (pointA.x + pointB.x) / 2
        val midY = (pointA.y + pointB.y) / 2
        val dx = pointB.x - pointA.x
        val dy = pointB.y - pointA.y

        val distance = Math.hypot(dx, dy)
        val radius = distance / (2 * Math.sin(Math.toRadians(angle / 2.0)))

        val angleOffset = Math.atan2(dy, dx)
        val arcCenterAngle = angleOffset + Math.PI / 2

        val center = Point(
            midX + radius * Math.cos(arcCenterAngle),
            midY + radius * Math.sin(arcCenterAngle)
        )

        val startAngle = Math.atan2(pointA.y - center.y, pointA.x - center.x)
        val endAngle = Math.atan2(pointB.y - center.y, pointB.x - center.x)

        val angleStep = (endAngle - startAngle) / segments

        return (0..segments).map { i ->
            val theta = startAngle + i * angleStep
            Point(
                center.x + radius * Math.cos(theta),
                center.y + radius * Math.sin(theta)
            )
        }
    }

    override fun toGeoJSON(): String {

        return buildString {
            append("""{
  "type": "LineString",
  "coordinates": [
""")
            append(points.joinToString(", ") { it.toGeoJSON() })
            append("""]
}""")
        }
    }
}

class Circle(var center: Point, var radius: Double) : Command() {
    override var highlighted: Boolean = false
    fun calculateCircle(segments: Int = 36): List<Point> {
        val angleStep = 2 * Math.PI / segments
        return (0..segments).map { i ->
            val angle = i * angleStep
            Point(
                center.x + radius * Math.cos(angle),
                center.y + radius * Math.sin(angle)
            )
        }
    }

    override fun toGeoJSON(): String {
        val polygon = calculateCircle()
        return buildString {
            append("""{
  "type": "Polygon",
  "coordinates": [
    [
""")
            append(polygon.joinToString(", ") { it.toGeoJSON() })
            append("""]
  ]
}""")
        }
    }
}

class Line(var pointA: Point, var pointB: Point) : Command() {
    override var highlighted: Boolean = false
    override fun toGeoJSON(): String {
        return """{
  "type": "LineString",
  "coordinates": [${pointA.toGeoJSON()}, ${pointB.toGeoJSON()}]
}"""
    }
}

class Box(var pointA: Point, var pointB: Point) : Command() {
    var points: List<Point> = calculatePoints()
    override var highlighted: Boolean = false
    fun calculatePoints(): List<Point> {
        return listOf(
            pointA,
            Point(pointB.x, pointA.y),
            pointB,
            Point(pointA.x, pointB.y),
            pointA
        )
    }

    override fun toGeoJSON(): String {
        val polygon = calculatePoints()
        return buildString {
            append("""{
  "type": "Polygon",
  "coordinates": [
    [
""")
            append(polygon.joinToString(", ") { it.toGeoJSON() })
            append("""]
  ]
}""")
        }
    }
}

class Point(var x: Double, var y: Double) {
    var highlighted: Boolean = false
    fun toGeoJSON(): String = "[$x, $y]"
}


