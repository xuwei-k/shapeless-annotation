package example

import shapeless_annotation.DefineType
import shapeless_annotation.DefineType.LazyVal
import shapeless_annotation.ShapelessGeneric
import shapeless_annotation.ShapelessLabelledGeneric

@ShapelessGeneric
case class A1(x: Int, y: String, z: Boolean)

@ShapelessGeneric(name = "anotherName", DefineType.Val)
case class A2(x1: A1, x2: Long)

object A2

@ShapelessLabelledGeneric
case class A3(x1: Int, x2: String, x3: Long)

@ShapelessGeneric(defineType = LazyVal)
case class A4(x1: A1, x2: String)

@ShapelessGeneric(defineType = DefineType.Def)
case class A5(x1: A1)

@ShapelessLabelledGeneric(defineType = DefineType.Val)
case class A6(x: Int)

@ShapelessLabelledGeneric(defineType = DefineType.Def)
case class A7(x: Int)

@ShapelessLabelledGeneric("anotherName", DefineType.LazyVal)
case class A8(x: Int)
