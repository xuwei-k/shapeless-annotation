package example

import shapeless_annotation.ShapelessGeneric
import shapeless_annotation.ShapelessLabelledGeneric

@ShapelessGeneric
case class A1(x: Int, y: String, z: Boolean)

@ShapelessGeneric("anotherName")
case class A2(x1: A1, x2: Long)

object A2

@ShapelessLabelledGeneric
case class A3(x1: Int, x2: String, x3: Long)
