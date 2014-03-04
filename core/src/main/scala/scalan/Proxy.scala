/**
 * Shamelessly taken from https://github.com/namin/lms-sandbox
 */
package scalan

import java.lang.{reflect => jreflect}
import scalan.staged.{BaseExp}
import scalan.common.Lazy
import scala.reflect.ClassTag

trait ProxyBase { self: Scalan =>
  def proxyOps[Ops<:AnyRef](x: Rep[Ops], forceInvoke: Option[Boolean] = None)(implicit ct: ClassTag[Ops]): Ops
  
  def getStagedFunc(name: String): Rep[_] = {
    val clazz = this.getClass()
    val f = clazz.getDeclaredMethod(name)
    f.invoke(this).asInstanceOf[Rep[_]]
  }
}

trait ProxySeq extends ProxyBase { self: ScalanSeq =>
  def proxyOps[Ops<:AnyRef](x: Rep[Ops], forceInvoke: Option[Boolean] = None)(implicit ct: ClassTag[Ops]): Ops = x.asInstanceOf[Ops]
}

trait ProxyExp extends ProxyBase with BaseExp { self: ScalanStaged =>

  case class MethodCall[T](receiver: Exp[Any], method: jreflect.Method, args: List[AnyRef])(implicit leT: LElem[T]) extends Def[T] {
    def selfType = leT.value
    def uniqueOpId = s"$name:${method.getName}"
    override def mirror(t: Transformer) =
      MethodCall[T](t(receiver), method, args map { case a: Exp[_] => t(a) case a => a })
    override def self: Rep[T] = { this }
  }

//TODO
//  case class MethodCallLifted[T](receiver: PA[Any], method: jreflect.Method, args: List[AnyRef])
//                                (implicit val elem: Elem[T]) extends StagedArrayBase[T] {
//    override def mirror(t: Transformer) =
//      MethodCallLifted[T](t(receiver), method, args map { case a: Exp[_] => t(a) case a => a })
//  }

  override def proxyOps[Ops<:AnyRef](x: Rep[Ops], forceInvoke: Option[Boolean] = None)(implicit ct: ClassTag[Ops]): Ops = {
    val clazz = ct.runtimeClass
    val handler = new InvocationHandler(x, forceInvoke)
    val proxy = jreflect.Proxy.newProxyInstance(clazz.getClassLoader(), Array(clazz), handler)
    proxy.asInstanceOf[Ops]
  }

  var invokeEnabled = false

  private def hasFuncArg(args: Array[AnyRef]):Boolean = args.exists(_.isInstanceOf[(_) => _])

  // stack of receivers for which MethodCall nodes should be created by InvocationHandler
  var methodCallReceivers = List.empty[Exp[Any]]

  class InvocationHandler(receiver: Exp[Any], forceInvoke: Option[Boolean]) extends jreflect.InvocationHandler {

    def invoke(proxy: AnyRef, m: jreflect.Method, _args: Array[AnyRef]) = {
      val args = _args == null match { case true => Array.empty[AnyRef] case _ => _args }
      receiver match {
        case Def(d) => {  // call method of the node
          val nodeClazz = d.getClass
          m.getDeclaringClass.isAssignableFrom(nodeClazz) && (invokeEnabled || forceInvoke.getOrElse(false)) && (!hasFuncArg(args)) match {
            case true =>
              val res = m.invoke(d, args: _*)
              res
            case _ => invokeMethodOfVar(m, args)
          }
        }
        case _ => invokeMethodOfVar(m, args)
      }
    }


    def invokeMethodOfVar(m: jreflect.Method, args: Array[AnyRef]) = {
      /* If invoke is enabled or current method has arg of type <function> - do not create methodCall */
      methodCallReceivers.contains(receiver) || (!((invokeEnabled || forceInvoke.getOrElse(false)) || hasFuncArg(args))) match {
        case true =>
          createMethodCall(m, args)
        case _ => receiver.elem match {
          case ve: ViewElem[_,_] =>
            //val e = getRecieverElem
            val iso = ve.iso
            methodCallReceivers = methodCallReceivers :+ receiver
            val wrapper = iso.to(iso.from(receiver))
            methodCallReceivers = methodCallReceivers.tail
            val Def(d) = wrapper
            val res = m.invoke(d, args: _*)
            res
          case _ =>
            createMethodCall(m, args)
        }
      }
    }

    def createMethodCall(m: jreflect.Method, args: Array[AnyRef]): Exp[Any] = {
      val resultElem = Lazy(getResultElem(m, args))
      reifyObject(MethodCall[AnyRef](
              receiver, m, args.toList)(resultElem))(resultElem)
    }

//    def getRecieverElem: ViewElem[Any,Any] = receiver.elem match {
//      case e: ViewElem[_,_] => e.asInstanceOf[ViewElem[Any,Any]]
//      case _ =>
//        !!!("Receiver with ViewElem expected", receiver)
//    }

    def getResultElem(m: jreflect.Method, args: Array[AnyRef]): Elem[AnyRef] = {
      val e = receiver.elem //getRecieverElem
      val zero = e match {
        case ve: ViewElem[_,_] => ve.iso.defaultRepTo.value
        case _ => e.defaultRepValue
      }
      val Def(zeroNode) = zero
      val res = m.invoke(zeroNode, args: _*)
      res match {
        case s: Exp[_] => s.asInstanceOf[Exp[AnyRef]].elem
        case other => ???(s"don't know how to get result elem for $other")
      }
    }

  }
}