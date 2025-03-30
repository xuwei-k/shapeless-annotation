package shapeless_annotation

import scala.annotation.StaticAnnotation

final class ShapelessLabelledGeneric(
  name: String = "",
  defineType: DefineType = DefineType.Val
) extends StaticAnnotation
