package fpInScala.exercises

import fpInScala.dataStructures._

object Exercise3Point28 {
  // Write a function map, analogous to the method of the same name on List,
  // that modifies each element in a tree with a given function.

  def map [A, B] (t: Tree[A]) (f: A => B): Tree[B] = t match {
    case Leaf(x) => Leaf(f(x))
    case Branch(l, r) => Branch(map(l)(f), map(r)(f))
  }
}
