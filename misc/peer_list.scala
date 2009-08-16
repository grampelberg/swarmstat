/* Copyright (C) 2009 Thomas Rampelberg <pyronicide@gmail.com>

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

/* Take a scrape, fetch the info_hashes and return all peers associated with
 * each hash.
 * Usage:
 * peer_list.scala scrape remote_source
 */

import java.net._

import scala.collection.immutable._
import scala.io._

import org.saunter.bencode._
import scalax.io._
import scalax.io.Implicits._
import scalax.data.Implicits._

def get_map(data: Any): Map[String, Map[String, Int]] =
  data match {
    case x: Map[String, Map[String, Map[String, Int]]] => {
      x.get("files") match {
        case Some(x) => x
        case _ => HashMap()
      }
    }
    case _ => HashMap()
  }

def parse_data(file: String): Map[String, Map[String, Int]] =
  Bencode.parse(args(0).toFile.reader.lines.mkString("\n")) match {
    case Some(x) => get_map(x)
    case _ => HashMap()
  }

def parse_ips(peers: String): List[String] = {
  val parse_ips = List.range(0, peers.length/6).map(
    x => peers.slice(0+6*x, 6+6*x) )
  parse_ips.map( x => InetAddress.getByAddress(x.slice(0, 4).toArray.map( x => x.toByte) ).getHostAddress )
}

def get_peers(info_hash: String, total_peers: Int) = {
  val url = "http://tracker.openbittorrent.com/announce?info_hash=" + URLEncoder.encode(info_hash) + "&numwant=" + total_peers
  val data = InputStreamResource.url(url).reader.lines.mkString("\n")
  Bencode.parse(data) match {
      case Some(x: Map[String, _]) => x.get("peers") match {
        case Some(x: String) => parse_ips(x)
        case _ => List()
      }
      case _ => List()
    }
}

def count(map: Map[String, Int], key: String): Int =
  map.get(key) match {
    case Some(x) => x
    case _ => 0
  }

val data = parse_data(args(0))
data.elements.foreach( x => x match {
  case Tuple2(x, y) => println(get_peers(x, count(y, "complete") + count(y, "incomplete")))
})
