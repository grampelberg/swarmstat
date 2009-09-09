/* Copyright (C) 2009 Thomas Rampelberg <pyronicide@saunter.org>

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

import org.saunter.bencode._
import scalax.io._
import scalax.io.Implicits._
import scalax.data.Implicits._
import java.security._
import java.net._
import java.lang.Integer

val torrent_obj = Bencode.parse("data/test.torrent".toFile.reader.slurp) match {
  case Some(x: Map[String, _]) => x
}
val info_obj = torrent_obj.get("info") match {
  case Some(x: Map[String, _]) => x
}
val info_encoded = Bencode.encode(info_obj)
println(info_encoded.slice(0, 500))
val info_bytes = info_encoded.getBytes
val hasher = MessageDigest.getInstance("SHA")
hasher.reset
hasher.update(info_bytes)
val digest = hasher.digest()
println(digest.toString)
val digest_hex = digest.map( x => (0xFF & x) match {
  case x if x < 16 => "0" + Integer.toHexString(x)
  case x => Integer.toHexString(x)
}).foldLeft("")( (x, y) => x + "%" + y )
println(digest_hex)


