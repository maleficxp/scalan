package scalan.primitives

import scalan.staged.{ProgramGraphs, BaseExp}
import scalan.{ScalanExp, ScalanSeq, Scalan}
import collection.mutable
import scala.language.{implicitConversions}
import scalan.common.Lazy

trait Functions { self: Scalan =>
  implicit class LambdaOps[A,B](f: Rep[A => B]) {
    def apply(x: Rep[A]): Rep[B] = mkApply(f, x)
    def >>[C](g: Rep[B => C]) = compose(g, f)
    def <<[C](g: Rep[C => A]) = compose(f, g)
  }
  def par[B:Elem](nJobs: Rep[Int], f: Rep[Int=>B]): Arr[B]
  def mkApply[A,B](f: Rep[A=>B], x: Rep[A]): Rep[B]
  def mkLambda[A,B](fun: Rep[A] => Rep[B], mayInline: Boolean)(implicit eA: LElem[A], eB: Elem[B]): Rep[A => B]
  def mkLambda[A,B,C](fun: Rep[A]=>Rep[B]=>Rep[C])(implicit eA: LElem[A], eB: Elem[B], eC: Elem[C]): Rep[A=>B=>C]
  def mkLambda[A,B,C](fun: (Rep[A], Rep[B])=>Rep[C])(implicit eA: LElem[A], eB: LElem[B], eC: Elem[C]): Rep[((A,B))=>C]
  implicit def fun[A,B](f: Rep[A] => Rep[B])(implicit eA: LElem[A], eB: Elem[B]): Rep[A => B] = mkLambda(f, true)
  implicit def fun2[A,B,C](fun: (Rep[A], Rep[B])=>Rep[C])(implicit eA: LElem[A], eB: LElem[B], eC: Elem[C]): Rep[((A,B))=>C] = mkLambda(fun)
  def funGlob[A,B](f: Rep[A] => Rep[B])(implicit eA: LElem[A], eB: Elem[B]): Rep[A => B] = mkLambda(f, false)
  //def fun[A,B,C](f: Rep[A]=>Rep[B]=>Rep[C])(implicit eA: Elem[A], eB: Elem[B], eC: Elem[C]): Rep[A=>B=>C] = mkLambda(f)
  def funRec[A,B](f: (Rep[A=>B])=>(Rep[A]=>Rep[B]), mayInline: Boolean)(implicit eA: Elem[A], eb:Elem[B]): Rep[A=>B]
  def funRec[A,B](f: (Rep[A=>B])=>(Rep[A]=>Rep[B]))(implicit eA: Elem[A], eb:Elem[B]): Rep[A=>B] = funRec(f, true)
  //def fun[A,B,C]  (f: Rep[A] => Rep[B] => Rep[C])(implicit eA: Elem[A], eB: Elem[B]): Rep[A=>B=>C]
  def identityFun[A: Elem]: Rep[A => A]
  def constFun[A: Elem, B](x: Rep[B]): Rep[A => B]
  def compose[A, B, C](f: Rep[B => C], g: Rep[A => B]): Rep[A => C]
}

trait FunctionsSeq extends Functions { self: ScalanSeq =>
  def par[B](nJobs: Rep[Int], f: Rep[Int=>B])(implicit elem:Elem[B]): Arr[B] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val tag = elem.classTag
    val tasks = for (i <- 0 until nJobs) yield scala.concurrent.Future {
      f(i)
    }
    scala.concurrent.Await.result(scala.concurrent.Future.sequence(tasks), scala.concurrent.duration.Duration.Inf).toArray
  }
  def mkApply[A,B](f: Rep[A=>B], x: Rep[A]): Rep[B] = f(x)
  def mkLambda[A,B](f: Rep[A] => Rep[B], mayInline: Boolean)(implicit eA: LElem[A], eB: Elem[B]): Rep[A => B] = f
  def mkLambda[A,B,C](fun: Rep[A]=>Rep[B]=>Rep[C])(implicit eA: LElem[A], eB: Elem[B], eC: Elem[C]) = fun
  def mkLambda[A,B,C](fun: (Rep[A], Rep[B])=>Rep[C])(implicit eA: LElem[A], eB: LElem[B], eC: Elem[C]): Rep[((A,B))=>C] = {
    case (x, y) => fun(x, y)
  }
  //def fun[A,B,C]  (f: Rep[A] => Rep[B] => Rep[C])(implicit eA: Elem[A], eB: Elem[B]): Rep[A=>B=>C] = f
  def funRec[A,B](f: (Rep[A=>B])=>(Rep[A]=>Rep[B]), mayInline: Boolean)(implicit eA: Elem[A], eb:Elem[B]): Rep[A=>B] = {
    f(funRec(f, mayInline))(_)
  }
  def identityFun[A: Elem]: Rep[A => A] = x => x
  def constFun[A: Elem, B](x: Rep[B]): Rep[A => B] = _ => x
  def compose[A, B, C](f: Rep[B => C], g: Rep[A => B]): Rep[A => C] = x => f(g(x))
}

