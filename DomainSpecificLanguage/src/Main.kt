import java.io.File

fun main() {



    val file = File("C:\\Users\\djela\\Documents\\RIT\\PVPJ\\projekt proba\\proba\\tekst.txt")
    if (!file.exists()) {
        println("Error: File not found!")
        return
    }

    /*val exp1 = plus(times(int(1), int(2)), int(3))
    val exp2 = times(int(1),plus(int(2),hex("#F1")))
    val exp3 = minus(minus(int(4),int(1)),int(1))
    val exp4 = divides(int(4),plus(variable("abc"),int(1)))
    val exp5 = bwand(plus(variable("x"),variable("y")),bwor(int(2),int(2)))
    val exp6 = plus(int(1),int(2))


   val expressions: MutableList<String> = mutableListOf<String>()
    expressions.add(exp1.toString())
    expressions.add(exp2.toString())
    expressions.add(exp3.toString())
    expressions.add(exp4.toString())
    expressions.add(exp5.toString())
    expressions.add(exp6.toString())

    for (i in expressions) {
        file.printWriter().use { out ->
            out.print(i)
        }
        val scanner = Scanner(file)
        val parser = Parser(scanner)

        try {
            println(parser.parse())
        } catch (e: Error) {
            println(e.message)
        }
    }


    /*TO XML*/
    val fileXML = File("expr.xml")
    if (!fileXML.exists()) {
        println("Error: File not found!")
        return
    }
    fileXML.printWriter().use { out ->
        val test1: fullExpr = fullExpr(divides(int(4), plus(variable("abc"), int(1))))
        out.print(test1.toXML())
    }


    print("Ime datoteke: ")
    val fileName = readln()
    val file = File("C:\\Users\\djela\\Documents\\RIT\\PVPJ\\Vaja2\\$fileName")

    if (!file.exists()) {
        println("Error: File not found!")
        return
    }
*/



    val scanner = Scanner(file)

    val parser = Parser(scanner)

    try {
        println(parser.parse())
    }catch (e:Exception){
        println(e.message)
    }
    /*
    println("Tokens:")
    try {
        while (!scanner.eof()) {
            val token = scanner.nextToken()
            if (token.token != -1) {
                println(token)
            }
        }
    } catch (e: Error) {
        println(e.message)
    }
    */

}