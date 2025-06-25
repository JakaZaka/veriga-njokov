class Token(var lexem: String, var column: Int, var row: Int, var token: Int, var eof: Boolean) {
    var tokenMap = hashMapOf(


        1 to "variable",
        2 to "fname",
        3 to "plus",
        4 to "minus",
        5 to "divide",
        6 to "times",
        7 to "assign",
        8 to "greaterThan",
        9 to "lessThan",
        10 to "greaterThanOrEqual",
        11 to "lessThanOrEqual",
        12 to "int",
        13 to "double",
        14 to "lparen",
        15 to "rparen",
        16 to "rbracket",
        17 to "lbracket",
        18 to "semi",
        19 to "comma",
        22 to "name",
        26 to "road",
        31 to "return",
        35 to "bend",
        42 to "building",
        44 to "box",
        48 to "line",
        50 to "if",
        53 to "for",
        55 to "fun",
        59 to "city",
        64 to "neigh",
        67 to "null",
        71 to "else",
        73 to "let",
        83 to "highlight",
        88 to "store",
        92 to "user",
        95 to "call",
        97 to "to",
        102 to "point",
        103 to "equal",
        104 to "eof"
    )

    override fun toString(): String {
        return "${tokenMap[token]} (\"$lexem\")"
    }
}