trait FunctionsExp extends Functions with BaseExp with ProgramGraphs { self: ScalanExp =>

  class Lambda[A, B](val f: Option[Exp[A] => Exp[B]], val x: Exp[A], val y: Exp[B], self0: Rep[A=>B], val mayInline: Boolean)
                    (implicit val eA: Elem[A] = x.elem, val eB: Elem[B] = y.elem)
    extends BaseDef[A => B] with AstGraph with Product { thisLambda =>
    lazy val uniqueOpId = s"Lambda[${eA.name},${eB.name}]"

    override lazy val self = self0
    override def mirror(t: Transformer) = {
      val newSym = fresh[A=>B]
      val newLam = new Lambda(None, t(x), t(y), newSym, mayInline)
      toExp(newLam, newSym)
    }

    // structural equality pattern implementation
    override lazy val hashCode: Int = 41 * (41 + x.hashCode) + y.hashCode
    override def equals(other: Any) =
      other match {
        case that: Lambda[_,_] =>
          (that canEqual this) &&
          (this.x equals that.x) &&
          (this.y equals that.y)
        case _ => false
      }
    override def toString = s"Lambda(${if (f.isDefined) "f is Some" else "f is None"}, $x => $y})"
    def canEqual(other: Any) = other.isInstanceOf[Lambda[_,_]]

    // Product implementation
    def productElement(n: Int): Any = n match {
      case 0 => x
      case 1 => y
      case _ => throw new NoSuchElementException(s"Lambda.productElement($n) is undefined")
    }
    def productArity: Int = 2

    // AstGraph implementation
    val boundVars = List(x)
    val roots = List(y)
    override lazy val freeVars = super.freeVars
    override lazy val schedule = super.schedule

    def isGlobalLambda: Boolean =
      freeVars.forall { x =>
        val xIsGlobalLambda = x.isLambda && { val Def(lam: Lambda[_, _]) = x; lam.isGlobalLambda }
        x.isConst || xIsGlobalLambda
      }
  }

  type LambdaData[A,B] = (Lambda[A,B], Option[Exp[A] => Exp[B]], Exp[A], Exp[B])
  object Lambda {
    def unapply[A,B](lam: Lambda[A, B]): Option[LambdaData[A,B]] = lam match {
      case null => None
      case _ => Some((lam, lam.f, lam.x, lam.y))
    }
  }

  object ConstantLambda {
    // if lam.y depends on lam.x indirectly, lam.schedule must contain the dependency path
    // and its length will be > 1
    def unapply[A,B](lam: Lambda[A, B]): Option[Exp[B]] =
      if (lam.schedule.length <= 1 && !dep(lam.y).contains(lam.x) && lam.y != lam.x)
        Some(lam.y)
      else
        None
  }

  // matcher version of Lambda.isIdentity
  object IdentityLambda {
    def unapply[A,B](lam: Lambda[A, B]): Boolean = lam.isIdentity
  }

  case class ParallelExecute[B:Elem](nJobs: Exp[Int], f: Exp[Int => B])  extends Def[Array[B]] {
    def selfType = element[Array[B]]
    def uniqueOpId = name(selfType)
    override def mirror(t: Transformer) = ParallelExecute(t(nJobs), t(f))
    /* Added only for debugging
    override def equals(other: Any) = {
      other match {
        case that: ParallelExecute[_] =>
          val eqJobs = nJobs == that.nJobs
          val eqFuncs = f.equals(that.f)
          eqJobs && eqFuncs
        case _ => false
      }
    }
    */
  }

  case class Apply[A,B]
    (f: Exp[A => B], arg: Exp[A])
    (implicit eB: LElem[B])   // enforce explicit laziness at call sites to tie recursive knot (see executeFunction)
      extends Def[B]
  {
    def selfType = eB.value
    lazy val uniqueOpId = name(arg.elem, selfType)
    override def mirror(t: Transformer) = Apply(t(f), t(arg))(eB)
  }

