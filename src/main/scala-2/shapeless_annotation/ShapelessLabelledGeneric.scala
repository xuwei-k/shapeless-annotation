package shapeless_annotation

import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro annotations")
final class ShapelessLabelledGeneric(name: String = "") extends StaticAnnotation {
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
        val instanceName: TermName = c.macroApplication match {
          case Apply(Select(Apply(_, List(Literal(Constant(nameArg: String)))), _), _) =>
            TermName(nameArg)
          case Apply(Select(Apply(_, _), _), _) =>
            TermName("labelledGenericInstance")
        }

        q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
          ..$body

          implicit val ${instanceName}: _root_.shapeless.LabelledGeneric.Aux[${tname.toTypeName}, ${aux}] =
            _root_.shapeless.LabelledGeneric.materializeProduct
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
