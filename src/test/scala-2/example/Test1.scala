package example

import java.lang.reflect.Modifier
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.freespec.AnyFreeSpec
import shapeless.::
import shapeless.Generic
import shapeless.HNil

class Test1 extends AnyFreeSpec {
  def a1: Generic.Aux[A1, Int :: String :: Boolean :: HNil] =
    A1.genericInstance

  def a2: Generic.Aux[A2, A1 :: Long :: HNil] =
    A2.anotherName

  private def hasField(c: Class[?], name: String): Boolean =
    c.getDeclaredFields.toList.exists(f => Modifier.isPrivate(f.getModifiers) && f.getName.contains(name))

  private def hasLazyVal(c: Class[?]): Boolean =
    c.getDeclaredFields.exists(_.getName.contains("bitmap"))

  private def isDef(c: Class[?])(implicit pos: Position): Assertion = {
    assert(!hasField(c, ""))
    assert(!hasLazyVal(c))
  }

  private def isVal(c: Class[?], name: String)(implicit pos: Position): Assertion = {
    assert(hasField(c, name))
    assert(!hasLazyVal(c))
  }

  private def isLazyVal(c: Class[?], name: String)(implicit pos: Position): Assertion = {
    assert(hasField(c, name))
    assert(hasLazyVal(c))
  }

  "DefineType" - {
    "Generic" - {
      "default" in {
        isVal(A1.getClass, "genericInstance")
      }
      "Val" in {
        isVal(A2.getClass, "anotherName")
      }
      "LazyVal" in {
        isLazyVal(A4.getClass, "genericInstance")
      }
      "Def" in {
        isDef(A5.getClass)
      }
    }

    "LabelledGeneric" - {
      "default" in {
        isVal(A3.getClass, "labelledGenericInstance")
      }
      "Val" in {
        isVal(A6.getClass, "labelledGenericInstance")
      }
      "LazyVal" in {
        isLazyVal(A8.getClass, "anotherName")
      }
      "Def" in {
        isDef(A7.getClass)
      }
    }
  }
}
