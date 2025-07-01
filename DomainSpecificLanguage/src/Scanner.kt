import java.io.File
import java.io.FileReader
import java.io.PushbackReader
import java.io.StringReader



class Scanner(private val input: File) {
    private val content = input.readText()
    private var reader = PushbackReader(FileReader(input))
    private var lastToken: Token? = null
    var row = 1
    var column = 1
    //mark za for loop
    private var charsRead = 0
    private var markCharsRead = 0
    private var markRow = 1
    private var markColumn = 1
    private val maxState = 110
    private val startState = 0
    private val noEdge = -1

    private val automata = Array(maxState + 1) { IntArray(256) { noEdge } }
    private val finite = IntArray(maxState + 1) {noEdge}

    val tLexError: Int = -1
    val tIgnore: Int = 0
    val tVariable: Int = 1
    var tFname: Int = 2
    val tPlus: Int = 3
    var tMinus: Int = 4
    var tDivide: Int = 5
    var tTimes: Int = 6
    var tAssign: Int = 7
    var tGreaterthan: Int = 8
    var tLessthan: Int = 9
    var tGreaterthanorequal: Int = 10
    var tLessthanorequal: Int = 11
    val tInteger: Int = 12
    val tDouble: Int = 13
    var tLparen: Int = 14
    var tRparen: Int = 15
    var tRbracket: Int = 16
    var tLbracket: Int = 17
    var tSemi: Int = 18
    var tComma: Int = 19
    var tName: Int = 22
    var tRoad: Int = 26
    var tReturn: Int = 31
    var tBend: Int = 35
    var tBuilding: Int = 42
    var tBox: Int = 44
    var tLine: Int = 48
    var tIf: Int = 50
    var tFor: Int = 53
    var tFun: Int = 55
    var tCity: Int = 59
    var tNeigh: Int = 64
    var tNull: Int = 67
    var tElse: Int = 71
    var tLet: Int = 73
    var tHighlight: Int = 83
    var tStore: Int = 88
    var tUser: Int = 92
    var tCall: Int = 95
    var tTo: Int = 97
    var tPoint: Int = 102
    var tEqual: Int = 103

    val keywordPrefixStates = listOf(
        23, 24, 25,        // "r", "ro", "roa" → road
        27, 28, 29, 30,    // "re", "ret", "retu", "retur" → return
        32, 33, 34,        // "b", "be", "ben" → bend
        36, 37, 38, 39, 40, 41, // "bu", "bui", ..., "buildin" → building
        43,                // "bo" → box
        45, 46, 47,        // "l", "li", "lin" → line
        49,                // "i" → if
        51, 52,            // "f", "fo" → for
        54,                // "fu" → fun
        56, 57, 58,        // "c", "ci", "cit" → city
        60, 61, 62, 63,    // "n", "ne", "nei", "neig" → neigh
        65, 66,            // "nu", "nul" → null
        68, 69, 70,        // "e", "el", "els" → else
        72,                // "le" → let
        74, 75, 76, 77, 78, 79, 80, 81, 82, // "highlight" → .h, .hi, ...
        84, 85, 86, 87,    // "s", "st", "sto", "stor" → store
        89, 90, 91,        // "u", "us", "use" → user
        93, 94,            // "ca", "cal" → call
        96,                // "t" → to
        98, 99, 100, 101   // "p", "po", "poi", "poin" → point
    )