  implicit class LambdaExtensions[A, B](lam: Lambda[A,B]) {
    def argsTree: ProjectionTree = lam.projectionTreeFrom(lam.x)
  }

  implicit class FuncExtensions[A, B](f: Exp[A=>B]) {
    implicit def eA = f.elem.eDom
    def getLambda: Lambda[A,B] = f match {
      case Def(lam: Lambda[_,_]) => lam.asInstanceOf[Lambda[A,B]]
      case _ => !!!(s"Expected symbol of Lambda node but was $f", f)
    }

    def zip[C](g: Rep[A=>C]): Rep[A=>(B,C)] = {
      implicit val eB = f.elem.eRange
      implicit val eC = g.elem.eRange
      fun { (x: Rep[A]) => Pair(f(x), g(x)) }
    }

    def argsTree = getLambda.argsTree
    
    def alphaEqual(g: Exp[A=>B]): Boolean = {
      import graphs._
      val F = new FuncMatcher(f)
      g match {
        case F(res, subst) => res == SimilarityEmbeded || res == SimilarityEqual
        case _ => false
      }
    }
  }

  //=====================================================================================
  //   Function application

  def par[B:Elem](nJobs: Rep[Int], f: Rep[Int=>B]): Arr[B] = ParallelExecute(nJobs, f)

  def mkApply[A,B](f: Exp[A => B], x: Exp[A]): Exp[B] = {
    implicit val leB = Lazy(f.elem.eRange)
    if (recursion.valuesIterator.contains(f)) {
      // f is not in Defs table at this time, thus a special case here
      f.isRecursive = true
      // hit recursion call ! so just make an application
      Apply(f, x)
    } else {
      // not in recursion, so lookup definition
      f match {
        case Def(lam: Lambda[A, B] @unchecked) if lam.mayInline => // unfold initial non-recursive function
          try {
            unfoldLambda(f, lam, x)
          } catch {
            case e: StackOverflowError =>
              if (f.isRecursive)
                Apply(f, x)
              else
                !!!(s"Stack overflow in applying non-recursive $f($x)", e)
          }
        case Def(Apply(_, _)) => // function that is a result of Apply (curried application)
          Apply(f, x)
        case _ => // unknown function
          Apply(f, x)
      }
    }
  }

  def unfoldLambda[A,B](f: Exp[A=>B], lam: Lambda[A,B], x: Exp[A]): Exp[B] = {
    lam.f match {
      case Some(g) => g(x) // unfold initial non-recursive function
      case None => mirrorApply(f, x)  // f is mirrored, unfold it by mirroring
    }
  }

  def unfoldLambda[A,B](f: Exp[A=>B], x: Exp[A]): Exp[B] = {
    val lam = f.getLambda
    unfoldLambda(f, lam, x)
  }

  def mirrorApply[A,B](f: Exp[A => B], s: Exp[A], subst: MapTransformer = MapTransformer.Empty): Exp[B] = {
    val Def(lam: Lambda[A, B]) = f
    val body = lam.scheduleSyms
    val (t, _) = DefaultMirror.mirrorSymbols(subst + (lam.x -> s), NoRewriting, lam, body)
    t(lam.y).asRep[B]
  }

  //=====================================================================================
  //   Function reification

  def mkLambda[A,B](f: Exp[A] => Exp[B], mayInline: Boolean)(implicit eA: LElem[A], eB: Elem[B]): Exp[A=>B] = {
    // in Scalan
    // funRec[A,B]((f: Exp[A => B]) => fun, mayInline)
    val x = fresh[A]
    lambda(x)(f, mayInline)
  }

  def mkLambda[A,B,C]
  (f: Rep[A]=>Rep[B]=>Rep[C])
  (implicit eA: LElem[A], eB: Elem[B], eC: Elem[C]): Rep[A=>B=>C] = {
    val y = fresh[B]
    mkLambda((a: Rep[A]) => lambda(y)((b:Rep[B]) => f(a)(b), true), true)
  }

  def mkLambda[A,B,C](f: (Rep[A], Rep[B])=>Rep[C])(implicit eA: LElem[A], eB: LElem[B], eC: Elem[C]): Rep[((A,B))=>C] = {
    implicit val leAB = Lazy(pairElement(eA.value, eB.value))
    mkLambda({ (p: Rep[(A, B)]) =>
      val (x, y) = unzipPair(p)
      f(x, y)
    }, true)
  }

