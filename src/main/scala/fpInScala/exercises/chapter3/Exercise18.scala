package fpInScala.exercises.chapter3

import fpInScala.dataStructures.list.List

object Exercise18 {
  // Write a function map that generalizes modifying each element in a list while maintaining the structure of the list.
  // Here is its signature:

  // Once again, I'll defer to the implementation on List:
  def map [A, B] (as: List[A]) (f: A => B): List[B] = List.map(as)(f)
}
