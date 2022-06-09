package main.kotlin

/**
 * The base type for an expression.
 * An expression constitutes a number, a boolean,
 * an if statement, a variable or function name,
 * a lambda, or a function call.
 */
interface ExprC {

    /**
     * Interprets the given expression
     * using the given environment, in order
     * to evaluate what Value the expression
     * represents.
     */
    fun interp(env : Map<String, Value>) : Value
}
class NumC(private val n : Double) : ExprC {
    constructor(m: Int) : this(m.toDouble())
    override fun interp(env: Map<String, Value>): Value {
        return NumV(n)
    }

    override fun toString(): String {
        return "NumC($n)"
    }
}

/**
 * A boolean expression.
 */
class BoolC(private val b : Boolean) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        return BoolV(b)
    }

    override fun toString(): String {
        return "BoolC($b)"
    }
}

/**
 * An if expression.
 */
class IfC(private val ifCond : ExprC, private val ifT: ExprC, private val ifF: ExprC) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        val res : Value = ifCond.interp(env)
        if (res is BoolV) {
            val b : Boolean = res.b;
            return if (b) {
                ifT.interp(env)
            } else {
                ifF.interp(env)
            }
        } else {
            throw error("condition is not boolean");
        }
    }

    override fun toString(): String {
        return "IfC($ifCond, $ifT, $ifF)"
    }
}

/**
 * A function or variable name expression.
 */
class IdC(private val id : String) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        val result = env[id]
        if (result == null) {
            error("id $id not found in environment")
        } else {
            return result;
        }
    }

    override fun toString(): String {
        return "IdC($id)"
    }
}

/**
 * A lambda expression.
 */
class LamC(private val args : List<String>, private val body : ExprC) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        return ClosV(args, body, env)
    }

    override fun toString(): String {
        return "LamC($args, $body)"
    }
}

/**
 * A function call expression.
 */
class AppC(val func : ExprC, val args : List<ExprC>) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        val application : Value = func.interp(env)
        return if (application is PrimV) {
            evalPrimV((application as PrimV).s, args.map {it.interp(env)})
        } else if (application is ClosV) {
            val closApp = application as ClosV
            val interpedArgs : List<Value> = args.map {it.interp(env)}
            val newEnv : Map<String, Value> = addToEnv(closApp.env, closApp.args, interpedArgs)
            closApp.body.interp(newEnv)
        } else {
            error("incorrect AppC format")
        }
    }

    override fun toString(): String {
        return "AppC($func, $args)"
    }


}

/**
 * The base type for a value. Expressions are evaluated into values
 * until eventually only one value remains: the result of the program.
 */
interface Value {
    fun serialize() : String
}

/**
 * The number value, effectively a Double.
 */
class NumV(val n: Double) : Value {
    constructor(m: Int) : this(m.toDouble())
    override fun serialize(): String {
        return n.toString()
    }
    override fun toString(): String {
        return this.serialize()
    }
}

/**
 * The boolean value.
 */
class BoolV(val b : Boolean) : Value {
    override fun serialize(): String {
        return b.toString()
    }
    override fun toString(): String {
        return this.serialize()
    }
}

/**
 * The value for a primitive operation.
 */
class PrimV(val s : String) : Value {
    override fun serialize(): String {
        return "#<primop>"
    }
    override fun toString(): String {
        return this.serialize()
    }
}

/**
 * The value for a lambda expression
 */
class ClosV(val args : List<String>, val body : ExprC, val env : Map<String, Value>) : Value {
    override fun serialize(): String {
        return "#<procedure>"
    }
    override fun toString(): String {
        return this.serialize()
    }
}

/**
 * The top-level environment.
 * The environment indicates what
 * symbols translate into what functions
 * or other values.
 */
val topEnv: Map<String, Value> = mapOf(
    "+" to PrimV("+"),
    "-" to PrimV("-"),
    "*" to PrimV("*"),
    "/" to PrimV("/"),
    "<=" to PrimV("<="),
    "equal?" to PrimV("equal?"),
    "true" to PrimV("true"),
    "false" to PrimV("false"),
    "error" to PrimV("error")
)

