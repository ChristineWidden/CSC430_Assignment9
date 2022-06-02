package main.kotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

val testEnv : Map<String, Value> = mapOf(
    "x" to NumV(8)
) + topEnv

class Tests {



    @Test
    fun x() {
        assertEquals("4.0", NumC(4).interp(testEnv).serialize())

        val x = AppC(LamC(listOf("x"), AppC(IdC("+"), listOf(IdC("x"), NumC(1)))), listOf(NumC(8)))
        assertEquals("9.0", x.interp(topEnv).serialize())
    }

}