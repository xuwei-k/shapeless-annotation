package shapeless_annotation

import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro annotations")
final class ShapelessGeneric(name: String = "") extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ShapelessGeneric.macroTransformImpl
}

object ShapelessGeneric {
  def macroTransformImpl(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._

    def valTypesFromClass(cls: ClassDef): Tree = {
      // TODO support more types. not only case class
      cls.impl.body.collect {
        case x: ValDef if x.mods.hasFlag(Flag.PARAMACCESSOR | Flag.CASE) =>
          x.tpt
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
            TermName("genericInstance")
        }

        q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
          ..$body

          implicit val ${instanceName}: _root_.shapeless.Generic.Aux[${tname.toTypeName}, ${aux}] =
            _root_.shapeless.Generic.materialize[${tname.toTypeName}, ${aux}]
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
