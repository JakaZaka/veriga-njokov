class City(val name: String, var blocks: MutableList<Block>){
//FEATURE COLLECTION
fun toGeoJSON(): String{
    var result = StringBuilder()
    result.append("{\n" +
            " \"type\": \"FeatureCollection\", \n" +
            " \"name\": \"$name\", \n" +
            "\"features\": [ \n"
    )
    result.append(blocks.joinToString(separator = ",\n"){it.toGeoJSON()})
    result.append("]\n}\n")
    return result.toString()

}
}

abstract class Block(){
    //FEATURE
    abstract fun toGeoJSON(): String;
}

open class Building(var commands: MutableList<Command>) : Block(){
    override fun toGeoJSON(): String {
        var result = StringBuilder()
        result.append("{\n" +
                " \"type\": \"Feature\", \n" +
                " \"properties\": {\n" +
                " \"type\": \"building\"\n" +
                " },\n" +
                "\"geometry\": {\n" +
                "    \"type\": \"GeometryCollection\",\n" +
                "    \"geometries\": [ \n")
        result.append(commands.joinToString(", ") { it.toGeoJSON() })
        result.append("]\n}\n}\n")
        return result.toString();

    }
}

class Road(val name: String, var commands: MutableList<Command>) : Block(){
    override fun toGeoJSON(): String {
        var result = StringBuilder()
        result.append("{\n" +
                " \"type\": \"Feature\", \n" +
                " \"properties\": {\n" +
                " \"type\": \"road\",\n" +
                " \"name\": \"$name\"\n" +
                " },\n" +
                "\"geometry\": {\n" +
                "    \"type\": \"GeometryCollection\",\n" +
                "    \"geometries\": [ \n")
        result.append(commands.joinToString(", ") { it.toGeoJSON() })
        result.append("]\n}\n}\n")
        return result.toString();

    }
}
class User(val name: String, var point: Point) : Block(){
    override fun toGeoJSON(): String {
        var result = StringBuilder()
        result.append("{\n" +
                " \"type\": \"Feature\", \n" +
                " \"properties\": {\n" +
                " \"type\": \"user\",\n" +
                " \"name\": \"$name\"\n" +
                " },\n" +
                "\"geometry\": {\n" +
                "    \"type\": \"Point\",\n" +
                "    \"coordinates\": ${point.toGeoJSON()}\n }}")
        return result.toString();
    }
}

class Store(val name: String, val building: Building) : Block(){
    override fun toGeoJSON(): String {
        var result = StringBuilder()
        result.append("{\n" +
                " \"type\": \"Feature\", \n" +
                " \"properties\": {\n" +
                " \"type\": \"store\",\n" +
                " \"name\": \"$name\"\n" +
                " },\n" +
                "\"geometry\": {\n" +
                "    \"type\": \"GeometryCollection\",\n" +
                "    \"geometries\": [ \n")
        result.append(building.commands.joinToString(", ") { it.toGeoJSON() })
        result.append("]\n}\n}\n")
        return result.toString();
    }
}

abstract class Command(){
    abstract fun toGeoJSON(): String;
}

class Bend(var pointA: Point, var pointB: Point, var angle: Double): Command(){

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

        // Interpolate between start and end angle
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
        val polygon = calculateArcPoints()
        var result = StringBuilder()
        result.append("{\n" +
                "        \"type\": \"LineString\",\n" +
                "        \"coordinates\": [" )
        result.append(polygon.joinToString(", ") { it.toGeoJSON() })
        result.append("]}")
        return result.toString();
    }

}

class Circle(var center: Point, var radius: Double) : Command(){
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
        var result = StringBuilder()
        result.append("{\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [" )
        result.append(polygon.joinToString(", ") { it.toGeoJSON() })
        result.append("]}\n")
        return result.toString();
    }

}

class Line(var pointA: Point, var pointB: Point): Command(){
    override fun toGeoJSON(): String {

        var result = StringBuilder()
        result.append("{\n" +
                "        \"type\": \"LineString\",\n" +
                "        \"coordinates\": [ ${pointA.toGeoJSON()}, ${pointB.toGeoJSON()} ] }" )
        return result.toString();
    }

}

class Box(var pointA: Point, var pointB: Point): Command(){
    fun calculatePoints(): List<Point>{
        var points = mutableListOf<Point>()
        points.add(pointA)
        points.add(Point(pointB.x, pointA.y))
        points.add(pointB)
        points.add(Point(pointA.x, pointB.y))
        points.add(pointA)
        return points
    }
    override fun toGeoJSON(): String {
        val polygon = calculatePoints()
        var result = StringBuilder()
        result.append("{\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [" )
        result.append(polygon.joinToString(", ") { it.toGeoJSON() })
        result.append("]}\n")
        return result.toString();
    }
}

class Point(var x: Double, var y: Double){
    var highlighted: Boolean = false
    fun toGeoJSON(): String {
        return "[$x,$y]"
    }
}

class Function(var fname: String, var argumentName: String, var miniScanner: MiniScanner = MiniScanner(mutableListOf())){
    fun execute(): MutableList<Command>{
        var commands = mutableListOf<Command>()
        return commands
    }
}

