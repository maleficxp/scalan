package scalan.primitives

import scala.language.reflectiveCalls
import scalan.common.SegmentsDslExp
import scalan.{BaseTests, TestContext}

class FunctionTests extends BaseTests { suite =>

  test("identity functions equality works") {
    val ctx = new TestContext(suite, "identityFuns") with SegmentsDslExp {
      lazy val t1 = identityFun[Int]
      lazy val t2 = identityFun[Int]
      lazy val t3 = identityFun[Double]
    }
    import ctx._
    t1 shouldEqual t2
    t1 shouldNot equal(t3)
  }

  test("IdentityLambda matcher works") {
    val ctx = new TestContext(suite, "identityFuns2") {
      lazy val t1 = constFun[Int, Int](1)
      lazy val t2 = fun { x: Rep[Int] => fun { y: Rep[Int] => x } }
    }
    import ctx._

    t1 shouldNot matchPattern { case Def(IdentityLambda()) => }
    t2.getLambda.y shouldNot matchPattern { case Def(IdentityLambda()) => }

    identityFun[Int] should matchPattern { case Def(IdentityLambda()) => }
    fun[Int, Int] { x => x } should matchPattern { case Def(IdentityLambda()) => }
    fun[Int, Int] { x => (x + 1) * 2 } shouldNot matchPattern { case Def(IdentityLambda()) => }
  }

  test("const functions equality works") {
    val ctx = new TestContext(suite, "constFuns1") {
      lazy val t1 = constFun[Int, Int](1)
      lazy val t2 = constFun[Int, Int](1)
      lazy val t3 = constFun[Double, Int](1)
    }
    import ctx._

    t1 shouldEqual t2
    t1 shouldNot equal(t3)
  }

  test("ConstantLambda matcher works") {
    val ctx = new TestContext(suite, "constFuns2") {
      lazy val t1 = constFun[Int, Int](1)
      lazy val t2 = fun { x: Rep[Int] => fun { y: Rep[Int] => x } }
    }
    import ctx._

    t1 should matchPattern { case Def(ConstantLambda(_)) => }
    t2.getLambda.y should matchPattern { case Def(ConstantLambda(_)) => }

    identityFun[Int] shouldNot matchPattern { case Def(ConstantLambda(_)) => }
    fun[Int, Int] { x => x + 1 } shouldNot matchPattern { case Def(ConstantLambda(_)) => }
    fun[Int, Int] { x => (x + 1) * 2 } shouldNot matchPattern { case Def(ConstantLambda(_)) => }
  }

  test("Alpha-equivalence works") {
    val ctx = new TestContext(suite, "alphaEquivalence") {
      lazy val idInt1 = fun[Int, Int] { x => x }
      lazy val idInt2 = fun[Int, Int] { x => x }
      lazy val idDouble = fun[Double, Double] { x => x }
      lazy val f1_1 = fun { x: Rep[Int] => fun { y: Rep[Int] => x } }
      lazy val f1_2 = fun { y: Rep[Int] => fun { x: Rep[Int] => y } }
      lazy val f3 = fun { y: Rep[Int] => fun { x: Rep[Int] => x } }
      lazy val f2_1 = fun { x: Rep[Int] => x + 1 }
      lazy val f2_2 = fun { x: Rep[Int] => x + 1 }
    }
    import ctx._

    idInt1.alphaEqual(idInt2) should be(true)
    f2_1.alphaEqual(f2_2) should be(true)
    f1_1.alphaEqual(f1_2) should be(true)
    idInt1.alphaEqual(f2_1) should be(false)
    f1_1.alphaEqual(f3) should be(false)
  }
}
