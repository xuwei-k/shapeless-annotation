package shapeless_annotation

sealed abstract class DefineType extends Product with Serializable

object DefineType {
  case object Val extends DefineType
  case object LazyVal extends DefineType
  case object Def extends DefineType
}
