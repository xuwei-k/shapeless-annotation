package example

import shapeless.::
import shapeless.HNil
import shapeless.LabelledGeneric
import shapeless.labelled.FieldType
import shapeless.tag.@@

object Test2 {
  def a3: LabelledGeneric.Aux[
    A3,
    FieldType[Symbol @@ "x1", Int] ::
      FieldType[Symbol @@ "x2", String] ::
      FieldType[Symbol @@ "x3", Long] ::
      HNil
  ] = A3.labelledGenericInstance
}
