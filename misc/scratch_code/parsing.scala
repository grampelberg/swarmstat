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
import org.saunter.bencode._
import java.security.MessageDigest
import org.saunter.swarmstat.util.WebFetch

val q = ReaderResource.url("http://tracker.openbittorrent.com/announce?info_hash=%FC%E7%8E%E8%FE%12%D6%CBG%D0%B2%C4e%A6%9A%AEBT4%21&numwant=10000&compact=1&peer_id=-SW0001-437850138358").slurp
val w = BencodeDecoder.decode(q) match {
  case Some(x: Map[String, _]) => x
}
val e = w.get("downloaded") match {
  case Some(x: Int) => x
  case Some(x: Long) => x
  case _ => 0
}
println(e)

val r = w match {
  case x: Map[String, String] if x.contains("peers") => x.get("peers")
  case x: Map[String, String] if x.contains("peers6") => x.get("peers6")
  case _ => None
}
println(r)
