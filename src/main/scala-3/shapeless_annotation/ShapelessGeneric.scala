package shapeless_annotation

import scala.annotation.StaticAnnotation

final class ShapelessGeneric(
  name: String = "",
  defineType: DefineType = DefineType.Val
) extends StaticAnnotation
