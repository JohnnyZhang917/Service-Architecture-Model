package eu.pmsoft.sam.see

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.testng.annotations.Test
import org.testng.Assert

class FutureApiTest {



  @Test
  def testFutureCallsFlatMap() {
    val one = Future {
      Thread.sleep(30)
      println("calculate one")
      1
    }
    val two = Future {
      Thread.sleep(10)
      println("calculate two")
      2
    }

    val three = one.flatMap {
      o => {
        println("calculate flatMap")
        two.map {
          println("two.map")
          r => r + o
        }
      }
    }

    val r = Await.result(three,0 nanos)
    System.out.println(r)
    Assert.assertEquals(3,r)
  }

  @Test
  def testFutureCallsForOut() {
    val one = Future {
      Thread.sleep(30)
      println("calculate one")

      1
    }
    val two = Future {
      Thread.sleep(10)
      println("calculate two")
      2
    }

    val three = for {
      o <- one
      t <- two
    } yield {
      println("calculate three")
      o + t
    }

    val r = Await.result(three,50 millis)
    System.out.println(r)
    Assert.assertEquals(3,r)
  }

  @Test
  def testFutureCallsForSequence() {

    val three = for {
      o <- Future {println("init one"); Thread.sleep(30); println("calculate one"); 1 }
      t <- Future {println("init two"); Thread.sleep(10); println("calculate two"); 2 }
    } yield {
      println("calculate three")
      o + t
    }

    val r = Await.result(three,50 millis)
    System.out.println(r)
    Assert.assertEquals(3,r)
  }

  @Test
  def testFutureFlatMap() {
    val f1 = Future {println("init one"); Thread.sleep(20); println("calculate one"); 1 }
    val f2 = Future {println("init two"); Thread.sleep(10); println("calculate two"); 2 }
    val f3 = f1 flatMap {
      one => {
        println("f1 flatMap")
        f2.map {
          println("f2 map")
          two => {
            println("f3 final calculation")
            two + one
          }
        }
      }
    }
    val r = Await.result(f3,50 millis)
    System.out.println(r)
    Assert.assertEquals(3,r)

  }

  @Test
  def testFutureFlatMapOnFor() {
    val f1 = Future {println("init one"); Thread.sleep(20); println("calculate one"); 1 }
    val f2 = Future {println("init two"); Thread.sleep(10); println("calculate two"); 2 }

    f1.foreach {
      one => {

      }
    }
    val f3 = for {
      one <- f1
      two <- f2
    } yield one + two

    f1.flatMap {
    one => {
      f2.map {
        two => two + one
      }
    }
    }


    val r = Await.result(f3,50 millis)
    System.out.println(r)
    Assert.assertEquals(3,r)

  }
}
