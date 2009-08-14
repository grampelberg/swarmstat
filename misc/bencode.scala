/* Scala Bencode Parser.
 *
 *
 * Copyright (C) 2009 Thomas Rampelberg <pyronicide@gmail.com>

 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

import scala.collection.immutable._
import scala.io.Source
import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._
import scala.util.parsing.input.CharArrayReader.EofCh

class Lexer extends StdLexical {
  override def token: Parser[Token] =
    ( number ^^ NumericLit
    | string ^^ processIdent
    | dstart ^^ Keyword
    | lstart ^^ Keyword
    | expr_end ^^ Keyword )

  // Integer: i10e | i-10e
  def number = 'i' ~> signed_int <~ 'e'
  def signed_int = opt('-') ~ rep1(digit) ^^ {
    case x ~ y => (optString("", x) :: y) mkString "" }

  private def optString[A](pre: String, a: Option[A]) = a match {
    case Some(x) => pre + x.toString
    case None => ""
  }

  // String: length:string -> 3:foo
  def string = len >> { x => repN(x.toInt, char) ^^ {
    case y => y mkString "" } }
  def len = rep1(digit) <~ ':' ^^ { case x => x mkString "" }
  // Any char ..... not sure if this is actually valid or not.
  def char = elem("char", x => true)

  // Keywords for start/end of lists and dictionaries
  def dstart = elem("dictionary start", d => d == 'd') ^^ {
    case x => x toString }
  def lstart = elem("list start", d => d == 'l') ^^ { case x => x toString }
  def expr_end = elem("expression end", d => d == 'e') ^^ {
    case x => x toString }
}

object Bencode extends StdTokenParsers {
  type Tokens = Lexer
  val lexical = new Tokens

  // Probably should be doing some kind of logging here.
  def parse(input: String): Option[Any] =
    phrase(doc)(new lexical.Scanner(input)) match {
      case Success(result, _) => Some(result)
      case Failure(msg, _) => println(msg); None
      case Error(msg, _) => println(msg); None
    }

  def doc: Parser[Any] = ( num | str | list | dict )
  def num = numericLit ^^ { case x => x.toInt }
  def str = ident ^^ { case x => x }
  def list = "l" ~> rep1(doc) <~ "e" ^^ { case x => x }
  def dict = "d" ~> rep1(str ~ doc) <~ "e" ^^ {
    case x => HashMap(x map { case x ~ y => (x, y) }: _*) }
}

val test_scrape = Source.fromFile("test.scrape", "ISO-8859-1").getLines.mkString
println(Bencode.parse(test_scrape))
