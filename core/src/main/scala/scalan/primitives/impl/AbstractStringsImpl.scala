package scalan.primitives

import scalan._
import scalan.common.Default
import scala.reflect.runtime.universe.{WeakTypeTag, weakTypeTag}
import scalan.meta.ScalanAst._

package impl {
// Abs -----------------------------------
trait AbstractStringsAbs extends AbstractStrings with scalan.Scalan {
  self: AbstractStringsDsl =>

  // single proxy for each type family
  implicit def proxyAString(p: Rep[AString]): AString = {
    proxyOps[AString](p)(scala.reflect.classTag[AString])
  }

  // familyElem
  class AStringElem[To <: AString]
    extends EntityElem[To] {
    lazy val parent: Option[Elem[_]] = None
    lazy val entityDef: STraitOrClassDef = {
      val module = getModules("AbstractStrings")
      module.entities.find(_.name == "AString").get
    }
    lazy val tyArgSubst: Map[String, TypeDesc] = {
      Map()
    }
    override def isEntityType = true
    override lazy val tag = {
      weakTypeTag[AString].asInstanceOf[WeakTypeTag[To]]
    }
    override def convert(x: Rep[Reifiable[_]]) = {
      implicit val eTo: Elem[To] = this
      val conv = fun {x: Rep[AString] => convertAString(x) }
      tryConvert(element[AString], this, x, conv)
    }

    def convertAString(x : Rep[AString]): Rep[To] = {
      assert(x.selfType1 match { case _: AStringElem[_] => true; case _ => false })
      x.asRep[To]
    }
    override def getDefaultRep: Rep[To] = ???
  }

  implicit def aStringElement: Elem[AString] =
    new AStringElem[AString]

  implicit case object AStringCompanionElem extends CompanionElem[AStringCompanionAbs] {
    lazy val tag = weakTypeTag[AStringCompanionAbs]
    protected def getDefaultRep = AString
  }

  abstract class AStringCompanionAbs extends CompanionBase[AStringCompanionAbs] with AStringCompanion {
    override def toString = "AString"
  }
  def AString: Rep[AStringCompanionAbs]
  implicit def proxyAStringCompanion(p: Rep[AStringCompanion]): AStringCompanion =
    proxyOps[AStringCompanion](p)

  // elem for concrete class
  class SStringElem(val iso: Iso[SStringData, SString])
    extends AStringElem[SString]
    with ConcreteElem[SStringData, SString] {
    override lazy val parent: Option[Elem[_]] = Some(aStringElement)
    override lazy val entityDef = {
      val module = getModules("AbstractStrings")
      module.concreteSClasses.find(_.name == "SString").get
    }
    override lazy val tyArgSubst: Map[String, TypeDesc] = {
      Map()
    }

    override def convertAString(x: Rep[AString]) = SString(x.wrappedValueOfBaseType)
    override def getDefaultRep = super[ConcreteElem].getDefaultRep
    override lazy val tag = {
      weakTypeTag[SString]
    }
  }

  // state representation type
  type SStringData = String

  // 3) Iso for concrete class
  class SStringIso
    extends Iso[SStringData, SString] {
    override def from(p: Rep[SString]) =
      p.wrappedValueOfBaseType
    override def to(p: Rep[String]) = {
      val wrappedValueOfBaseType = p
      SString(wrappedValueOfBaseType)
    }
    lazy val defaultRepTo: Rep[SString] = SString("")
    lazy val eTo = new SStringElem(this)
  }
  // 4) constructor and deconstructor
  abstract class SStringCompanionAbs extends CompanionBase[SStringCompanionAbs] with SStringCompanion {
    override def toString = "SString"

    def apply(wrappedValueOfBaseType: Rep[String]): Rep[SString] =
      mkSString(wrappedValueOfBaseType)
  }
  object SStringMatcher {
    def unapply(p: Rep[AString]) = unmkSString(p)
  }
  def SString: Rep[SStringCompanionAbs]
  implicit def proxySStringCompanion(p: Rep[SStringCompanionAbs]): SStringCompanionAbs = {
    proxyOps[SStringCompanionAbs](p)
  }

  implicit case object SStringCompanionElem extends CompanionElem[SStringCompanionAbs] {
    lazy val tag = weakTypeTag[SStringCompanionAbs]
    protected def getDefaultRep = SString
  }

  implicit def proxySString(p: Rep[SString]): SString =
    proxyOps[SString](p)

  implicit class ExtendedSString(p: Rep[SString]) {
    def toData: Rep[SStringData] = isoSString.from(p)
  }

  // 5) implicit resolution of Iso
  implicit def isoSString: Iso[SStringData, SString] =
    new SStringIso

  // 6) smart constructor and deconstructor
  def mkSString(wrappedValueOfBaseType: Rep[String]): Rep[SString]
  def unmkSString(p: Rep[AString]): Option[(Rep[String])]

  // elem for concrete class
  class CStringElem(val iso: Iso[CStringData, CString])
    extends AStringElem[CString]
    with ConcreteElem[CStringData, CString] {
    override lazy val parent: Option[Elem[_]] = Some(aStringElement)
    override lazy val entityDef = {
      val module = getModules("AbstractStrings")
      module.concreteSClasses.find(_.name == "CString").get
    }
    override lazy val tyArgSubst: Map[String, TypeDesc] = {
      Map()
    }

    override def convertAString(x: Rep[AString]) = CString(x.wrappedValueOfBaseType)
    override def getDefaultRep = super[ConcreteElem].getDefaultRep
    override lazy val tag = {
      weakTypeTag[CString]
    }
  }

  // state representation type
  type CStringData = String

  // 3) Iso for concrete class
  class CStringIso
    extends Iso[CStringData, CString] {
    override def from(p: Rep[CString]) =
      p.wrappedValueOfBaseType
    override def to(p: Rep[String]) = {
      val wrappedValueOfBaseType = p
      CString(wrappedValueOfBaseType)
    }
    lazy val defaultRepTo: Rep[CString] = CString("")
    lazy val eTo = new CStringElem(this)
  }
  // 4) constructor and deconstructor
  abstract class CStringCompanionAbs extends CompanionBase[CStringCompanionAbs] with CStringCompanion {
    override def toString = "CString"

    def apply(wrappedValueOfBaseType: Rep[String]): Rep[CString] =
      mkCString(wrappedValueOfBaseType)
  }
  object CStringMatcher {
    def unapply(p: Rep[AString]) = unmkCString(p)
  }
  def CString: Rep[CStringCompanionAbs]
  implicit def proxyCStringCompanion(p: Rep[CStringCompanionAbs]): CStringCompanionAbs = {
    proxyOps[CStringCompanionAbs](p)
  }

  implicit case object CStringCompanionElem extends CompanionElem[CStringCompanionAbs] {
    lazy val tag = weakTypeTag[CStringCompanionAbs]
    protected def getDefaultRep = CString
  }

  implicit def proxyCString(p: Rep[CString]): CString =
    proxyOps[CString](p)

  implicit class ExtendedCString(p: Rep[CString]) {
    def toData: Rep[CStringData] = isoCString.from(p)
  }

  // 5) implicit resolution of Iso
  implicit def isoCString: Iso[CStringData, CString] =
    new CStringIso

  // 6) smart constructor and deconstructor
  def mkCString(wrappedValueOfBaseType: Rep[String]): Rep[CString]
  def unmkCString(p: Rep[AString]): Option[(Rep[String])]

  registerModule(scalan.meta.ScalanCodegen.loadModule(AbstractStrings_Module.dump))
}

// Seq -----------------------------------
trait AbstractStringsSeq extends AbstractStringsDsl with scalan.ScalanSeq {
  self: AbstractStringsDslSeq =>
  lazy val AString: Rep[AStringCompanionAbs] = new AStringCompanionAbs with UserTypeSeq[AStringCompanionAbs] {
    lazy val selfType = element[AStringCompanionAbs]
  }

  case class SeqSString
      (override val wrappedValueOfBaseType: Rep[String])

    extends SString(wrappedValueOfBaseType)
        with UserTypeSeq[SString] {
    lazy val selfType = element[SString]
  }
  lazy val SString = new SStringCompanionAbs with UserTypeSeq[SStringCompanionAbs] {
    lazy val selfType = element[SStringCompanionAbs]
  }

  def mkSString
      (wrappedValueOfBaseType: Rep[String]): Rep[SString] =
      new SeqSString(wrappedValueOfBaseType)
  def unmkSString(p: Rep[AString]) = p match {
    case p: SString @unchecked =>
      Some((p.wrappedValueOfBaseType))
    case _ => None
  }

  case class SeqCString
      (override val wrappedValueOfBaseType: Rep[String])

    extends CString(wrappedValueOfBaseType)
        with UserTypeSeq[CString] {
    lazy val selfType = element[CString]
  }
  lazy val CString = new CStringCompanionAbs with UserTypeSeq[CStringCompanionAbs] {
    lazy val selfType = element[CStringCompanionAbs]
  }

  def mkCString
      (wrappedValueOfBaseType: Rep[String]): Rep[CString] =
      new SeqCString(wrappedValueOfBaseType)
  def unmkCString(p: Rep[AString]) = p match {
    case p: CString @unchecked =>
      Some((p.wrappedValueOfBaseType))
    case _ => None
  }
}

// Exp -----------------------------------
trait AbstractStringsExp extends AbstractStringsDsl with scalan.ScalanExp {
  self: AbstractStringsDslExp =>
  lazy val AString: Rep[AStringCompanionAbs] = new AStringCompanionAbs with UserTypeDef[AStringCompanionAbs] {
    lazy val selfType = element[AStringCompanionAbs]
    override def mirror(t: Transformer) = this
  }

  case class ExpSString
      (override val wrappedValueOfBaseType: Rep[String])

    extends SString(wrappedValueOfBaseType) with UserTypeDef[SString] {
    lazy val selfType = element[SString]
    override def mirror(t: Transformer) = ExpSString(t(wrappedValueOfBaseType))
  }

  lazy val SString: Rep[SStringCompanionAbs] = new SStringCompanionAbs with UserTypeDef[SStringCompanionAbs] {
    lazy val selfType = element[SStringCompanionAbs]
    override def mirror(t: Transformer) = this
  }

  object SStringMethods {
  }

  object SStringCompanionMethods {
  }

  def mkSString
    (wrappedValueOfBaseType: Rep[String]): Rep[SString] =
    new ExpSString(wrappedValueOfBaseType)
  def unmkSString(p: Rep[AString]) = p.elem.asInstanceOf[Elem[_]] match {
    case _: SStringElem @unchecked =>
      Some((p.asRep[SString].wrappedValueOfBaseType))
    case _ =>
      None
  }

  case class ExpCString
      (override val wrappedValueOfBaseType: Rep[String])

    extends CString(wrappedValueOfBaseType) with UserTypeDef[CString] {
    lazy val selfType = element[CString]
    override def mirror(t: Transformer) = ExpCString(t(wrappedValueOfBaseType))
  }

  lazy val CString: Rep[CStringCompanionAbs] = new CStringCompanionAbs with UserTypeDef[CStringCompanionAbs] {
    lazy val selfType = element[CStringCompanionAbs]
    override def mirror(t: Transformer) = this
  }

  object CStringMethods {
  }

  object CStringCompanionMethods {
  }

  def mkCString
    (wrappedValueOfBaseType: Rep[String]): Rep[CString] =
    new ExpCString(wrappedValueOfBaseType)
  def unmkCString(p: Rep[AString]) = p.elem.asInstanceOf[Elem[_]] match {
    case _: CStringElem @unchecked =>
      Some((p.asRep[CString].wrappedValueOfBaseType))
    case _ =>
      None
  }

  object AStringMethods {
    object wrappedValueOfBaseType {
      def unapply(d: Def[_]): Option[Rep[AString]] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[AStringElem[_]] && method.getName == "wrappedValueOfBaseType" =>
          Some(receiver).asInstanceOf[Option[Rep[AString]]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[AString]] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }
  }

  object AStringCompanionMethods {
    object defaultVal {
      def unapply(d: Def[_]): Option[Unit] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem == AStringCompanionElem && method.getName == "defaultVal" =>
          Some(()).asInstanceOf[Option[Unit]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Unit] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object apply {
      def unapply(d: Def[_]): Option[Rep[String]] = d match {
        case MethodCall(receiver, method, Seq(msg, _*), _) if receiver.elem == AStringCompanionElem && method.getName == "apply" =>
          Some(msg).asInstanceOf[Option[Rep[String]]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[String]] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }
  }
}

object AbstractStrings_Module {
  val packageName = "scalan.primitives"
  val name = "AbstractStrings"
  val dump = "H4sIAAAAAAAAALVVT4hbRRj/8ja72SRLd7vQwu7FNY0rFk2WQulhCyVNUxHSzbKvisRSmLxM0mnnzZudmd0mPfTgUW/iVaT33rwIghcRxIMnUcGzp6pIUXtq6cy8P3lJ+9r14DsM8+bNfN/vz/fNu/8HzEsBm9JDFLGajxWquXbekKrqtpgianwl6B9QfAkPPjz5pXeFXZQOLHdh4QaSlyTtQjGctEY8mbt4vw1FxDwsVSCkglfbNkPdCyjFniIBqxPfP1CoR3G9TaTabkO+F/TH+3AXcm1Y8QLmCayw26RISiyj9UVsEJHkvWjfxx0+ycHqhkU9xeKqQERp+DrHSrh/D3N3zAI29hUci6B1uIGl9xSIzwOh4hQFHe5G0I9f8wzpBVht30SHqK5TDOuuEoQN9ckyR94tNMQ7eovZnteAJaaDq2Nu3+faUJJ4Xwv0js+pXRlxANAOnLEgahN9aok+NaNP1cWCIEruIPNxVwSjMYRPbg5gxHWIN18SIo6AW6xf/eia98Ejt+w75vDIQClYhgs60CsZ1WCt0Dp+t/eJfPj2vXMOlLpQIrLRk0ogT6Utj9QqI8YCZTEnAiIx1G5VstyyWRp6z0xJFL3A54jpSJGUS9onSjyizGazthS5kyF9QXEcb82NeC7hu5HB19ZNE1G6+2Dtrdd+b73vgDOdoqhDurrwRRxUQaERloOV1AzFSN3sPAnj1x/82f92C645iU5R2KNZo0PMy19+Kv/4xgUHFru2kC9TNOxqqWSLYr8jmgFTXVgMDrEIvxQOETWz51pV6OMBOqAqEjDNfE4zV7CR2XIcG1m2bXnnYgHKYYXuBAxXL+9W/3W///S+KUABS+GXsAefkHOPfz02ULY2FZy8LRDnuP8eoge4M7iIJDauWpDLCuZ0Myf6nMqykuNdQXx9dRzis9989e5fX+/MWzdXI4o2+MS5fJqtAeFUKgoWJhtCVyfelkICbuDj45WH5Pq9j5V1MTeavis6vZu6ObftubUXGBrfWf90t5y/137+3IGi9q1HlI94deuInfY/dg8k9T0Z1rVOK26oUTOdbn1yv6zaqW4Td6Jl6nM57srI3MxusrFSe08kdWYj/oeqMcPGs5aa8ZQdNzO5No/ItTnLNUyUgr8J07yLe5gMiLm6j6RHBPpZ5NOnl1qjBOvWi9hP02xk0py579ZnYJ2fXtRSLMf/ivCQ/g8cjxqAx+0pIwICKhm94UaVqdvj7qPPdk7/8MVvtpdLpsb13cKSf3e6h6elWJ0Bov/JKfAK8qb8LfynWm1v9yMJAAA="
}
}

trait AbstractStringsDsl extends impl.AbstractStringsAbs {self: AbstractStringsDsl =>}
trait AbstractStringsDslSeq extends impl.AbstractStringsSeq {self: AbstractStringsDslSeq =>}
trait AbstractStringsDslExp extends impl.AbstractStringsExp {self: AbstractStringsDslExp =>}
