package shapeless_annotation

import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro annotations")
final class ShapelessLabelledGeneric(
  name: String = "",
  defineType: DefineType = DefineType.Val
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ShapelessLabelledGeneric.macroTransformImpl
}

object ShapelessLabelledGeneric {
  def macroTransformImpl(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._

    def valTypesFromClass(cls: ClassDef): Tree = {
      // TODO support more types. not only case class
      cls.impl.body.collect {
        case x: ValDef if x.mods.hasFlag(Flag.PARAMACCESSOR | Flag.CASE) =>
          val str = c.internal.constantType(Constant(x.name.toString))
          tq"""_root_.shapeless.labelled.FieldType[_root_.shapeless.tag.@@[_root_.scala.Symbol, ${str}], ${x.tpt}]"""
      }.foldRight(tq"_root_.shapeless.HNil": Tree) { case (a, b) =>
        tq"_root_.shapeless.::[$a, $b]"
      }
    }

    def modifyObject(aux: Tree, obj: Tree): Tree = obj match {
      case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" =>
        val instanceName: TermName = ShapelessGeneric.nameArg(c).getOrElse(TermName("labelledGenericInstance"))
        val defineType: DefineType = ShapelessGeneric.defineTypeArg(c)
        val returnType = tq"_root_.shapeless.LabelledGeneric.Aux[${tname.toTypeName}, ${aux}]"
        val rhs = q"_root_.shapeless.LabelledGeneric.materializeProduct"

        val instance = defineType match {
          case DefineType.Val =>
            ValDef(
              Modifiers(Flag.IMPLICIT),
              instanceName,
              returnType,
              rhs
            )
          case DefineType.LazyVal =>
            ValDef(
              Modifiers(Flag.IMPLICIT | Flag.LAZY),
              instanceName,
              returnType,
              rhs
            )
          case DefineType.Def =>
            DefDef(
              Modifiers(Flag.IMPLICIT),
              instanceName,
              Nil,
              Nil,
              returnType,
              rhs
            )
        }

        q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
          ..$body

          $instance
        }"""
      case _ => sys.error(s"impossible $obj")
    }

    def modify(cls: ClassDef, obj: Tree): Tree = {
      val aux = valTypesFromClass(cls)
      q"..${Seq(cls, modifyObject(aux, obj))}"
    }

    annottees match {
      case (cls: ClassDef) :: (obj: ModuleDef) :: Nil =>
        modify(cls, obj)
      case (cls: ClassDef) :: Nil =>
        modify(cls, q"object ${cls.name.toTermName}")
      case _ =>
        c.abort(c.enclosingPosition, s"unexpected Tree ${annottees.map(_.getClass)}")
    }
  }
}
