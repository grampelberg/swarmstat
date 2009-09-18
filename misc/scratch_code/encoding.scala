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

import scalax.io._
import org.apache.commons.codec.net._
import org.saunter.bencode._
import java.security.MessageDigest
import org.saunter.swarmstat.util.WebFetch

val q = ReaderResource.url("http://www.mininova.org/get/2959179").slurp
val w = BencodeDecoder.decode(q)
val e = w match {
  case Some(x: Map[String, _]) => x.get("info")
}
val r = e match {
  case Some(x: Map[String, _]) => BencodeEncoder.encode(x)
}
val t = MessageDigest.getInstance("SHA").digest(r.getBytes)
println(t)
val y = new URLCodec()
val u = t.map(_.toChar).mkString + " "
println(y.encode(u.toArray.map(_.toByte)).map(_.toChar).mkString.replaceAll("\\+", "%20"))

println(y.encode(t).map(_.toChar).mkString)
println(WebFetch.escape(u))
