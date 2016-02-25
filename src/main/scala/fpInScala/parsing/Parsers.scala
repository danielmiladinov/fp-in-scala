package fpInScala.parsing

import scala.language.{implicitConversions, higherKinds}
import fpInScala.testing._
import fpInScala.testing.Prop._

import scala.util.matching.Regex

trait Parsers [ParseError, Parser[+_]] { self =>

  def run [A] (p: Parser[A]) (input: String): Either[ParseError, A]
  def char (c: Char): Parser[Char] = string(c.toString) map (_.charAt(0))
  def succeed [A] (a: A): Parser[A] = string("") map (_ => a)

  def slice [A] (p: Parser[A]): Parser[String]

  def many [A] (p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_ :: _) or succeed(List())
  def many1 [A] (p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_ :: _)
  def map [A, B] (p: Parser[A]) (f: A => B): Parser[B]

  def product [A, B] (p: Parser[A], p2: => Parser[B]): Parser[(A, B)] =
    flatMap(p)(a => map(p2)(b => (a, b)))

  def map2 [A, B, C] (p: Parser[A], p2: => Parser[B]) (f: (A,B) => C): Parser[C] =
    flatMap(p)(a => map(p2)(b => f(a, b)))

  def flatMap [A, B] (p: Parser[A]) (f: A => Parser[B]): Parser[B]

  def listOfN [A] (n: Int, p: Parser[A]): Parser[List[A]] =
    if (n < 1) succeed(List())
    else map2(p, listOfN(n - 1, p))(_ :: _)

  def or [A] (s1: Parser[A], s2: => Parser[A]): Parser[A]
  implicit def string (s: String): Parser[String]
  implicit def operators [A] (p: Parser[A]): ParserOps[A] = ParserOps[A](p)
  implicit def asStringParser [A] (a: A) (implicit f: A => Parser[String]): ParserOps[String] = ParserOps(f(a))
  implicit def regex (r: Regex): Parser[String]

  case class ParserOps [A] (p: Parser[A]) {
    def | [B >: A] (p2: Parser[B]): Parser[B] = self.or(p, p2)
    def or [B >: A] (p2: Parser[B]): Parser[B] = self.or(p, p2)
    def many: Parser[List[A]] = self.many(p)
    def map [B] (f: A => B): Parser[B] = self.map(p)(f)
    def ** [B] (p2: Parser[B]): Parser[(A, B)] = self.product(p, p2)
    def product [B] (p2: Parser[B]): Parser[(A, B)] = self.product(p, p2)
    def flatMap [B] (f: A => Parser[B]): Parser[B] = self.flatMap(p)(f)
  }

  object Laws {
    def equal [A] (p1: Parser[A], p2: Parser[A]) (in: Gen[String]): Prop =
      forAll (in) (s => run(p1)(s) == run(p2)(s))

    def mapLaw [A] (p: Parser[A]) (in: Gen[String]): Prop =
      equal(p, p.map(a => a))(in)
  }
}
