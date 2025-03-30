package example

import shapeless.::
import shapeless.Generic
import shapeless.HNil

object Test1 {
  def a1: Generic.Aux[A1, Int :: String :: Boolean :: HNil] =
    A1.genericInstance

  def a2: Generic.Aux[A2, A1 :: Long :: HNil] =
    A2.anotherName
}