/**
 * Adds a list of symbols and their corresponding values to the environment.
 * This allows the user to define names of parameters.
 */
fun addToEnv(env : Map<String, Value>, symbols : List<String>, values : List<Value>) : Map<String, Value> {
    if (symbols.size > values.size) error("no values to add")
    if (symbols.size < values.size) error("no symbols to add")

    var newEnv = env.toMap()
    for (i in values.indices) {
        newEnv = newEnv + mapOf<String, Value>(symbols[i] to values[i])
    }
    return newEnv
}

/**
 * Given a primitive operation and its arguments,
 * evaluates the result.
 */
fun evalPrimV(s : String, args : List<Value>) : Value {
    if ((args[0] is NumV) and (args[1] is NumV)) {
        when (s) {
            "+" -> return NumV((args[0] as NumV).n + (args[1] as NumV).n)
            "-" -> return NumV((args[0] as NumV).n - (args[1] as NumV).n)
            "*" -> return NumV((args[0] as NumV).n * (args[1] as NumV).n)
            "/" -> {
                val div = (args[1] as NumV).n
                if (div == 0.0) {
                    error("divide by zero error")
                } else {
                    return NumV((args[0] as NumV).n / (args[1] as NumV).n)
                }
            }
            "<=" -> return BoolV((args[0] as NumV).n <= (args[1] as NumV).n)
            "equal?" -> return BoolV((args[0] as NumV).n == (args[1] as NumV).n)
        }
    } else if (args.size == 2) {
        if (s == "equal?") {
            return BoolV(args[0].equals(args[1]))
        } else {
            error("no primitive match")
        }
    } else {
        if (s == "error") {
            error("user error: $s")
        } else {
            error("no primitive match")
        }
    }
    error("should not get here")
}

/**
 * Checks if a list can be parsed into an if expression
 */
fun isIf(sexp : List<*>) : Boolean{
    return (sexp.size == 4) && sexp[0] == "if"
}

/**
 * Checks if a list can be parsed into a lambda expression
 */
fun isLambda(sexp : List<*>) : Boolean{
    return  sexp[(sexp.size - 2)] == "=>"
}

/**
 * Takes a list and parses it into a series of expressions,
 * which can then be interpreted into a result value.
 */
fun parse (sexp : Any) : ExprC {

    when (sexp){
        is Int -> return NumC(sexp)
        is Double -> return NumC(sexp)
        is String -> {
            return IdC(sexp)
        }
        is Boolean -> return BoolC(sexp)
        is List<*> -> {
            //if(isBinaryOp(sexp)){
            //    return AppC(parse((sexp[0] as Any)), listOf(parse(sexp[1] as Any), parse(sexp[2] as Any)) )
            //}
            //else
            if (isIf(sexp)){
                return IfC(parse((sexp[1] as Any)), parse((sexp[2] as Any)), parse((sexp[3] as Any)))
            }
            else if (isLambda(sexp)){
                return LamC((sexp.subList(0, (sexp.size - 2)) as List<String>) , parse((sexp[(sexp.size - 1)]) as Any))
            }
            else{
                val parsedFun = parse((sexp[0] as Any))

                val args = sexp.subList(1, sexp.size)

                var parsedArgs : List<ExprC> = listOf()

                for (s: Any in (args as List<Any>))
                {
                    parsedArgs = parsedArgs + parse (s as Any)
                }
                return AppC(parsedFun, parsedArgs)
            }

        }
        else -> {
            return NumC(1)
        }

    }
}



fun main(args: Array<String>) {

    val testEnv : Map<String, Value> = mapOf(
        "x" to NumV(8)
    ) + topEnv

    val v = NumV(5.0)
    println(v)

    val w = IfC(BoolC(true), NumC(1), NumC(2)).interp(testEnv)
    val x = IfC(BoolC(false), NumC(1), NumC(2)).interp(testEnv)
    println(w)
    println(x)

}

