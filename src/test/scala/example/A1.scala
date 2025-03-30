package example

import shapeless_annotation.ShapelessGeneric

@ShapelessGeneric
case class A1(x: Int, y: String, z: Boolean)

@ShapelessGeneric("anotherName")
case class A2(x1: A1, x2: Long)

object A2
