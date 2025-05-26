import com.sun.org.apache.xpath.internal.operations.Variable
import jdk.incubator.vector.VectorOperators.Operator
import jdk.internal.net.http.common.Pair.pair

enum class Operators{
    Equall, GreaterThan, LessThan, GreaterThanOrEqual, LessThanOrEqual
}

class Parser(
    val scanner: Scanner,
    var currentToken: Token = scanner.nextToken(),

    val variables: MutableMap<String, Double> = mutableMapOf(),
    val pointVars: MutableMap<String, Point> = mutableMapOf(),
    val nullVars: MutableMap<String, Double?> = mutableMapOf(),
    val neighVars: MutableMap<String, Circle> = mutableMapOf(),
    val functions: MutableMap<String, Pair<String, MutableList<Int>>> = mutableMapOf(),
    //val loops: MutableMap<String, MutableList<Int>>

    //val forRange: MutableMap<Int, Int> = mutableMapOf()

) {
    fun nextToken() {
        if (!scanner.eof()) currentToken = scanner.nextToken()
        else currentToken = Token("eof", scanner.column, scanner.row, 22, scanner.eof())
    }

    fun isToken(token: String): Boolean {
        return currentToken.tokenMap[currentToken.token].equals(token)
    }

    fun primary(): Double {
        if (isToken("int")) {
            var value: Double = currentToken.lexem.toDouble()
            nextToken()
            //println("primary ok, ${currentToken.tokenMap[currentToken.token]}")
            return value
        } else if (isToken("double")) {
            var value: Double = currentToken.lexem.toDouble()
            nextToken()
            return value

        }else if (isToken("variable")) {
            var value: Double = variables.getValue(currentToken.lexem)
            nextToken()
            if (pointVars.containsKey(currentToken.lexem)) return Double.MAX_VALUE
            return value
        } else if (isToken("lparen")) {
            nextToken()
            val value: Double = aditive()
            if (isToken("rparen")) {
                nextToken()
                return value
            } else if (scanner.eof()) {
                throw Exception("Expected closing parenthesis ')'")
            } else {
                //println("Error: Expected closing parenthesis ')', found ${currentToken.tokenMap[currentToken.token]}")
                throw Exception("Unexpected token ${currentToken.tokenMap[currentToken.token]}")
            }
            /*if(isToken("rparen")) {
                if(!scanner.eof()) nextToken()
                println("primary ok, ${currentToken.tokenMap[currentToken.token]}")
                return true
            } else return false*/
        } else throw Exception("Unexpected token ${currentToken.tokenMap[currentToken.token]}")
    }

    fun unary(): Double {
        if (isToken("plus")) {
            nextToken()
            //println("unary ok, ${currentToken.tokenMap[currentToken.token]}")
            return primary()
        } else if(isToken("minus")){
            nextToken()
            //var value: Int = primary()
            return primary()
        } else return primary()
    }

    fun secondMultiplicative(unary: Double): Double {
        if (isToken("times")) {
            nextToken()
            //println("multiplicative prim ok, ${currentToken.tokenMap[currentToken.token]}")
            return secondMultiplicative(unary.times(unary()))
        } else if (isToken("divide")){
            nextToken()
            return secondMultiplicative(unary.div(unary()))
        }
        else return unary
    }

    fun multiplicative(): Double {
        //println("multiplicative ok, ${currentToken.tokenMap[currentToken.token]}")
        return secondMultiplicative(unary())
    }

    fun secondAditive(multiplicative: Double): Double {
        if (isToken("plus")) {
            //println("aditive prim ok, ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            //println("aditive prim ok, ${currentToken.tokenMap[currentToken.token]}")
            return secondAditive(multiplicative.plus(multiplicative()))
        } else if (isToken("minus")) {
            nextToken()
            return secondAditive(multiplicative.minus(multiplicative()))
        } else return multiplicative
    }

    fun aditive(): Double {
        //println("aditive ok, ${currentToken.tokenMap[currentToken.token]}")
        return secondAditive(multiplicative())
    }



    fun expr(): Double {
        //println("exor ok, ${currentToken.tokenMap[currentToken.token]}")
        return aditive()
    }

    fun name(): String{
        if(isToken("name")) {
            var name: String = currentToken.lexem.removePrefix("\"").removeSuffix("\"")
            nextToken()
            return name
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun angle(willExec: Boolean): Double {
        if(isToken("int")){

            var value = currentToken.lexem.toDouble()
            nextToken()
            if(value < -180 || value > 180 ) throw Exception("The angle is not correct, it needs to be on the range from -180 to 180")
            return value
        }
        else if (isToken("double")){

            var value = currentToken.lexem.toDouble()
            nextToken()
            if(value < -180 || value > 180 ) throw Exception("The angle is not correct, it needs to be on the range from -180 to 180")
            return value
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun operator(willExec: Boolean): Operators {
        if (isToken("equal")) {

            nextToken()
            return Operators.Equall
        }
        else if (isToken("greaterThan")){

            nextToken()
            return Operators.GreaterThan
        }
        else if (isToken("lessThan")) {

            nextToken()
            return Operators.LessThan
        }
        else if (isToken("greaterThanOrEqual")) {

            nextToken()
            return Operators.GreaterThanOrEqual
        }
        else if (isToken("lessThanOrEqual")) {

            nextToken()
            return Operators.LessThanOrEqual
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun number(willExec: Boolean): Double{
        if (isToken("int")) {

            var value = currentToken.lexem.toDouble()
            nextToken()
            return value
        }
        else if (isToken("double")) {

            var value = currentToken.lexem.toDouble()
            nextToken()
            return value
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun point(willExec: Boolean): Point{
        var x = expr()
        //if(!expr()) return false
        if (isToken("comma")) {

            nextToken()
            var y = expr()
            return Point(x, y)
        }

        //POINT
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")


        /*
        * Point()
        *  x
        *   y
        *
        *
        * */
    }

    fun compare(willExec: Boolean): Boolean{
        if (!isToken("variable")) return false
        if(!willExec) {

            nextToken()
            operator(!willExec)
            number(!willExec)
            return true
        }
        val varName = currentToken.lexem
        val varVal = variables[varName] ?: throw Exception("Variable '$varName' does not exist")

        nextToken()

        val operator = operator(willExec)

        return when (operator) {
            Operators.Equall -> varVal == number(willExec)
            Operators.GreaterThan -> varVal > number(willExec)
            Operators.LessThan -> varVal < number(willExec)
            Operators.GreaterThanOrEqual -> varVal >= number(willExec)
            Operators.LessThanOrEqual -> varVal <= number(willExec)
        }

    }

    fun line(willExec: Boolean): Line{
        if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var A = point(willExec)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var B = point(willExec)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        return Line(A, B)
    }

    fun bend(willExec: Boolean): Bend{
        //BEND ::= ((POINT), (POINT), ANGLE);
        if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var A = point(willExec)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var B = point(willExec)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var angle = angle(willExec)
        //if(!angle()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        return Bend(A, B, angle)
    }

    fun box(willExec: Boolean): Box{
        if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var A = point(willExec)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var B = point(willExec)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()

        return Box(A, B)


        /*
        *
        * Box()
        *   List<Point>
        *
        *
        *
        * */
    }

    fun circle(willExec: Boolean): Circle {
        if (isToken("lparen")) {

            nextToken()
            var center = point(willExec)
            //if (!point()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            var radius = number(willExec)
           return Circle(center, radius)
        }
        return throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun cvalue(name: String, willExec: Boolean){
        if (isToken("point")){

            nextToken()
            if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            var point = point(willExec)
            if(willExec) pointVars[name]=point
            //if (!point()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else if (isToken("null")){

            nextToken()
            if(willExec) nullVars[name]=null
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else if(isToken("neigh")){

            nextToken()
            if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            var circle = circle(willExec)
            if (willExec) neighVars[name]=circle
            //if (!circle()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()/*
            if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else{

            if(willExec) variables[name]=expr()
            else expr()
            //if(!expr()) return false
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
    }

    fun value(name: String, willExec: Boolean) {
        if (isToken("point")){

            nextToken()
            if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            pointVars[name]=point(willExec)
            //if (!point()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()

            //return true
        }
        else if (isToken("null")){

            nextToken()
            nullVars[name]=null
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else{
            if(willExec) variables[name]=expr()
            else expr()
            //if(!expr()) return false
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
    }

    fun declaration(name: String, willExec: Boolean){
        if (isToken("assign")) {

            nextToken()
            value(name, willExec)
            return
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun cdeclaration(name: String, willExec: Boolean){
        if (isToken("assign")) {

            nextToken()
            cvalue(name, willExec)
            return
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun variableopt(name: String, willExec: Boolean, city: City?){
        if (isToken("highlight")){

            if (!pointVars.containsKey(name) && !neighVars.containsKey(name)) throw Exception("No such point: $name")
            nextToken()
            if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            if(city != null) {
                if (pointVars.containsKey(name)) pointVars[name]?.highlighted = true
                else if (neighVars.containsKey(name)) {
                    highlightFeaturesInCircles(city, neighVars[name]!!)
                }
            }

            return
        }
        declaration(name, willExec)
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        return
    }

    fun ifProduction(city: City, oldWillExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        var willExec = compare(oldWillExec)
        println(willExec)
        if (!oldWillExec) willExec = oldWillExec
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!cityLine(city, willExec)) return false
        if (!cityLines(city, willExec)) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return elseProduction(city, willExec)

    }

    fun bIfProduction(building: Building, oldWillExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        var willExec = compare(oldWillExec)
        println(willExec)
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!buildingCommand(building, willExec)) return false
        if (!buildingCommands(building, willExec)) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return bElseProduction(building, willExec)
    }

    fun rIfProduction(road: Road, oldWillExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        var willExec = compare(oldWillExec)
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!roadCommand(road, willExec)) return false
        if (!roadCommands(road, willExec)) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return rElseProduction(road, willExec)
    }

    fun fIfProduction(oldWillExec: Boolean): MutableList<Command>{
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var willExec = compare(oldWillExec)
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        var commands = mutableListOf<Command>()
        commands.addAll(functionLine(willExec))//) return false
        functionLines(willExec, commands)//) return false
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

        nextToken()
        return fElseProduction(willExec, commands)
    }

    fun elseProduction(city: City, willExec: Boolean): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!cityLine(city, !willExec)) return false
            if (!cityLines(city, !willExec)) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun bElseProduction(building: Building, willExec: Boolean): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!buildingCommand(building, !willExec)) return false
            if (!buildingCommands(building, !willExec)) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun rElseProduction(road: Road, willExec: Boolean): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!roadCommand(road, willExec)) return false
            if (!roadCommands(road, willExec)) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun fElseProduction(willExec: Boolean, commands: MutableList<Command>): MutableList<Command>{
        if (isToken("else")) {

            nextToken()
            if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            commands.addAll(functionLine(willExec)) //return false
            commands.addAll(functionLines(willExec, commands)) //return false
            if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            //return commands
        }
        return commands
    }

    fun forProduction(city: City, willExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        var name = currentToken.lexem
        nextToken()
        declaration(name, willExec)
        val start = variables[name] ?: throw Exception("Variable does not exist")
        println(start)
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        val end = currentToken.lexem.toInt()
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        var list = scanner.markFunPosition()
        if (!isToken("lbracket")) return false
        nextToken()
        for (i in start.toInt() .. end) {
            variables[name]=i.toDouble()
            scanner.resetToMark(list[0], list[1], list[2])
            currentToken = scanner.nextToken()
            while (!isToken("rbracket") && !scanner.eof()){
                cityLine(city, true)
            }

            //if (!cityLines(city, true)) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun bForProduction(building: Building, willExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        var name = currentToken.lexem
        nextToken()
        declaration(name, willExec)
        val start = variables[name] ?: throw Exception("Variable does not exist")
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        val end = currentToken.lexem.toInt()
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        var list = scanner.markFunPosition()
        if (!isToken("lbracket")) return false
        nextToken()
        for (i in start.toInt() .. end) {
            scanner.resetToMark(list[0], list[1], list[2])
            currentToken = scanner.nextToken()
            while (!isToken("rbracket") && !scanner.eof()){
                buildingCommand(building, true)
            }

            //if (!cityLines(city, true)) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun rForProduction(road: Road, willExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        var name = currentToken.lexem
        nextToken()
        declaration(name, willExec)
        val start = variables[name] ?: throw Exception("Variable does not exist")
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        val end = currentToken.lexem.toInt()
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        var list = scanner.markFunPosition()
        if (!isToken("lbracket")) return false
        nextToken()
        for (i in start.toInt() .. end) {
            scanner.resetToMark(list[0], list[1], list[2])
            currentToken = scanner.nextToken()
            while (!isToken("rbracket") && !scanner.eof()){
                roadCommand(road, true)
            }

            //if (!cityLines(city, true)) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun fForProduction(willExec: Boolean): MutableList<Command>{
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("variable")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        var name = currentToken.lexem
        nextToken()
        declaration(name, willExec)
        val start = variables[name] ?: throw Exception("Variable does not exist")
        if (!isToken("to")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("int")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        val end = currentToken.lexem.toInt()
        nextToken()
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var list = scanner.markFunPosition()
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var commands = mutableListOf<Command>()
        for (i in start.toInt() .. end) {
            scanner.resetToMark(list[0], list[1], list[2])
            currentToken = scanner.nextToken()
            while (!isToken("rbracket") && !scanner.eof()){
                commands.addAll(functionLine(true))
            }

            //if (!cityLines(city, true)) return false
            if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
        }
        return commands
    }

    fun function(willExec: Boolean): Boolean{
        if (!isToken("fname")) return false
        val fname = currentToken.lexem
        nextToken()

        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        val argName = currentToken.lexem
        variables[argName]= Double.MAX_VALUE
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        functions[fname] = argName to scanner.markFunPosition()
        if (!isToken("lbracket")) return false
        nextToken()
        functionLine(false/*, false*/)//) return false
        functionLines(false, mutableListOf()/*, false*/)//) return false

        if (!isToken("rbracket")) {
            /*if(isToken("return")){
                nextToken()
                if(isToken("semi")) {
                    nextToken()
                    return true
                }
                return false
            }
            return false*/
        }
        nextToken()
        return true
    }

    fun functionLine(willExec: Boolean/*, called: Boolean*/): MutableList<Command>{

        var commands = mutableListOf<Command>()
        if (isToken("for")){

            nextToken()
            commands.addAll(fForProduction(willExec))
            return commands
        }
        if (isToken("if")){

            nextToken()
            commands.addAll(fIfProduction(willExec))
            return commands
        }
        if (isToken("let")){

            nextToken()
            if(!isToken("variable")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")


            //if(willExec) functions[fname]?.add(currentToken)
            var name = currentToken.lexem
            nextToken()
            cdeclaration(name, willExec)
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            return commands
        }
        if (isToken("box")) {

            nextToken()
            commands.add(box(willExec))
            //box(willExec, read, currentFun)
            return commands//box()
        }
        if (isToken("variable")){

            var name = currentToken.lexem
            nextToken()
            variableopt(name, willExec, null)
            //if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            //nextToken()
            return commands
        }
        if (isToken("line")){

            nextToken()
            commands.add(line(willExec))//line()
            return commands//line()
        }
        if (isToken("bend")){

            nextToken()
            commands.add(bend(willExec))
            //bend()
            return commands //bend()
        }
        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            var fname = currentToken.lexem
            nextToken()
            if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            var varVal = number(willExec)
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            if(!functions.containsKey(fname)) throw Exception("Function not defined")
            functions[fname]?.second?.addAll(scanner.markFunPosition())
            variables[functions[fname]!!.first] = varVal
            if(!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()

            scanner.resetToMark(functions[fname]!!.second[0], functions[fname]!!.second[1], functions[fname]!!.second[2])
            currentToken=scanner.nextToken()
            commands.addAll(functionLine(willExec))
            functionLines(willExec, commands)
            if (!isToken("rbracket")) {
                /*if(isToken("return")){
                    nextToken()
                    if(isToken("semi")) {
                        nextToken()
                        return true
                    }
                    return false
                }*/
                return commands
            }
            nextToken()
            variables.remove(functions[fname]!!.first)
            scanner.resetToMark(functions[fname]!!.second[3], functions[fname]!!.second[4], functions[fname]!!.second[5])
            currentToken=scanner.nextToken()
            return commands
        }
        if (isToken("return")){

            nextToken()
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

            nextToken()
            return commands
        }
        return commands

    }

    fun functionLines(willExec: Boolean, commands: MutableList<Command>): MutableList<Command>{
        var newcommands = functionLine(willExec)
        if(newcommands.isEmpty()) return commands
        else{
            commands.addAll(newcommands)
            return functionLines(willExec, commands)
        }

        //return true
    }

    fun codeLine(city: City, willExec: Boolean): Boolean{
        if (isToken("for")){
            nextToken()
            return forProduction(city, willExec)
        }
        if (isToken("if")){
            nextToken()
            return ifProduction(city, willExec)
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            var name = currentToken.lexem
            nextToken()
            cdeclaration(name, willExec)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            var name = currentToken.lexem
            nextToken()
            variableopt(name, willExec, city)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) return false
            var fname = currentToken.lexem
            nextToken()
            if(!isToken("lparen")) return false
            nextToken()
            var varVal = number(willExec)
            if (!isToken("rparen"))return false
            nextToken()
            if(!functions.containsKey(fname)) throw Exception("Function not defined")
            functions[fname]?.second?.addAll(scanner.markFunPosition())
            variables[functions[fname]!!.first] = varVal
            if(!isToken("semi"))return false
            nextToken()

            scanner.resetToMark(functions[fname]!!.second[0], functions[fname]!!.second[1], functions[fname]!!.second[2])
            currentToken=scanner.nextToken()
            functionLine(willExec)
            functionLines(willExec, mutableListOf())
            if (!isToken("rbracket")) {
                /*if(isToken("return")){
                    nextToken()
                    if(isToken("semi")) {
                        nextToken()
                        return true
                    }
                    return false
                }*/
                return false
            }
            nextToken()
            //variables.remove(functions[fname]!!.first)
            scanner.resetToMark(functions[fname]!!.second[3], functions[fname]!!.second[4], functions[fname]!!.second[5])
            currentToken=scanner.nextToken()
            return true
        }
        if(isToken("fun")){
            nextToken()
            return function(willExec)
        }
        return false

    }

    fun bCodeLine(building: Building, willExec: Boolean): Boolean{
        if (isToken("for")){
            nextToken()
            return bForProduction(building, willExec)
        }
        if (isToken("if")){
            nextToken()
            return bIfProduction(building, willExec)
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            var name = currentToken.lexem
            nextToken()
            cdeclaration(name, willExec)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            nextToken()
            var name = currentToken.lexem
            variableopt(name, willExec, null)//) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            var fname = currentToken.lexem
            nextToken()
            if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            var varVal = number(willExec)
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            if(!functions.containsKey(fname)) throw Exception("Function not defined")
            functions[fname]?.second?.addAll(scanner.markFunPosition())
            variables[functions[fname]!!.first] = varVal
            if(!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()

            scanner.resetToMark(functions[fname]!!.second[0], functions[fname]!!.second[1], functions[fname]!!.second[2])
            currentToken=scanner.nextToken()
            building.commands.addAll(functionLine(willExec))
            functionLines(willExec, building.commands)
            if (!isToken("rbracket")) {
                /*if(isToken("return")){
                    nextToken()
                    if(isToken("semi")) {
                        nextToken()
                        return true
                    }
                    return false
                }*/
                return true
            }
            nextToken()
            variables.remove(functions[fname]!!.first)
            scanner.resetToMark(functions[fname]!!.second[3], functions[fname]!!.second[4], functions[fname]!!.second[5])
            currentToken=scanner.nextToken()
            return true
        }
        return false

    }

    fun rCodeLine(road: Road, willExec: Boolean): Boolean{
        if (isToken("for")){
            nextToken()
            return rForProduction(road, willExec)
        }
        if (isToken("if")){
            nextToken()
            return rIfProduction(road, willExec)
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            var name = currentToken.lexem
            nextToken()
            cdeclaration(name, willExec)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            var name = currentToken.lexem
            nextToken()
            variableopt(name, willExec, null) //return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            var fname = currentToken.lexem
            nextToken()
            if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            var varVal = number(willExec)
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()
            if(!functions.containsKey(fname)) throw Exception("Function not defined")
            functions[fname]?.second?.addAll(scanner.markFunPosition())
            variables[functions[fname]!!.first] = varVal
            if(!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            nextToken()

            scanner.resetToMark(functions[fname]!!.second[0], functions[fname]!!.second[1], functions[fname]!!.second[2])
            currentToken=scanner.nextToken()
            road.commands.addAll(functionLine(willExec))
            functionLines(willExec, road.commands)
            if (!isToken("rbracket")) {
                /*if(isToken("return")){
                    nextToken()
                    if(isToken("semi")) {
                        nextToken()
                        return true
                    }
                    return false
                }*/
                return true
            }
            nextToken()
            variables.remove(functions[fname]!!.first)
            scanner.resetToMark(functions[fname]!!.second[3], functions[fname]!!.second[4], functions[fname]!!.second[5])
            currentToken=scanner.nextToken()
            return true
        }
        return false

    }

    fun buildingCommand(building: Building, willExec: Boolean): Boolean{
        if (isToken("box")){
            nextToken()
            if(willExec) building.commands.add(box(willExec))
            else box(willExec)
            return true
        }
        if (isToken("line")){
            nextToken()
            if(willExec) building.commands.add(line(willExec))
            else line(willExec)
            return true
        }
        return bCodeLine(building, willExec)
    }

    fun buildingCommands(building: Building, willExec: Boolean): Boolean{
        if(buildingCommand(building, willExec)) return buildingCommands(building, willExec)
        return true
    }

    fun roadCommand(road: Road, willExec: Boolean): Boolean{
        if (isToken("bend")){
            nextToken()
            road.commands.add(bend(willExec))
            return true
        }
        if (isToken("line")){
            nextToken()
            road.commands.add(line(willExec))
            return true
        }
        return rCodeLine(road, willExec)
    }

    fun roadCommands(road: Road, willExec: Boolean): Boolean{
        if(roadCommand(road, willExec)) return roadCommands(road, willExec)
        return true
    }

    fun road(willExec: Boolean): Road{
        if(!isToken("road")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var name = name()
        var road = Road(name, mutableListOf())
        //if(!name()) return false
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        roadCommand(road, willExec)
        roadCommands(road, willExec)
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        return road
    }

    fun building(willExec: Boolean): Building{
        if(!isToken("building")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        //if(!name()) return false
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var building = Building(mutableListOf())
        buildingCommand(building, willExec)
        buildingCommands(building, willExec)
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        return building
    }

    fun user (willExec: Boolean): User{
        if (!isToken("user"))throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var name = name()
        //if(!name()) return false
        if (!isToken("lbracket"))throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("point")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var point = point(willExec)
        //if(!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        return User(name, point)
    }

    fun store(willExec: Boolean): Store{
        if (!isToken("store")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var name = name()
        //if(!name()) return false
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var building = building(willExec)
        //if (!building()) return false
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        return Store(name, building)
    }

    fun block(willExec: Boolean): Block{
        if(isToken("building")) return building(willExec)
        else if(isToken("road")) return road(willExec)
        else if (isToken("user")) return user(willExec)
        else if(isToken("store")) return store(willExec)
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun cityLine(city: City, willExec: Boolean): Boolean{
        if(codeLine(city, willExec)) return true
        else if (isToken("building") || isToken("road") || isToken("user") || isToken("store")) {
            var block = block(willExec)
            if(willExec) city.blocks.add(block)
            return true
        }
        return false

        /*
        * if(codeLine()) return true
        else if (block()) return true
        return false
        * */
    }

    fun cityLines(city: City, willExec: Boolean): Boolean{
        //var blocks = mutableListOf<Block>()


        if (cityLine(city, willExec)){
            return cityLines(city, willExec)
        } else return true

    }
    fun city(name: String): City{
        var city = City(name, mutableListOf())
        cityLine(city, true)
        cityLines(city, true)
        /*if (cityLine()){
            return cityLines()
        }*/
        return city
    }
    fun code(): City{
        if(!isToken("city")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var name = name()
        //if(!name()) return false
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        var city = city(name)
        //if (!city())return false
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        //highlightFeaturesInCircles(city, neighVars)
        //functions.forEach(){ it -> println(it) }
        return city
    }






    fun parse(): City {
        var value = code()

        if (scanner.eof() && !isToken("rparen")) return value
        else throw Exception("Unexpected token ${currentToken.tokenMap[currentToken.token]} at ${currentToken.column} ${currentToken.row}  lex val ${currentToken.lexem}")
    }



    fun highlightFeaturesInCircles(city: City, circle: Circle) {
        for (block in city.blocks) {
            when (block) {
                is Building -> block.commands.forEach { highlightCommand(it, circle) }
                is Road     -> block.commands.forEach { highlightCommand(it, circle) }
                is Store    -> block.building.commands.forEach { highlightCommand(it, circle) }
                is User     -> highlightPoint(block.point, circle)
            }
        }
    }

    fun highlightCommand(cmd: Command, circle: Circle) {
        when (cmd) {
            is Line -> {
                highlightPoint(cmd.pointA, circle)
                highlightPoint(cmd.pointB, circle)
                if(cmd.pointA.highlighted && cmd.pointB.highlighted) cmd.highlighted = true
            }
            is Box -> {
                var highlight = true
                cmd.points.forEach { highlightPoint(it, circle)
                if(!it.highlighted) highlight = false
                }
                if (highlight) cmd.highlighted=true

            }
            is Bend -> {
                var highlight = true
                cmd.points.forEach { highlightPoint(it, circle)
                    if(!it.highlighted) highlight = false
                }
                if (highlight) cmd.highlighted=true
            }
            is Circle -> {
                var highlight = true
                cmd.calculateCircle().forEach { highlightPoint(it, circle)
                    if(!it.highlighted) highlight = false
                }
                if (highlight) cmd.highlighted=true
            }
        }
    }


    fun highlightPoint(point: Point, circle: Circle) {
            val dx = point.x - circle.center.x
            val dy = point.y - circle.center.y
            val distance = Math.sqrt(dx * dx + dy * dy)
            if (distance <= circle.radius) {
                point.highlighted = true
                return
            }

    }



}