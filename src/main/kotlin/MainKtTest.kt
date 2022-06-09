package main.kotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

val testEnv : Map<String, Value> = mapOf(
    "x" to NumV(8)
) + topEnv

class Tests {


    @Test
    fun basicTests() {
        assertEquals("4.0", NumC(4)
            .interp(testEnv).serialize())

        val x = AppC(LamC(listOf("x"),
            AppC(IdC("+"), listOf(IdC("x"), NumC(1)))),
            listOf(NumC(8)))
        assertEquals("9.0", x.interp(topEnv).serialize())
    }

    @Test
    fun binaryOperations() {
        assertEquals(parse(listOf("+", 1, 2))
            .interp(topEnv).serialize(), "3.0")
        assertEquals(parse(listOf("-", 3, 0.5))
            .interp(topEnv).serialize(), "2.5")
        assertEquals(parse(listOf("*", 2, 3))
            .interp(topEnv).serialize(), "6.0")
        assertEquals(parse(listOf("/", 8, 2))
            .interp(topEnv).serialize(), "4.0")

        assertEquals(parse(listOf("<=", 2, 8))
            .interp(topEnv).serialize(), "true")
        assertEquals(parse(listOf("<=", 8, 2))
            .interp(topEnv).serialize(), "false")
        assertEquals(parse(listOf("equal?", 2, 2))
            .interp(topEnv).serialize(), "true")
        assertEquals(parse(listOf("equal?", 1, 2))
            .interp(topEnv).serialize(), "false")

    }

    @Test
    fun parseTest() {
        assertTrue(isLambda(listOf("=>", 1)))
        assertEquals(parse(listOf("if", true, 2.0, 1.0)).toString(),
            "IfC(BoolC(true), NumC(2.0), NumC(1.0))")
        assertEquals(parse(listOf("+", 2.0, 1.0)).toString(),
            "AppC(IdC(+), [NumC(2.0), NumC(1.0)])")
    }

    @Test
    fun complexProgramTests() {
        assertEquals(parse(listOf("+", 2.0, 1.0))
            .interp(testEnv).serialize(), "3.0")
        //(top-interp '(+ 2 1))

        assertEquals(parse(listOf(listOf("+", "-", "=>",
            listOf("+", 3, listOf("-", 6, 4))), "-", "+"))
            .interp(topEnv).toString(), "-7.0")
        //(top-interp '(var ((+ = -) (- = +)) in (+ 3 (- 6 4))))

        assertEquals(parse(
            listOf(listOf( "a", "b", "c", "=>",
                listOf("+", "a", listOf(listOf("x", "y", "=>",
                    listOf("*", "x", "y")), 3, 3 ))), 2, 4, 5))
            .interp(topEnv).serialize(), "11.0")
        //(top-interp '((a b c => (+ a ((x y => (* x y)) 3 3))) 2 4 5))

        assertEquals(parse(
            listOf(listOf(listOf(listOf("x", "=>", listOf("y", "=>",
                listOf("z", "=>", listOf("+", "x", listOf("+", "y", "z"))))), 2), 3), 4)
        ).interp(topEnv).serialize(), "9.0")
        //(top-interp '((((x => (y => (z => (+ x (+ y z))))) 2) 3) 4))
    }

}