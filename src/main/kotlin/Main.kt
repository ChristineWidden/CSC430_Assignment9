package main.kotlin


interface ExprC {
    fun interp() : Value
}
class NumC(val n : Double) : ExprC {
    override fun interp(): Value {
        return NumV(n)
    }
}

class StrC(val s : String) : ExprC {
    override fun interp(): Value {
        return StrV(s)
    }
}

class BoolC(val b : Boolean) : ExprC {
    override fun interp(): Value {
        return BoolV(b)
    }
}

class IfC(val ifCond : ExprC, val ifT: ExprC, val ifF: ExprC) : ExprC {
    override fun interp(): Value {
        val res : Value = ifCond.interp()
        if (res is BoolV) {
            val b : Boolean = res.b;
            return if (b) {
                ifT.interp();
            } else {
                ifF.interp();
            }
        } else {
            throw error("condition is not boolean");
        }
    }
}
class IdC(val id : String) : ExprC {
    override fun interp(): Value {
        TODO("Not yet implemented")
    }
}

class LamC(val args : List<String>, val body : ExprC) : ExprC {
    override fun interp(): Value {
        TODO("Not yet implemented")
    }
}

class AppC(val func : ExprC, val args : List<ExprC>) : ExprC {
    override fun interp(): Value {
        TODO("Not yet implemented")
    }
}


interface Value {
    fun serialize() : String
}
class NumV(val n: Double) : Value {
    override fun serialize(): String {
        return n.toString();
    }
    override fun toString(): String {
        return this.serialize();
    }
}
class BoolV(val b : Boolean) : Value {
    override fun serialize(): String {
        return b.toString()
    }
    override fun toString(): String {
        return this.serialize();
    }
}
class StrV(val s : String) : Value {
    override fun serialize(): String {
        return "\"" + s + "\""
    }
    override fun toString(): String {
        return this.serialize();
    }
}
class PrimV() : Value {
    override fun serialize(): String {
        return "#<primop>"
    }
    override fun toString(): String {
        return this.serialize();
    }
}
class ClosV(val args : List<String>, val body : ExprC, val env : List<Binding>) : Value {
    override fun serialize(): String {
        return "#<procedure>"
    }
    override fun toString(): String {
        return this.serialize();
    }
}

open class Binding(val name : String, val v : Value)

val topEnv: List<Binding> = mutableListOf(
    Binding("+", PrimV()),
    Binding("-", PrimV()),
    Binding("*", PrimV()),
    Binding("/", PrimV()),
    Binding("<=", PrimV()),
    Binding("equal?", PrimV()),
    Binding("true", PrimV()),
    Binding("false", PrimV()),
    Binding("error", PrimV())
)


fun main(args: Array<String>) {


    println("Hello World!")

    val v = NumV(5.0);
    println(v)

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}
