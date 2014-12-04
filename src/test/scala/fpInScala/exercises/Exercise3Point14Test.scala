package fpInScala.exercises

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, ShouldMatchers}
import fpInScala.dataStructures._

class Exercise3Point14Test extends FlatSpec with ShouldMatchers with TableDrivenPropertyChecks {

  def defaultAppend [A] (a1: List[A], a2: List[A]): List[A] = a1 match {
    case Nil => a2
    case Cons(h, t) => Cons(h, defaultAppend(t, a2))
  }

  val testData = Table(
    ("list1",       "list2"),
    (Nil,           Nil),
    (Nil,           List(1, 2, 3)),
    (List(1, 2, 3), Nil),
    (List(1, 2),    List(3, 4))
  )

  forAll (testData) { (list1, list2) =>
    val appended = defaultAppend(list1, list2)

    Exercise3Point14.appendRight(list1, list2) should be === appended
    Exercise3Point14.appendLeft(list1,  list2) should be === appended
  }
}