    init {
        for (i in 0 until maxState + 1) {
            for (j in 0 until 256) {
                automata[i][j] = noEdge
            }
        }
        for (i in '0'.code until '9'.code + 1) {
            automata[0][i] = 12 //int
            automata[1][i] = 1 //var
            automata[12][i] = 12 //int
            automata[13][i] = 13 //double
            automata[21][i] = 21 // "n9ame"
            automata[105][i] = 13 //3,
            for (j in 23..73){
                automata[j][i]=1 //road, bend,...
            }
            for (j in 84..102){
                automata[j][i]=1 //road, bend... pt2
            }

        }
        /*for (i in 'A'.code until 'z'.code + 1) {
            automata[0][i]= 2
            automata[2][i]=2
            if(i <= 'F'.code || i >= 'a'.code && i <= 'f'.code){
                automata[3][i]=4
                automata[4][i]=4
            }

        }*/


        for (i in 'A'.code..'Z'.code) {
            automata[0][i] = 2 // fname
            automata[2][i] = 2 // fname
            automata[1][i] = 1 // var
            automata[20][i] = 21 // "Name"
            automata[21][i] = 21 // "nAme"
            for (j in 23..73){
                automata[j][i]=1 //road, bend,...
            }
            for (j in 84..102){
                automata[j][i]=1 //road, bend... pt2
            }
        }

        for (i in 'a'.code..'z'.code) {
            automata[0][i] = 1 // var
            automata[2][i] = 2 // fname
            automata[1][i] = 1 // var
            automata[20][i] = 21 // "name"
            automata[21][i] = 21 // "name"
            for (j in 23..73){
                automata[j][i]=1 //road, bend,...
            }
            for (j in 84..102){
                automata[j][i]=1 //road, bend... pt2
            }
        }

        /*road*/
        //roa =
        automata[0]['r'.code]=23
        automata[23]['o'.code]=24
        automata[24]['a'.code]=25
        automata[25]['d'.code]=26
        /*return*/
        automata[23]['e'.code]=27
        automata[27]['t'.code]=28
        automata[28]['u'.code]=29
        automata[29]['r'.code]=30
        automata[30]['n'.code]=31
        /*bend*/
        automata[0]['b'.code]=32
        automata[32]['e'.code]=33
        automata[33]['n'.code]=34
        automata[34]['d'.code]=35
        /*building*/
        automata[32]['u'.code]=36
        automata[36]['i'.code]=37
        automata[37]['l'.code]=38
        automata[38]['d'.code]=39
        automata[39]['i'.code]=40
        automata[40]['n'.code]=41
        automata[41]['g'.code]=42
        /*box*/
        automata[32]['o'.code]=43
        automata[43]['x'.code]=44
        /*line*/
        automata[0]['l'.code]=45
        automata[45]['i'.code]=46
        automata[46]['n'.code]=47
        automata[47]['e'.code]=48
        /*if*/
        automata[0]['i'.code]=49
        automata[49]['f'.code]=50
        /*for*/
        automata[0]['f'.code]=51
        automata[51]['o'.code]=52
        automata[52]['r'.code]=53
        /*fun*/
        automata[51]['u'.code]=54
        automata[54]['n'.code]=55
        /*city*/
        automata[0]['c'.code]=56
        automata[56]['i'.code]=57
        automata[57]['t'.code]=58
        automata[58]['y'.code]=59
        /*neigh*/
        automata[0]['n'.code]=60
        automata[60]['e'.code]=61
        automata[61]['i'.code]=62
        automata[62]['g'.code]=63
        automata[63]['h'.code]=64
        /*null*/
        automata[60]['u'.code]=65
        automata[65]['l'.code]=66
        automata[66]['l'.code]=67
        /*else*/
        automata[0]['e'.code]=68
        automata[68]['l'.code]=69
        automata[69]['s'.code]=70
        automata[70]['e'.code]=71
        /*let*/
        automata[45]['e'.code]=72
        automata[72]['t'.code]=73
        /*.highlight*/
        automata[0]['.'.code]=74
        automata[74]['h'.code]=75
        automata[75]['i'.code]=76
        automata[76]['g'.code]=77
        automata[77]['h'.code]=78
        automata[78]['l'.code]=79
        automata[79]['i'.code]=80
        automata[80]['g'.code]=81
        automata[81]['h'.code]=82
        automata[82]['t'.code]=83
        /*store*/
        automata[0]['s'.code]=84
        automata[84]['t'.code]=85
        automata[85]['o'.code]=86
        automata[86]['r'.code]=87
        automata[87]['e'.code]=88
        /*user*/
        automata[0]['u'.code]=89
        automata[89]['s'.code]=90
        automata[90]['e'.code]=91
        automata[91]['r'.code]=92
        /*call*/
        automata[56]['a'.code]=93
        automata[93]['l'.code]=94
        automata[94]['l'.code]=95
        /*to*/
        automata[0]['t'.code]=96
        automata[96]['o'.code]=97
        /*point*/
        automata[0]['p'.code]=98
        automata[98]['o'.code]=99
        automata[99]['i'.code]=100
        automata[100]['n'.code]=101
        automata[101]['t'.code]=102


        automata[0]['+'.code] = 3 // +
        automata[0]['-'.code] = 4 // -
        automata[0]['/'.code] = 5 // /
        automata[0]['*'.code] = 6 // *
        automata[0]['='.code] = 7 // =
        automata[7]['='.code] = 103 // ==
        automata[0]['>'.code] = 8 // >
        automata[8]['='.code] = 10 // >=
        automata[0]['<'.code] = 9 // <
        automata[9]['='.code] = 11 // <=
        automata[0]['('.code] = 14 // (
        automata[0][')'.code] = 15 // )
        automata[0]['}'.code]= 16 // }
        automata[0]['{'.code]= 17 // {
        automata[0][';'.code] = 18 // ;
        automata[12]['.'.code] = 105 // 1.3
        automata[0][','.code] = 19 // ,
        automata[0]['"'.code] = 20 // "
        automata[21]['"'.code] = 22 // "name"
        automata[0]['\t'.code] = 104
        automata[0]['\n'.code] = 104
        automata[0]['\r'.code] = 104
        automata[0][' '.code] = 104

        automata[104]['\t'.code] = 104
        automata[104]['\n'.code] = 104
        automata[104]['\r'.code] = 104
        automata[104][' '.code] = 104








        finite[0] = tLexError
        finite[1] = tVariable
        finite[2] = tFname
        finite[3] = tPlus
        finite[4] = tMinus
        finite[5] = tDivide
        finite[6] = tTimes
        finite[7] = tAssign
        finite[8] = tGreaterthan
        finite[9] = tLessthan
        finite[10] = tGreaterthanorequal
        finite[11] = tLessthanorequal
        finite[12] = tInteger
        finite[13] = tDouble
        finite[14] = tLparen
        finite[15] = tRparen
        finite[16] = tRbracket
        finite[17] = tLbracket
        finite[18] = tSemi
        finite[19] = tComma
        finite[22] = tName
        finite[26] = tRoad
        finite[31] = tReturn
        finite[35] = tBend
        finite[42] = tBuilding
        finite[44] = tBox
        finite[48] = tLine
        finite[50] = tIf
        finite[53] = tFor
        finite[55] = tFun
        finite[59] = tCity
        finite[64] = tNeigh
        finite[67] = tNull
        finite[71] = tElse
        finite[73] = tLet
        finite[83] = tHighlight
        finite[88] = tStore
        finite[92] = tUser
        finite[95] = tCall
        finite[97] = tTo
        finite[102] = tPoint
        finite[103] = tEqual
        finite[104] = tIgnore





    }

