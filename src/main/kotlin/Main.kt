package main.kotlin

import java.util.DoubleSummaryStatistics


interface ExprC {
    fun interp(env : Map<String, Value>) : Value
}
class NumC(private val n : Double) : ExprC {
    constructor(m: Int) : this(m.toDouble())
    override fun interp(env: Map<String, Value>): Value {
        return NumV(n)
    }
}

class StrC(private val s : String) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        return StrV(s)
    }
}

class BoolC(private val b : Boolean) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        return BoolV(b)
    }
}

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
}
class IdC(private val id : String) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        val result = env[id]
        if (result == null) {
            error("id $id not found in environment")
        } else {
            return result;
        }
    }
}

class LamC(private val args : List<String>, private val body : ExprC) : ExprC {
    override fun interp(env: Map<String, Value>): Value {
        return ClosV(args, body, env)
    }
}

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
}


interface Value {
    fun serialize() : String
}
class NumV(val n: Double) : Value {
    constructor(m: Int) : this(m.toDouble())
    override fun serialize(): String {
        return n.toString()
    }
    override fun toString(): String {
        return this.serialize()
    }
}
class BoolV(val b : Boolean) : Value {
    override fun serialize(): String {
        return b.toString()
    }
    override fun toString(): String {
        return this.serialize()
    }
}
class StrV(private val s : String) : Value {
    override fun serialize(): String {
        return "\"" + s + "\""
    }
    override fun toString(): String {
        return this.serialize()
    }
}
class PrimV(val s : String) : Value {
    override fun serialize(): String {
        return "#<primop>"
    }
    override fun toString(): String {
        return this.serialize()
    }
}
class ClosV(val args : List<String>, val body : ExprC, val env : Map<String, Value>) : Value {
    override fun serialize(): String {
        return "#<procedure>"
    }
    override fun toString(): String {
        return this.serialize()
    }
}

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

fun addToEnv(env : Map<String, Value>, symbols : List<String>, values : List<Value>) : Map<String, Value> {
    if (symbols.size > values.size) error("no values to add")
    if (symbols.size < values.size) error("no symbols to add")

    var newEnv = env.toMap()
    for (i in values.indices) {
        newEnv = newEnv + mapOf<String, Value>(symbols[i] to values[i])
    }
    return newEnv
}
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

fun main(args: Array<String>) {

    val testEnv : Map<String, Value> = mapOf(
        "x" to NumV(8)
    ) + topEnv

    println("Hello World!")

    val v = NumV(5.0)
    println(v)

    val w = IfC(BoolC(true), NumC(1), NumC(2)).interp(testEnv)
    val x = IfC(BoolC(false), NumC(1), NumC(2)).interp(testEnv)
    println(w)
    println(x)

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")


}

