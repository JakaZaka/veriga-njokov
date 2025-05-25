import com.sun.org.apache.xpath.internal.operations.Variable
import jdk.incubator.vector.VectorOperators.Operator
import jdk.internal.net.http.common.Pair.pair

enum class Operators{
    Equall, GreaterThan, LessThan, GreaterThanOrEqual, LessThanOrEqual
}

class Parser(
    val scanner: Scanner,
    var currentToken: Token = scanner.nextToken(),
    val varVal: MutableMap<String, Int> = mutableMapOf("x" to 1, "y" to 3),
    val variables: MutableMap<String, Double> = mutableMapOf(),
    val pointVars: MutableMap<String, Point> = mutableMapOf(),
    val nullVars: MutableMap<String, Double?> = mutableMapOf(),
    val neighVars: MutableMap<String, Circle> = mutableMapOf(),
    val functions: MutableList<Function> = mutableListOf()

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

    fun angle(willExec: Boolean, read: Boolean, currentFun: Function?): Double {
        if(isToken("int")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            var value = currentToken.lexem.toDouble()
            nextToken()
            if(value < -180 || value > 180 ) throw Exception("The angle is not correct, it needs to be on the range from -180 to 180")
            return value
        }
        else if (isToken("double")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            var value = currentToken.lexem.toDouble()
            nextToken()
            if(value < -180 || value > 180 ) throw Exception("The angle is not correct, it needs to be on the range from -180 to 180")
            return value
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun operator(willExec: Boolean, read: Boolean, currentFun: Function?): Operators {
        if (isToken("equal")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            return Operators.Equall
        }
        else if (isToken("greaterThan")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            return Operators.GreaterThan
        }
        else if (isToken("lessThan")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            return Operators.LessThan
        }
        else if (isToken("greaterThanOrEqual")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            return Operators.GreaterThanOrEqual
        }
        else if (isToken("lessThanOrEqual")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            return Operators.LessThanOrEqual
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun number(willExec: Boolean, read: Boolean, currentFun: Function?): Double{
        if (isToken("int")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            var value = currentToken.lexem.toDouble()
            nextToken()
            return value
        }
        else if (isToken("double")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            var value = currentToken.lexem.toDouble()
            nextToken()
            return value
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun point(willExec: Boolean, read: Boolean, currentFun: Function?): Point{
        var x = expr()
        //if(!expr()) return false
        if (isToken("comma")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
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

    fun compare(willExec: Boolean, read: Boolean, currentFun: Function?): Boolean{
        if (!isToken("variable")) return false
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        val varName = currentToken.lexem
        val varVal = variables[varName] ?: throw Exception("Variable '$varName' does not exist")

        nextToken()

        val operator = operator(willExec, read, currentFun)

        return when (operator) {
            Operators.Equall -> varVal == number(willExec, read, currentFun)
            Operators.GreaterThan -> varVal > number(willExec, read, currentFun)
            Operators.LessThan -> varVal < number(willExec, read, currentFun)
            Operators.GreaterThanOrEqual -> varVal >= number(willExec, read, currentFun)
            Operators.LessThanOrEqual -> varVal <= number(willExec, read, currentFun)
        }

    }

    fun line(willExec: Boolean, read: Boolean, currentFun: Function?): Line{
        if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var A = point(willExec, read, currentFun)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var B = point(willExec, read, currentFun)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        return Line(A, B)
    }

    fun bend(willExec: Boolean, read: Boolean, currentFun: Function?): Bend{
        //BEND ::= ((POINT), (POINT), ANGLE);
        if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var A = point(willExec, read, currentFun)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var B = point(willExec, read, currentFun)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var angle = angle(willExec, read, currentFun)
        //if(!angle()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        return Bend(A, B, angle)
    }

    fun box(willExec: Boolean, read: Boolean, currentFun: Function?): Box{
        if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var A = point(willExec, read, currentFun)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        var B = point(willExec, read, currentFun)
        //if (!point()) return false
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
        nextToken()
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
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

    fun circle(willExec: Boolean, read: Boolean, currentFun: Function?): Circle {
        if (isToken("lparen")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            var center = point(willExec, read, currentFun)
            //if (!point()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if (!isToken("comma")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            var radius = number(willExec, read, currentFun)
           return Circle(center, radius)
        }
        return throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun cvalue(name: String, willExec: Boolean, read: Boolean, currentFun: Function?){
        if (isToken("point")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            var point = point(willExec, read, currentFun)
            if(willExec) pointVars[name]=point
            //if (!point()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else if (isToken("null")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if(willExec) nullVars[name]=null
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else if(isToken("neigh")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            var circle = circle(willExec, read, currentFun)
            if (willExec) neighVars[name]=circle
            //if (!circle()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()/*
            if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else{
            if(willExec && read) {
                var row = currentToken.row
                var column = currentToken.column
                var value = expr()
                currentFun?.miniScanner?.input?.add(Token(value.toString(), row, column, 13, false))
            }
            else if(willExec && !read) variables[name]=expr()
            else expr()
            //if(!expr()) return false
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
    }

    fun value(name: String, willExec: Boolean, read: Boolean, currentFun: Function?) {
        if (isToken("point")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            pointVars[name]=point(willExec, read, currentFun)
            //if (!point()) return false
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()

            //return true
        }
        else if (isToken("null")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            nullVars[name]=null
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
        else{
            if(willExec && read) {
                var row = currentToken.row
                var column = currentToken.column
                var value = expr()
                currentFun?.miniScanner?.input?.add(Token(value.toString(), row, column, 13, false))
            }
            else if(willExec && !read) variables[name]=expr()
            else expr()
            //if(!expr()) return false
            /*if (!isToken("semi")) return false
            nextToken()*/
            //return true
        }
    }

    fun declaration(name: String, willExec: Boolean, read: Boolean, currentFun: Function?){
        if (isToken("assign")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            value(name, willExec, read, currentFun)
            return
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun cdeclaration(name: String, willExec: Boolean, read: Boolean, currentFun: Function?){
        if (isToken("assign")) {
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            cvalue(name, willExec, read, currentFun)
            return
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun variableopt(name: String, willExec: Boolean, read: Boolean, currentFun: Function?){
        if (isToken("highlight")){
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            if (!pointVars.containsKey(name)) throw Exception("No such point: $name")
            nextToken()
            if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun?.miniScanner?.input?.add(currentToken)
            nextToken()
            pointVars[name]?.highlighted=true
            return
        }
        declaration(name, willExec, read, currentFun)
        if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        nextToken()
        return
    }

    fun ifProduction(city: City, oldWillExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        var willExec = compare(oldWillExec, false, null)
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
        var willExec = compare(oldWillExec, false, null)
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
        var willExec = compare(oldWillExec, false, null)
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

    fun fIfProduction(oldWillExec: Boolean, read: Boolean, currentFun: Function): MutableList<Command>{
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(oldWillExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        var willExec = compare(oldWillExec, read, currentFun)
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        var commands = mutableListOf<Command>()
        commands.addAll(functionLine(willExec, read, currentFun))//) return false
        functionLines(willExec, read, currentFun, commands)//) return false
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        return fElseProduction(willExec, read, currentFun, commands)
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
            if (!buildingCommand(building, willExec)) return false
            if (!buildingCommands(building, willExec)) return false
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

    fun fElseProduction(willExec: Boolean, read: Boolean, currentFun: Function, commands: MutableList<Command>): MutableList<Command>{
        if (isToken("else")) {
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            commands.addAll(functionLine(willExec, read, currentFun)) //return false
            commands.addAll(functionLines(willExec, read, currentFun, commands)) //return false
            if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
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
        declaration(name, willExec, false, null)
        val start = variables[name] ?: throw Exception("Variable does not exist")
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        val end = currentToken.lexem.toInt()
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        scanner.markPosition()
        if (!isToken("lbracket")) return false
        nextToken()
        for (i in start.toInt() .. end) {
            scanner.resetToMark()
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
        declaration(name, willExec, false, null)
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!buildingCommand(building, true)) return false
        if (!buildingCommands(building, true)) return false
        //BUILDINGCOMMAND BUILDINGCOMMANDS
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun rForProduction(road: Road, willExec: Boolean): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        var name = currentToken.lexem
        nextToken()
        declaration(name, willExec, false, null)
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!roadCommand(road, true)) return false
        if (!roadCommands(road, true)) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun fForProduction(willExec: Boolean, read: Boolean, currentFun: Function): MutableList<Command>{
        if (!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        if (!isToken("variable")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        var name = currentToken.lexem
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        declaration(name, willExec, read, currentFun)//) return false
        if (!isToken("to")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        if (!isToken("int")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        if (!isToken("lbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
        var commands = mutableListOf<Command>()
        commands.addAll(functionLine(true, read, currentFun))//) return false
        functionLines(true, read, currentFun, commands)//) return false
        if (!isToken("rbracket")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
        if(willExec && read) currentFun.miniScanner.input.add(currentToken)
        nextToken()
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
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        var currentFun = Function(fname, argName)
        functions.add(currentFun)
        functionLine(willExec, true, currentFun)//) return false
        functionLines(willExec, true, currentFun, mutableListOf())//) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun functionLine(willExec: Boolean, read: Boolean, currentFun: Function): MutableList<Command>{
        var commands = mutableListOf<Command>()
        if (isToken("for")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            commands.addAll(fForProduction(willExec, read, currentFun))
            return commands
        }
        if (isToken("if")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            commands.addAll(fIfProduction(willExec, read, currentFun))
            return commands
        }
        if (isToken("let")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if(!isToken("variable")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)

            //if(willExec) functions[fname]?.add(currentToken)
            var name = currentToken.lexem
            nextToken()
            cdeclaration(name, willExec, read, currentFun)
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            return commands
        }
        if (isToken("box")) {
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            commands.add(box(willExec, read, currentFun))
            //box(willExec, read, currentFun)
            return commands//box()
        }
        if (isToken("variable")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            var name = currentToken.lexem
            nextToken()
            variableopt(name, willExec, read, currentFun)
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            return commands
        }
        if (isToken("line")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            commands.add(line(willExec, read, currentFun))//line()
            return commands//line()
        }
        if (isToken("bend")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            commands.add(bend(willExec, read, currentFun))
            //bend()
            return commands //bend()
        }
        if (isToken("call")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if(!isToken("fname")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if(!isToken("lparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if(!isToken("variable"))throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if (!isToken("rparen")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if(!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            return commands
        }
        if (isToken("return")){
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            if (!isToken("semi")) throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
            if(willExec && read) currentFun.miniScanner.input.add(currentToken)
            nextToken()
            return commands
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")

    }

    fun functionLines(willExec: Boolean, read: Boolean, currentFun: Function, commands: MutableList<Command>): MutableList<Command>{
        var newcommands = functionLine(willExec, read, currentFun)
        if(newcommands.isEmpty()) return commands
        else{
            commands.addAll(newcommands)
            return functionLines(willExec, read, currentFun, commands)
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
            cdeclaration(name, willExec, false, null)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            var name = currentToken.lexem
            nextToken()
            variableopt(name, willExec, false, null)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) return false
            nextToken()
            if(!isToken("lparen")) return false
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if (!isToken("rparen"))return false
            nextToken()
            if(!isToken("semi"))return false
            nextToken()
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
            cdeclaration(name, willExec, false, null)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            nextToken()
            var name = currentToken.lexem
            variableopt(name, willExec, false, null)//) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) return false
            nextToken()
            if(!isToken("lparen")) return false
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if (!isToken("rparen"))return false
            nextToken()
            if(!isToken("semi"))return false
            nextToken()
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
            cdeclaration(name, willExec, false, null)
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            var name = currentToken.lexem
            nextToken()
            variableopt(name, willExec, false, null) //return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("call")){
            nextToken()
            if(!isToken("fname")) return false
            nextToken()
            if(!isToken("lparen")) return false
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if (!isToken("rparen"))return false
            nextToken()
            if(!isToken("semi"))return false
            nextToken()
            return true
        }
        return false

    }

    fun buildingCommand(building: Building, willExec: Boolean): Boolean{
        if (isToken("box")){
            nextToken()
            building.commands.add(box(willExec, false, null))
            return true
        }
        if (isToken("line")){
            nextToken()
            building.commands.add(line(willExec, false, null))
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
            road.commands.add(bend(willExec, false, null))
            return true
        }
        if (isToken("line")){
            nextToken()
            road.commands.add(line(willExec, false, null))
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
        var point = point(willExec, false, null)
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
        return city
    }






    fun parse(): City {
        var value = code()

        if (scanner.eof() && !isToken("rparen")) return value
        else throw Exception("Unexpected token ${currentToken.tokenMap[currentToken.token]} at ${currentToken.column} ${currentToken.row}  lex val ${currentToken.lexem}")
    }


}