    fun getNextState(state: Int, char: Int): Int {
        if (char == -1 || char == '\uFFFF'.code) return noEdge
        return automata[state][char]
    }

    fun isFiniteState(state: Int): Boolean = finite[state] != tLexError

    fun isPrefixState(state: Int): Boolean = state in keywordPrefixStates

    fun getFiniteState(state: Int) = finite[state]


    fun nextToken(): Token {
        lastToken = nextTokenImp()
        return lastToken!!
    }

    fun currentToken(): Token? = lastToken

    fun read(): Int {
        val temp = reader.read()
        if (temp == -1) return temp
        column++
        charsRead++
        if (temp == '\n'.code) {
            row++;
            column = 1
        }
        return temp

    }

    fun peek(): Int {
        val nextChar = reader.read()
        if (nextChar != -1 && nextChar != '\uFFFF'.code) reader.unread(nextChar)
        return if (nextChar == '\uFFFF'.code) -1 else nextChar
    }

    fun eof(): Boolean = peek() == -1

    fun markPosition(){
        markCharsRead = charsRead
        markRow = row
        markColumn = column
    }



    fun resetToMark(markCharsRead: Int, markRow: Int, markColumn: Int) {
            val remaining = content.substring(markCharsRead)
            reader = PushbackReader(StringReader(remaining))
            charsRead = markCharsRead
            row = markRow
            column = markColumn
            lastToken = null

    }

    fun markFunPosition(): MutableList<Int>{
        var list: MutableList<Int> = mutableListOf()
        list.add(charsRead)
        list.add(row)
        list.add(column)
        return list
    }

    fun nextTokenImp(): Token {
        var currentState = startState
        var lexem = StringBuilder()
        var startColumn = column
        var startRow = row
        while (true) {
            var nextChar = peek().toChar()
            var nextState = getNextState(currentState, nextChar.code)

            if (nextState != noEdge) {
                currentState = nextState
                lexem.append(read().toChar())

            } else {
                if (isFiniteState(currentState)) {

                    var token = Token(lexem.toString(), startColumn, startRow, getFiniteState(currentState), eof())
                    if (token.token == tIgnore) return nextToken()
                    else return token
                }
                if (isPrefixState(currentState)) {
                    val token = Token(lexem.toString(), startColumn, startRow, tVariable, eof())
                    return token
                }

                throw Error("Invalid token at $startRow:$startColumn, lexeme so far: '$lexem'")
            }
        }
    }

}