  private def lambda[A,B](x: Rep[A])(f: Exp[A] => Exp[B], mayInline: Boolean)(implicit eA: LElem[A], eB: Elem[B]): Exp[A=>B] = {
    implicit val eA1 = eA.value
    val res = fresh[A => B]
    reifyFunction(f, x, res, mayInline)
  }

  class LambdaStack {
    var stack = new mutable.Stack[Exp[_]]()
    def top: Option[Exp[_]] = stack.isEmpty match { case true => None case _ => Some(stack.top) }
    def push(e: Exp[_]): this.type = { stack.push(e); this }
    def pop: Exp[_] = stack.pop
  }
  protected var recursion = Map.empty[_ => _, Exp[_]]

  protected val lambdaStack = new LambdaStack
  private def executeFunction[A, B](f: Exp[A]=>Exp[B], x: Exp[A], fSym: Exp[A => B]): Exp[B] = {
    recursion.get(f) match {
      case None =>
        val saveRecursion = recursion
        recursion += (f -> fSym)
        lambdaStack.push(fSym)
        val res = f(x) // execute looking for recursive call back to this exec
        lambdaStack.pop
        recursion = saveRecursion
        res
      case Some(fs) => // hit recursion call !
        fs.isRecursive = true
        Apply(fs.asInstanceOf[Exp[A=>B]], x)(Lazy(fSym.elem.eRange))
    }
  }

  def funRec[A:Elem,B:Elem](f: (Rep[A=>B])=>(Rep[A]=>Rep[B]), mayInline: Boolean): Rep[A=>B] = {
    val x = fresh[A]
    val res = fresh[A => B]
    val fun = f(res)
    reifyFunction(fun, x, res, mayInline)
  }

  def reifyFunction[A, B](fun: Exp[A] => Exp[B], x: Exp[A], fSym: Exp[A=>B], mayInline: Boolean): Exp[A=>B] = {
    val Block(y) = reifyEffects(executeFunction(fun, x, fSym))
    val lam = new Lambda(Some(fun), x, y, fSym, mayInline)

    val optScope = thunkStack.top
    optScope match {
      case Some(scope) =>
        scope.findDef(lam) match {
          case Some(TableEntry(sym, Lambda(_, Some(f), _, _))) =>
            if (f equals fun) sym.asRep[A=>B]
            else {
              val te = createDefinition(optScope, fSym, lam)
              fSym
            }
          case None =>
            val te = createDefinition(optScope, fSym, lam)
            te.sym
        }
      case None =>
        findDefinition(globalThunkSym, lam) match {
          case Some(TableEntry(sym, Lambda(_, Some(f), _, _))) => {
            f equals fun match {
              case true => sym.asRep[A=>B]
              case false =>
                createDefinition(None, fSym, lam)
                fSym
            }
          }
          case None =>
            createDefinition(None, fSym, lam)
            fSym
        }
    }
  }

  def functionSplit[A, B, C](f: Rep[A=>B], g: Rep[A=>C]): Rep[A=>(B,C)] = {
    implicit val eA = f.elem.eDom
    implicit val eB = f.elem.eRange
    implicit val eC = g.elem.eRange
    fun { (x: Rep[A]) => Pair(f(x), g(x)) }
  }

  private val identityFuns = collection.mutable.Map.empty[Element[_], Exp[_]]
  def identityFun[A](implicit e: Element[A]) =
    identityFuns.getOrElseUpdate(e, fun[A, A](x => x)).asRep[A => A]

  private val constFuns = collection.mutable.Map.empty[(Element[_], Exp[_]), Exp[_]]
  def constFun[A, B](x: Rep[B])(implicit e: Element[A]) = {
    implicit val eB = x.elem
    constFuns.getOrElseUpdate((e, x), fun[A, B](_ => x)).asRep[A => B]
  }

  def compose[A, B, C](f: Rep[B => C], g: Rep[A => B]): Rep[A => C] = {
    f match {
      case Def(IdentityLambda()) => g.asRep[A => C]
      case _ => g match {
        case Def(IdentityLambda()) => f.asRep[A => C]
        case _ =>
          implicit val eA = g.elem.eDom
          implicit val eC = f.elem.eRange
          fun { x => f(g(x)) }
      }
    }
  }

  override def rewriteDef[T](d: Def[T]) = d match {
    case Apply(f @ Def(l: Lambda[a,b]), x) if l.mayInline => {
      f(x)
    }
    case _ => super.rewriteDef(d)
  }
}
