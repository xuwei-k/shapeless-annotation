# shaepless annotation

`build.sbt`

```scala
libraryDependencies += "com.github.xuwei-k" %% "shapeless-annotation" % "version"

scalacOptions ++= {
  scalaBinaryVersion.value match {
    case "2.13" =>
      Seq("-Ymacro-annotations")
    case _ =>
      Nil
  }
}
```

```scala
import shapeless_annotation.ShapelessGeneric

@ShapelessGeneric // macro annotation
case class A1(x: Int, y: String, z: Boolean)
```

↓

```scala
import shapeless.Generic
import shapeless.HNil
import shapeless.::

case class A1(x: Int, y: String, z: Boolean)

object A1 {
  // generate by macro annotation 😄
  implicit val genericInstance: Generic.Aux[A1, Int :: String :: Boolean :: HNil] =
    Generic.materialize[A1, Int :: String :: Boolean :: HNil]
}
```


## Credit

- <https://github.com/milessabin/shapeless/commit/df313df8c3efb6a2e9094341bc66e7e2d78edd7d>
- <https://github.com/milessabin/shapeless/issues/1287>
