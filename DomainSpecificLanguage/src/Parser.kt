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
    val variables: MutableMap<String, Float> = mutableMapOf(),
    val forRange: MutableMap<Int, Int> = mutableMapOf()

) {
    fun nextToken() {
        if (!scanner.eof()) currentToken = scanner.nextToken()
        else currentToken = Token("eof", scanner.column, scanner.row, 22, scanner.eof())
    }

    fun isToken(token: String): Boolean {
        return currentToken.tokenMap[currentToken.token].equals(token)
    }

    fun primary(): Float {
        if (isToken("int")) {
            var value: Float = currentToken.lexem.toFloat()
            nextToken()
            //println("primary ok, ${currentToken.tokenMap[currentToken.token]}")
            return value
        } else if (isToken("double")) {
            var value: Float = currentToken.lexem.toFloat()
            nextToken()
            return value

        }else if (isToken("variable")) {
            var value: Float = variables.getValue(currentToken.lexem)
            nextToken()
            return value
        } else if (isToken("lparen")) {
            nextToken()
            //val value: Int = bitwise()
            if (isToken("rparen")) {
                nextToken()
                return aditive()
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

    fun unary(): Float {
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

    fun secondMultiplicative(unary: Float): Float {
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

    fun multiplicative(): Float {
        //println("multiplicative ok, ${currentToken.tokenMap[currentToken.token]}")
        return secondMultiplicative(unary())
    }

    fun secondAditive(multiplicative: Float): Float {
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

    fun aditive(): Float {
        //println("aditive ok, ${currentToken.tokenMap[currentToken.token]}")
        return secondAditive(multiplicative())
    }



    fun expr(): Float {
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

    fun angle(): Float {
        if(isToken("int")){
            var value = currentToken.lexem.toFloat()
            nextToken()
            if(value < -180 || value > 180 ) throw Exception("The angle is not correct, it needs to be on the range from -180 to 180")
            return value
        }
        else if (isToken("double")){
            var value = currentToken.lexem.toFloat()
            nextToken()
            if(value < -180 || value > 180 ) throw Exception("The angle is not correct, it needs to be on the range from -180 to 180")
            return value
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun operator(): Operators {
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

    fun number(): Float{
        if (isToken("int")) {
            var value = currentToken.lexem.toFloat()
            nextToken()
            return value
        }
        else if (isToken("double")) {
            var value = currentToken.lexem.toFloat()
            nextToken()
            return value
        }
        throw Exception("Unexpected token: ${currentToken.tokenMap[currentToken.token]}")
    }

    fun point(): Boolean{
        if(!expr()) return false
        if (isToken("comma")) {
            nextToken()
            return expr()
        }
        //POINT
        return true


        /*
        * Point()
        *  x
        *   y
        *
        *
        * */
    }

    fun compare(): Boolean{
        if (!isToken("variable")) return false

        nextToken()
        if (!operator()) return false
        if (!number()) return false
        return true
    }

    fun line(): Boolean{
        if(!isToken("lparen")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!point()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("comma")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!point()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("semi")) return false
        nextToken()
        return true
    }

    fun bend(): Boolean{
        //BEND ::= ((POINT), (POINT), ANGLE);
        if(!isToken("lparen")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!point()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("comma")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!point()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("comma")) return false
        nextToken()
        if(!angle()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("semi")) return false
        nextToken()
        return true
    }

    fun box(): Boolean{
        if(!isToken("lparen")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!point()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("comma")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!point()) return false
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("rparen"))return false
        nextToken()
        if (!isToken("semi")) return false
        nextToken()
        return true


        /*
        *
        * Box()
        *   List<Point>
        *
        *
        *
        * */
    }

    fun circle(): Boolean {
        if (isToken("lparen")) {
            nextToken()
            if (!point()) return false
            if (!isToken("rparen")) return false
            nextToken()
            if (!isToken("comma")) return false
            nextToken()
           return number()
        }
        return false
    }

    fun cvalue(): Boolean{
        if (isToken("point")){
            nextToken()
            if (!isToken("lparen")) return false
            nextToken()
            if (!point()) return false
            if (!isToken("rparen")) return false
            nextToken()
            /*if (!isToken("semi")) return false
            nextToken()*/
            return true
        }
        else if (isToken("null")){
            nextToken()
            /*if (!isToken("semi")) return false
            nextToken()*/
            return true
        }
        else if(isToken("neigh")){
            nextToken()
            if (!isToken("lparen")) return false
            nextToken()
            if (!circle()) return false
            if (!isToken("rparen")) return false
            nextToken()/*
            if (!isToken("semi")) return false
            nextToken()*/
            return true
        }
        else{
            if(!expr()) return false
            /*if (!isToken("semi")) return false
            nextToken()*/
            return true
        }
    }

    fun value(): Boolean {
        if (isToken("point")){
            nextToken()
            if (!isToken("lparen")) return false
            nextToken()
            if (!point()) return false
            if (!isToken("rparen")) return false
            nextToken()

            return true
        }
        else if (isToken("null")){
            nextToken()
            /*if (!isToken("semi")) return false
            nextToken()*/
            return true
        }
        else{
            if(!expr()) return false
            /*if (!isToken("semi")) return false
            nextToken()*/
            return true
        }
    }

    fun declaration(): Boolean{
        if (isToken("assign")) {
            nextToken()
            return value()
        }
        return false
    }

    fun cdeclaration(): Boolean{
        if (isToken("assign")) {
            nextToken()
            return cvalue()
        }
        return false
    }

    fun variableopt(): Boolean{
        if (isToken("highlight")){
            nextToken()
            if(!isToken("lparen")) return false
            nextToken()
            if (!isToken("rparen")) return false
            nextToken()
            if (!isToken("semi")) return false
            nextToken()
            return true
        }
        if (!declaration()) return false
        if (!isToken("semi")) return false
        nextToken()
        return true
    }

    fun ifProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!compare()) return false
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!cityLine()) return false
        if (!cityLines()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return elseProduction()

    }

    fun bIfProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!compare()) return false
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!buildingCommand()) return false
        if (!buildingCommands()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return bElseProduction()
    }

    fun rIfProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!compare()) return false
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!roadCommand()) return false
        if (!roadCommands()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return rElseProduction()
    }

    fun fIfProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!compare()) return false
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!functionLine()) return false
        if (!functionLines()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return fElseProduction()
    }

    fun elseProduction(): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!cityLine()) return false
            if (!cityLines()) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun bElseProduction(): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!buildingCommand()) return false
            if (!buildingCommands()) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun rElseProduction(): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!roadCommand()) return false
            if (!roadCommands()) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun fElseProduction(): Boolean{
        if (isToken("else")) {
            nextToken()
            if (!isToken("lbracket")) return false
            nextToken()
            if (!functionLine()) return false
            if (!functionLines()) return false
            if (!isToken("rbracket")) return false
            nextToken()
        }
        return true
    }

    fun forProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        nextToken()
        if (!declaration()) return false
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!cityLine()) return false
        if (!cityLines()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun bForProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        nextToken()
        if (!declaration()) return false
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!buildingCommand()) return false
        if (!buildingCommands()) return false
        //BUILDINGCOMMAND BUILDINGCOMMANDS
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun rForProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        nextToken()
        if (!declaration()) return false
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!roadCommand()) return false
        if (!roadCommands()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun fForProduction(): Boolean{
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        nextToken()
        if (!declaration()) return false
        if (!isToken("to")) return false
        nextToken()
        if (!isToken("int")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!functionLine()) return false
        if (!functionLines()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun function(): Boolean{
        if (!isToken("fname")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if (!isToken("variable")) return false
        nextToken()
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("lbracket")) return false
        nextToken()
        if (!functionLine()) return false
        if (!functionLines()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }

    fun functionLine(): Boolean{
        if (isToken("for")){
            nextToken()
            return fForProduction()
        }
        if (isToken("if")){
            nextToken()
            return fIfProduction()
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if(!cdeclaration()) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }
        if (isToken("box")) {
            nextToken()
            return box()
        }
        if (isToken("variable")){
            nextToken()
            if(!variableopt()) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }
        if (isToken("line")){
            nextToken()
            return line()
        }
        if (isToken("bend")){
            nextToken()
            return bend()
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
        if (isToken("return")){
            nextToken()
            if (!isToken("semi")) return false
            nextToken()
            return true
        }
        return false

    }

    fun functionLines(): Boolean{
        if (functionLine()) return functionLines()
        return true
    }

    fun codeLine(): Boolean{
        if (isToken("for")){
            nextToken()
            return forProduction()
        }
        if (isToken("if")){
            nextToken()
            return ifProduction()
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if(!cdeclaration()) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            nextToken()
            if(!variableopt()) return false
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
            return function()
        }
        return false

    }

    fun bCodeLine(): Boolean{
        if (isToken("for")){
            nextToken()
            return bForProduction()
        }
        if (isToken("if")){
            nextToken()
            return bIfProduction()
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if(!cdeclaration()) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            nextToken()
            if(!variableopt()) return false
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

    fun rCodeLine(): Boolean{
        if (isToken("for")){
            nextToken()
            return rForProduction()
        }
        if (isToken("if")){
            nextToken()
            return rIfProduction()
        }
        if (isToken("let")){
            nextToken()
            if(!isToken("variable"))return false
            nextToken()
            if(!cdeclaration()) return false
            if (!isToken("semi"))return false
            nextToken()
            return true
        }

        if (isToken("variable")){
            nextToken()
            if(!variableopt()) return false
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

    fun buildingCommand(): Boolean{
        if (isToken("box")){
            nextToken()
            return box()
        }
        if (isToken("line")){
            nextToken()
            return line()
        }
        return bCodeLine()
    }

    fun buildingCommands(): Boolean{
        if(buildingCommand()) return buildingCommands()
        return true
    }

    fun roadCommand(): Boolean{
        if (isToken("bend")){
            nextToken()
            return bend()
        }
        if (isToken("line")){
            nextToken()
            return line()
        }
        return rCodeLine()
    }

    fun roadCommands(): Boolean{
        if(roadCommand()) return roadCommands()
        return true
    }

    fun road(): Boolean{
        if(!isToken("road")) return false
        nextToken()
        if(!name()) return false
        if (!isToken("lbracket")) return false
        nextToken()
        if(!roadCommand()) return false
        if(!roadCommands()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        if (!isToken("semi"))return false
        nextToken()
        return true
    }

    fun building(): Boolean{
        if(!isToken("building")) return false
        nextToken()
        //if(!name()) return false
        if (!isToken("lbracket")) return false
        nextToken()
        if(!buildingCommand()) return false
        if(!buildingCommands()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        if (!isToken("semi"))return false
        nextToken()
        return true
    }

    fun user (): Boolean{
        if (!isToken("user"))return false
        nextToken()
        if(!name()) return false
        if (!isToken("lbracket"))return false
        nextToken()
        if (!isToken("point")) return false
        nextToken()
        if (!isToken("lparen")) return false
        nextToken()
        if(!point()) return false
        if (!isToken("rparen")) return false
        nextToken()
        if (!isToken("semi")) return false
        nextToken()
        if (!isToken("rbracket")) return false
        nextToken()
        if (!isToken("semi")) return false
        nextToken()
        return true
    }

    fun store(): Boolean{
        if (!isToken("store"))return false
        nextToken()
        if(!name()) return false
        if (!isToken("lbracket")) return false
        nextToken()
        if (!building()) return false
        if (!isToken("rbracket")) return false
        nextToken()
        if (!isToken("semi")) return false
        nextToken()
        return true
    }

    fun block(): Boolean{
        if(building()) return true
        else if(road()) return true
        else if (user()) return true
        else if(store()) return true
        return false
    }

    fun cityLine(): Boolean{
        if(codeLine()) return true
        else if (block()) return true
        return false
    }

    fun cityLines(): Boolean{
        if (cityLine()){
            return cityLines()
        }
        return true
    }
    fun city(): Boolean{
        if (cityLine()){
            return cityLines()
        }
        return false
    }
    fun code(): Boolean{
        if(!isToken("city")) return false
        nextToken()
        if(!name()) return false
        if (!isToken("lbracket")) return false
        nextToken()
        if (!city())return false
        if (!isToken("rbracket")) return false
        nextToken()
        return true
    }






    fun parse(): Boolean {
        var value = code()

        if (scanner.eof() && !isToken("rparen")) return value
        else throw Exception("Unexpected token ${currentToken.tokenMap[currentToken.token]} at ${currentToken.column} ${currentToken.row}  lex val ${currentToken.lexem}")
    }


}