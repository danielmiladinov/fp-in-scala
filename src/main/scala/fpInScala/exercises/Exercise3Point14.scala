package fpInScala.exercises

import fpInScala.dataStructures._

object Exercise3Point14 {
  // Implement append in terms of either foldLeft or foldRight.

  // Defer to List.append for the implementations once again
  def appendRight [A] (a1: List[A], a2: List[A]): List[A] = List.appendRight(a1, a2)

  def appendLeft [A] (a1: List[A], a2: List[A]): List[A] = List.appendLeft(a1, a2)
}
