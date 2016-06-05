package fpInScala.applicative

import fpInScala.monads.Functor
import scala.language.higherKinds

trait Applicative [F[_]] extends Functor[F] {
  // primitive combinators
  def map2 [A, B, C] (fa: F[A], fb: F[B]) (f: (A, B) => C): F[C]
  def unit [A] (a: => A): F[A]

  // derived combinators
  def apply [A, B] (fab: F[A => B]) (fa: F[A]): F[B] = map2(fab, fa) (_(_))
  def map [A, B] (fa: F[A]) (f: A => B): F[B] = map2(fa, unit(()))((a, _) => f(a))
  def traverse [A, B] (as: List[A]) (f: A => F[B]): F[List[B]] =
    as.foldRight(unit(List[B]()))((a, fbs) => map2(f(a), fbs)(_ :: _))

  def sequence [A] (fas: List[F[A]]): F[List[A]] = traverse(fas)(a => a)
  def replicateM [A] (n: Int, fa: F[A]): F[List[A]] = sequence(List.fill(n)(fa))
  def product [A, B] (fa: F[A], fb: F[B]): F[(A, B)] = map2(fa, fb)((_, _))

  def map3 [A, B, C, D] (fa: F[A],
                         fb: F[B],
                         fc: F[C]) (f: (A, B, C) => D): F[D] = apply(apply(apply(unit(f.curried))(fa))(fb))(fc)

  def map4 [A, B, C, D, E] (fa: F[A],
                            fb: F[B],
                            fc: F[C],
                            fd: F[D]) (f: (A, B, C, D) => E): F[E] = apply(apply(apply(apply(unit(f.curried))(fa))(fb))(fc))(fd)
}

trait ApplicativeWithUnitAndApplyAsPrimitives [F[_]] extends Functor[F] {
  def apply [A, B] (fab: F[A => B]) (fa: F[A]): F[B] = map2(fab, fa) (_(_))

  def unit [A] (a: => A): F[A]

  def map [A, B] (fa: F[A]) (f: A => B): F[B] = apply(unit(f))(fa)
  def map2 [A, B, C] (fa: F[A], fb: F[B]) (f: (A, B) => C): F[C] = apply(map(fa)(f.curried))(fb)
}

object Applicative {
  // The idea behind this Applicative is to combine corresponding elements via zipping
  val streamApplicative = new Applicative[Stream] {
    // The infinite, constant stream
    def unit [A] (a: => A): Stream[A] = Stream.continually(a)

    // Combine elements pointwise
    def map2 [A, B, C] (fa: Stream[A], fb: Stream[B]) (f: (A, B) => C): Stream[C] = fa zip fb map f.tupled

    // For streams, sequence zips the list of streams together into a stream
    // of lists of each streams next value, until any one of the streams completes
    // It has the effect of transposing the list
    override def sequence [A] (a: List[Stream[A]]): Stream[List[A]] = traverse(a)(identity)
  }

  def validationApplicative [E]: Applicative[({type F[X] = Validation[E, X]})#F] = new Applicative[({type F[X] = Validation[E, X]})#F] {
    def unit [A] (a: => A): Validation[E, A] = Success(a)

    def map2 [A, B, C] (fa: Validation[E, A], fb: Validation[E, B]) (f: (A, B) => C): Validation[E, C] = (fa, fb) match {
      case (Success(a), Success(b)) => Success(f(a, b))
      case (Failure(ea, ta), Failure(eb, tb)) => Failure(ea, (ta :+ eb) ++ tb)
      case (e@Failure(_, _), _) => e
      case (_, e@Failure(_, _)) => e
    }
  }

  object Laws {
    def leftAndRightIdentity [S, T, U, V[_]] (f: T => U, g: S => T, fa: V[S], ap: Applicative[V]): Unit = {
      import ap._

      // Obeys Functor identity laws
      map(fa)(identity) == fa
      map(map(fa)(g))(f) == map(fa)(f compose g)

      // As well as Applicative identity laws
      map2(unit(()), fa)((_, a) => a) == fa
      map2(fa, unit(()))((a, _) => a) == fa

      ()
    }

    def associativity [V[_], U] (ap: Applicative[V], fa: V[U], fb: V[U], fc: V[U]): Unit = {
      import ap._

      // Recall that product just combines two effects into a pair, using map2;
      // And if we have pairs nested on the right, we can always turn those into pairs nested on the left:
      def assoc [A, B, C] (p: (A, (B, C))): ((A, B), C) =
        p match { case (a, (b, c)) => ((a, b), c) }

      // Using these combinators, product and assoc, the law of associativity
      // for applicative functors is as follows:
      product(product(fa,fb), fc) == map(product(fa, product(fb,fc)))(assoc)

      ()
    }
  }
}
