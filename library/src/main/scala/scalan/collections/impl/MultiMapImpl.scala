package scalan.collections

import scalan._
import scala.reflect.runtime.universe.{WeakTypeTag, weakTypeTag}
import scalan.meta.ScalanAst._

package impl {
// Abs -----------------------------------
trait MultiMapsAbs extends MultiMaps with scalan.Scalan {
  self: ScalanCommunityDsl =>

  // single proxy for each type family
  implicit def proxyMMultiMap[K, V](p: Rep[MMultiMap[K, V]]): MMultiMap[K, V] = {
    proxyOps[MMultiMap[K, V]](p)(scala.reflect.classTag[MMultiMap[K, V]])
  }

  // familyElem
  class MMultiMapElem[K, V, To <: MMultiMap[K, V]](implicit val elemKey: Elem[K], val elemValue: Elem[V])
    extends EntityElem[To] {
    lazy val parent: Option[Elem[_]] = None
    lazy val entityDef: STraitOrClassDef = {
      val module = getModules("MultiMaps")
      module.entities.find(_.name == "MMultiMap").get
    }
    lazy val tyArgSubst: Map[String, TypeDesc] = {
      Map("K" -> Left(elemKey), "V" -> Left(elemValue))
    }
    override def isEntityType = true
    override lazy val tag = {
      implicit val tagK = elemKey.tag
      implicit val tagV = elemValue.tag
      weakTypeTag[MMultiMap[K, V]].asInstanceOf[WeakTypeTag[To]]
    }
    override def convert(x: Rep[Reifiable[_]]) = {
      implicit val eTo: Elem[To] = this
      val conv = fun {x: Rep[MMultiMap[K, V]] => convertMMultiMap(x) }
      tryConvert(element[MMultiMap[K, V]], this, x, conv)
    }

    def convertMMultiMap(x : Rep[MMultiMap[K, V]]): Rep[To] = {
      assert(x.selfType1 match { case _: MMultiMapElem[_, _, _] => true; case _ => false })
      x.asRep[To]
    }
    override def getDefaultRep: Rep[To] = ???
  }

  implicit def mMultiMapElement[K, V](implicit elemKey: Elem[K], elemValue: Elem[V]): Elem[MMultiMap[K, V]] =
    new MMultiMapElem[K, V, MMultiMap[K, V]]

  implicit case object MMultiMapCompanionElem extends CompanionElem[MMultiMapCompanionAbs] {
    lazy val tag = weakTypeTag[MMultiMapCompanionAbs]
    protected def getDefaultRep = MMultiMap
  }

  abstract class MMultiMapCompanionAbs extends CompanionBase[MMultiMapCompanionAbs] with MMultiMapCompanion {
    override def toString = "MMultiMap"
  }
  def MMultiMap: Rep[MMultiMapCompanionAbs]
  implicit def proxyMMultiMapCompanion(p: Rep[MMultiMapCompanion]): MMultiMapCompanion =
    proxyOps[MMultiMapCompanion](p)

  // elem for concrete class
  class HashMMultiMapElem[K, V](val iso: Iso[HashMMultiMapData[K, V], HashMMultiMap[K, V]])(implicit elemKey: Elem[K], elemValue: Elem[V])
    extends MMultiMapElem[K, V, HashMMultiMap[K, V]]
    with ConcreteElem[HashMMultiMapData[K, V], HashMMultiMap[K, V]] {
    override lazy val parent: Option[Elem[_]] = Some(mMultiMapElement(element[K], element[V]))
    override lazy val entityDef = {
      val module = getModules("MultiMaps")
      module.concreteSClasses.find(_.name == "HashMMultiMap").get
    }
    override lazy val tyArgSubst: Map[String, TypeDesc] = {
      Map("K" -> Left(elemKey), "V" -> Left(elemValue))
    }

    override def convertMMultiMap(x: Rep[MMultiMap[K, V]]) = HashMMultiMap(x.map)
    override def getDefaultRep = super[ConcreteElem].getDefaultRep
    override lazy val tag = {
      implicit val tagK = elemKey.tag
      implicit val tagV = elemValue.tag
      weakTypeTag[HashMMultiMap[K, V]]
    }
  }

  // state representation type
  type HashMMultiMapData[K, V] = MMap[K,ArrayBuffer[V]]

  // 3) Iso for concrete class
  class HashMMultiMapIso[K, V](implicit elemKey: Elem[K], elemValue: Elem[V])
    extends Iso[HashMMultiMapData[K, V], HashMMultiMap[K, V]] {
    override def from(p: Rep[HashMMultiMap[K, V]]) =
      p.map
    override def to(p: Rep[MMap[K,ArrayBuffer[V]]]) = {
      val map = p
      HashMMultiMap(map)
    }
    lazy val defaultRepTo: Rep[HashMMultiMap[K, V]] = HashMMultiMap(element[MMap[K,ArrayBuffer[V]]].defaultRepValue)
    lazy val eTo = new HashMMultiMapElem[K, V](this)
  }
  // 4) constructor and deconstructor
  abstract class HashMMultiMapCompanionAbs extends CompanionBase[HashMMultiMapCompanionAbs] with HashMMultiMapCompanion {
    override def toString = "HashMMultiMap"

    def apply[K, V](map: Rep[MMap[K,ArrayBuffer[V]]])(implicit elemKey: Elem[K], elemValue: Elem[V]): Rep[HashMMultiMap[K, V]] =
      mkHashMMultiMap(map)
  }
  object HashMMultiMapMatcher {
    def unapply[K, V](p: Rep[MMultiMap[K, V]]) = unmkHashMMultiMap(p)
  }
  def HashMMultiMap: Rep[HashMMultiMapCompanionAbs]
  implicit def proxyHashMMultiMapCompanion(p: Rep[HashMMultiMapCompanionAbs]): HashMMultiMapCompanionAbs = {
    proxyOps[HashMMultiMapCompanionAbs](p)
  }

  implicit case object HashMMultiMapCompanionElem extends CompanionElem[HashMMultiMapCompanionAbs] {
    lazy val tag = weakTypeTag[HashMMultiMapCompanionAbs]
    protected def getDefaultRep = HashMMultiMap
  }

  implicit def proxyHashMMultiMap[K, V](p: Rep[HashMMultiMap[K, V]]): HashMMultiMap[K, V] =
    proxyOps[HashMMultiMap[K, V]](p)

  implicit class ExtendedHashMMultiMap[K, V](p: Rep[HashMMultiMap[K, V]])(implicit elemKey: Elem[K], elemValue: Elem[V]) {
    def toData: Rep[HashMMultiMapData[K, V]] = isoHashMMultiMap(elemKey, elemValue).from(p)
  }

  // 5) implicit resolution of Iso
  implicit def isoHashMMultiMap[K, V](implicit elemKey: Elem[K], elemValue: Elem[V]): Iso[HashMMultiMapData[K, V], HashMMultiMap[K, V]] =
    new HashMMultiMapIso[K, V]

  // 6) smart constructor and deconstructor
  def mkHashMMultiMap[K, V](map: Rep[MMap[K,ArrayBuffer[V]]])(implicit elemKey: Elem[K], elemValue: Elem[V]): Rep[HashMMultiMap[K, V]]
  def unmkHashMMultiMap[K, V](p: Rep[MMultiMap[K, V]]): Option[(Rep[MMap[K,ArrayBuffer[V]]])]

  registerModule(scalan.meta.ScalanCodegen.loadModule(MultiMaps_Module.dump))
}

// Seq -----------------------------------
trait MultiMapsSeq extends MultiMapsDsl with scalan.ScalanSeq {
  self: ScalanCommunityDslSeq =>
  lazy val MMultiMap: Rep[MMultiMapCompanionAbs] = new MMultiMapCompanionAbs with UserTypeSeq[MMultiMapCompanionAbs] {
    lazy val selfType = element[MMultiMapCompanionAbs]
  }

  case class SeqHashMMultiMap[K, V]
      (override val map: Rep[MMap[K,ArrayBuffer[V]]])
      (implicit elemKey: Elem[K], elemValue: Elem[V])
    extends HashMMultiMap[K, V](map)
        with UserTypeSeq[HashMMultiMap[K, V]] {
    lazy val selfType = element[HashMMultiMap[K, V]]
  }
  lazy val HashMMultiMap = new HashMMultiMapCompanionAbs with UserTypeSeq[HashMMultiMapCompanionAbs] {
    lazy val selfType = element[HashMMultiMapCompanionAbs]
  }

  def mkHashMMultiMap[K, V]
      (map: Rep[MMap[K,ArrayBuffer[V]]])(implicit elemKey: Elem[K], elemValue: Elem[V]): Rep[HashMMultiMap[K, V]] =
      new SeqHashMMultiMap[K, V](map)
  def unmkHashMMultiMap[K, V](p: Rep[MMultiMap[K, V]]) = p match {
    case p: HashMMultiMap[K, V] @unchecked =>
      Some((p.map))
    case _ => None
  }
}

// Exp -----------------------------------
trait MultiMapsExp extends MultiMapsDsl with scalan.ScalanExp {
  self: ScalanCommunityDslExp =>
  lazy val MMultiMap: Rep[MMultiMapCompanionAbs] = new MMultiMapCompanionAbs with UserTypeDef[MMultiMapCompanionAbs] {
    lazy val selfType = element[MMultiMapCompanionAbs]
    override def mirror(t: Transformer) = this
  }

  case class ExpHashMMultiMap[K, V]
      (override val map: Rep[MMap[K,ArrayBuffer[V]]])
      (implicit elemKey: Elem[K], elemValue: Elem[V])
    extends HashMMultiMap[K, V](map) with UserTypeDef[HashMMultiMap[K, V]] {
    lazy val selfType = element[HashMMultiMap[K, V]]
    override def mirror(t: Transformer) = ExpHashMMultiMap[K, V](t(map))
  }

  lazy val HashMMultiMap: Rep[HashMMultiMapCompanionAbs] = new HashMMultiMapCompanionAbs with UserTypeDef[HashMMultiMapCompanionAbs] {
    lazy val selfType = element[HashMMultiMapCompanionAbs]
    override def mirror(t: Transformer) = this
  }

  object HashMMultiMapMethods {
    object union {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[MMultiMap[K,V]]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(that, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "union" =>
          Some((receiver, that)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[MMultiMap[K,V]]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[MMultiMap[K,V]]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object toMap {
      def unapply(d: Def[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "toMap" =>
          Some(receiver).asInstanceOf[Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object contains {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "contains" =>
          Some((receiver, key)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[K]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object apply {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "apply" =>
          Some((receiver, key)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[K]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object applyIfBy {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V] => T], Rep[Unit => T]) forSome {type K; type V; type T}] = d match {
        case MethodCall(receiver, method, Seq(key, exists, otherwise, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "applyIfBy" =>
          Some((receiver, key, exists, otherwise)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V] => T], Rep[Unit => T]) forSome {type K; type V; type T}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V] => T], Rep[Unit => T]) forSome {type K; type V; type T}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object add {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[V]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, value, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "add" =>
          Some((receiver, key, value)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[V]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[V]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object addAll {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V]]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, value, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "addAll" =>
          Some((receiver, key, value)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V]]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V]]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object reduceBy {
      def unapply(d: Def[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[Array[V] => T]) forSome {type K; type V; type T}] = d match {
        case MethodCall(receiver, method, Seq(f, _*), _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "reduceBy" =>
          Some((receiver, f)).asInstanceOf[Option[(Rep[HashMMultiMap[K, V]], Rep[Array[V] => T]) forSome {type K; type V; type T}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[HashMMultiMap[K, V]], Rep[Array[V] => T]) forSome {type K; type V; type T}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object keys {
      def unapply(d: Def[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "keys" =>
          Some(receiver).asInstanceOf[Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object values {
      def unapply(d: Def[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "values" =>
          Some(receiver).asInstanceOf[Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object toArray {
      def unapply(d: Def[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "toArray" =>
          Some(receiver).asInstanceOf[Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object size {
      def unapply(d: Def[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[HashMMultiMapElem[_, _]] && method.getName == "size" =>
          Some(receiver).asInstanceOf[Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[HashMMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }
  }

  object HashMMultiMapCompanionMethods {
    object empty {
      def unapply(d: Def[_]): Option[Unit forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem == HashMMultiMapCompanionElem && method.getName == "empty" =>
          Some(()).asInstanceOf[Option[Unit forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Unit forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object make {
      def unapply(d: Def[_]): Option[Rep[String] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(name, _*), _) if receiver.elem == HashMMultiMapCompanionElem && method.getName == "make" =>
          Some(name).asInstanceOf[Option[Rep[String] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[String] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object fromArray {
      def unapply(d: Def[_]): Option[Arr[(K, V)] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(arr, _*), _) if receiver.elem == HashMMultiMapCompanionElem && method.getName == "fromArray" =>
          Some(arr).asInstanceOf[Option[Arr[(K, V)] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Arr[(K, V)] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }
  }

  def mkHashMMultiMap[K, V]
    (map: Rep[MMap[K,ArrayBuffer[V]]])(implicit elemKey: Elem[K], elemValue: Elem[V]): Rep[HashMMultiMap[K, V]] =
    new ExpHashMMultiMap[K, V](map)
  def unmkHashMMultiMap[K, V](p: Rep[MMultiMap[K, V]]) = p.elem.asInstanceOf[Elem[_]] match {
    case _: HashMMultiMapElem[K, V] @unchecked =>
      Some((p.asRep[HashMMultiMap[K, V]].map))
    case _ =>
      None
  }

  object MMultiMapMethods {
    object map {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "map" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object union {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[MMultiMap[K,V]]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(that, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "union" =>
          Some((receiver, that)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[MMultiMap[K,V]]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[MMultiMap[K,V]]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object isEmpty {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "isEmpty" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object contains {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "contains" =>
          Some((receiver, key)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[K]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object apply {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "apply" =>
          Some((receiver, key)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[K]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[K]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object applyIfBy {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V] => T], Rep[Unit => T]) forSome {type K; type V; type T}] = d match {
        case MethodCall(receiver, method, Seq(key, exists, otherwise, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "applyIfBy" =>
          Some((receiver, key, exists, otherwise)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V] => T], Rep[Unit => T]) forSome {type K; type V; type T}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V] => T], Rep[Unit => T]) forSome {type K; type V; type T}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object add {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[V]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, value, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "add" =>
          Some((receiver, key, value)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[V]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[V]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object addAll {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V]]) forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(key, value, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "addAll" =>
          Some((receiver, key, value)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V]]) forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[K], Rep[ArrayBuffer[V]]) forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object reduceBy {
      def unapply(d: Def[_]): Option[(Rep[MMultiMap[K, V]], Rep[Array[V] => T]) forSome {type K; type V; type T}] = d match {
        case MethodCall(receiver, method, Seq(f, _*), _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "reduceBy" =>
          Some((receiver, f)).asInstanceOf[Option[(Rep[MMultiMap[K, V]], Rep[Array[V] => T]) forSome {type K; type V; type T}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[(Rep[MMultiMap[K, V]], Rep[Array[V] => T]) forSome {type K; type V; type T}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object keys {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "keys" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object values {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "values" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object toArray {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "toArray" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object size {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "size" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object toMap {
      def unapply(d: Def[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem.isInstanceOf[MMultiMapElem[_, _, _]] && method.getName == "toMap" =>
          Some(receiver).asInstanceOf[Option[Rep[MMultiMap[K, V]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMultiMap[K, V]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }
  }

  object MMultiMapCompanionMethods {
    object empty {
      def unapply(d: Def[_]): Option[Unit forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, _, _) if receiver.elem == MMultiMapCompanionElem && method.getName == "empty" =>
          Some(()).asInstanceOf[Option[Unit forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Unit forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object make {
      def unapply(d: Def[_]): Option[Rep[String] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(name, _*), _) if receiver.elem == MMultiMapCompanionElem && method.getName == "make" =>
          Some(name).asInstanceOf[Option[Rep[String] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[String] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object fromArray {
      def unapply(d: Def[_]): Option[Arr[(K, V)] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(a, _*), _) if receiver.elem == MMultiMapCompanionElem && method.getName == "fromArray" =>
          Some(a).asInstanceOf[Option[Arr[(K, V)] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Arr[(K, V)] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }

    object fromMap {
      def unapply(d: Def[_]): Option[Rep[MMap[K,ArrayBuffer[V]]] forSome {type K; type V}] = d match {
        case MethodCall(receiver, method, Seq(map, _*), _) if receiver.elem == MMultiMapCompanionElem && method.getName == "fromMap" =>
          Some(map).asInstanceOf[Option[Rep[MMap[K,ArrayBuffer[V]]] forSome {type K; type V}]]
        case _ => None
      }
      def unapply(exp: Exp[_]): Option[Rep[MMap[K,ArrayBuffer[V]]] forSome {type K; type V}] = exp match {
        case Def(d) => unapply(d)
        case _ => None
      }
    }
  }
}

object MultiMaps_Module {
  val packageName = "scalan.collections"
  val name = "MultiMaps"
  val dump = "H4sIAAAAAAAAALVWTWwbRRQer+M6a4e0RCioSOAQGRAI7JBLDzlUievwUzuJslGFTIU0Xo+dKbOzm51xtMuhB45wQ1wR6r03Lpy4ISEOnBAgceZUyqECegLxZvbHu46dViD2MNqZefN+vu97s3v3PioJH70obMwwbzhE4oal37eFrFttLqkMu+5gzMg1Mvxw9Uu7y3eEgS720IVjLK4J1kNm9NIOvPTdIicdZGJuEyFdX0j0fEdHaNouY8SW1OVN6jhjifuMNDtUyK0OWui7g/AE3UaFDrpku9z2iSRWi2EhiIjXF4nKiKZzU8/DfW8SgzdVFc1MFUc+phLShxiXIvtD4lkhd3noSLQcp7bvqbTApkwdz/VlEqIM7o7dQTJd4BgW0ErnFj7FTQgxalrSp3wEJ6sett/HI7IHJsp8ARIWhA2PQk/Pix1UEeQEAHrL8ZheCTyEEDCwqZNoTPBppPg0FD51i/gUM/oBVpsHvhuEKHoKRYQCD1y8+ggXiQfS5oP6Rzftdx9aVcdQhwOVSllXeAEc1eaoQVMBOH5z+Il48MadKwaq9FCFiu2+kD62ZZbyGK0q5tyVOucUQOyPgK31eWzpKNtgMyUJ03YdD3PwFEO5BDwxalOpjNXaUszOHOjL0iOJaSHwCmm9a3Pq1bppYcYO7l1+7YVf2+8YyMiHMMGlBcL3E6cSmd3umEnaxV7sX40XJSpcn4Cspjf0VA1mMBnL56STAvPSvd8GX2+gm0YKZxz98RgEFyXx0w/V71++aqDFntb7LsOjHiAq2ow4+37L5bKHFt1T4kc75VPM1NtMRssDMsRQdIxzFqAiACTR2tzO9IhCb0t3QSEBoBoJec/lpL57UP/T+vbTu0qnPlqKdqJW/Zte+evn5aHUEpao6MSQA7pFaPA8/AvdRzFS3fZ9HO6Mh0PiTxnO5mqat0qUnOU65Mn1B/S9Ox9LzVAhyF8X+/1b0J9b+txz55CVXFt/9DaM3y//+LmBTOCkTyUUWt94zGb7HxsI5VFabsVXthbZZn7ziTexOE47IwNnYrCSbrayWWZAL6dDDdhezTnMnalNaH06k8UzhURi2kiiMgGtXydhKhGl/ZkSyaR6xoupvNzAbEzO83NWQbMwqaUN/Ow8TWgGVg87T7H7V78yUOltVBpCX4oOKvXdMR8k1ML3U5JA7iRrhTy1QCX2sZNSqZ81NIEv3x57Mw3OFpWp+vUpeZiHhA6p+hhNrf+n+zIrDW3amBm8qlS8ix3Kws154WffxznRzRFpBIg37ayWiTQbwH+BsBqPJjaxoZliCEnG0pn8B4gYGR+tz5GVFTc63Da3H36298p3X/yiv3UVdWXANczTv6GJiIIpwlYif4CMM+bwjwV/OZnsoS3UbaIz/wdvyXOPdQoAAA=="
}
}

