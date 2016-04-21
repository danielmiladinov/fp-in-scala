package fpInScala.monads

import scala.language.higherKinds

trait Monad[F[_]] extends Functor[F] {
  def unit [A] (a: => A): F[A]
  def flatMap [A, B] (ma: F[A]) (f: A => F[B]): F[B]

  def map [A, B] (ma: F[A]) (f: A => B): F[B] =
    flatMap(ma)(a => unit(f(a)))

  def map2 [A, B, C] (ma: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    flatMap(ma)(a => map(fb)(b => f(a, b)))
}
