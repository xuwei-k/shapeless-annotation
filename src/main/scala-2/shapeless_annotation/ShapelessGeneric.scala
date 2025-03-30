package shapeless_annotation

import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro annotations")
final class ShapelessGeneric(
  name: String = "",
  defineType: DefineType = DefineType.Val
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ShapelessGeneric.macroTransformImpl
}

object ShapelessGeneric {
  private[shapeless_annotation] def nameArg(c: blackbox.Context): Option[c.TermName] = {
    import c.universe.*
    val Apply(Select(Apply(_, args), _), _) = c.macroApplication
    args.collectFirst {
      case Literal(Constant(nameArg: String)) =>
        TermName(nameArg)
      case NamedArg(Ident(TermName("name")), Literal(Constant(nameArg: String))) =>
        TermName(nameArg)
    }
  }

  private[shapeless_annotation] def defineTypeArg(c: blackbox.Context): DefineType = {
    import c.universe.*
    val Apply(Select(Apply(_, args), _), _) = c.macroApplication

    object ExtractVal {
      def unapply(t: Tree): Boolean = PartialFunction.cond(t) {
        case Select(Ident(TermName("DefineType")), TermName("Val")) | Ident(TermName("Val")) =>
          true
      }
    }
    object ExtractLazyVal {
      def unapply(t: Tree): Boolean = PartialFunction.cond(t) {
        case Select(Ident(TermName("DefineType")), TermName("LazyVal")) | Ident(TermName("LazyVal")) =>
          true
      }
    }
    object ExtractDef {
      def unapply(t: Tree): Boolean = PartialFunction.cond(t) {
        case Select(Ident(TermName("DefineType")), TermName("Def")) | Ident(TermName("Def")) =>
          true
      }
    }
    args.collectFirst {
      case ExtractVal() | NamedArg(Ident(TermName("defineType")), ExtractVal()) =>
        DefineType.Val
      case ExtractLazyVal() | NamedArg(Ident(TermName("defineType")), ExtractLazyVal()) =>
        DefineType.LazyVal
      case ExtractDef() | NamedArg(Ident(TermName("defineType")), ExtractDef()) =>
        DefineType.Def
    }.getOrElse(
      DefineType.Val
    )
  }

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
        val instanceName: TermName = nameArg(c).getOrElse(TermName("genericInstance"))
        val defineType: DefineType = defineTypeArg(c)
        val returnType = tq"_root_.shapeless.Generic.Aux[${tname.toTypeName}, ${aux}]"
        val rhs = q"_root_.shapeless.Generic.materialize[${tname.toTypeName}, ${aux}]"

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
