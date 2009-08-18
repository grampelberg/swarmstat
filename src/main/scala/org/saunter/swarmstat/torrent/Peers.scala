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

/* Library for doing all kinds of interesting operations on peers.
 */

package org.saunter.swarmstat.torrent

import java.io._
import java.net._
import java.util.Random
import java.security._

import org.saunter.bencode._
import scala.io._
import scalax.io._
import scalax.io.Implicits._
import scalax.data.Implicits._

object Peers {

  /**
   * Take a torrent in string representation and return a list of all its peers.
   *
   * @param torrent a bencoded string representation of a torrent.
   * @return        a list of ip addresses in their string representation.
   */
  // XXX - This is UGLY, refactor .... even if it might be premature
  // optimization
  def fetch(torrent: String): List[String] = {
    val torrent_object = Bencode.parse(torrent)
    val tracker = torrent_object match {
      // wouldn't it be great if announce_list was handled as well?
      case Some(x: Map[String, _]) => x.get("announce") match {
        // The protocol shouldn't require http but accept udp as well.
        case Some(y: String) if y.startsWith("http") => y
        case _ => ""
      }
      case _ => ""
    }
    val info_hash = torrent_object match {
      case Some(x: Map[String, _]) => get_info_hash(x.get("info"))
      case _ => ""
    }
    // XXX - This is just gonna return a small number of peers ... set the
    // "numwant" especially high or hit the tracker twice if too few peers come
    // back?
    val url = tracker + "?info_hash=" + info_hash
    val data = InputStreamResource.url(url).reader.lines.mkString("\n")
    Bencode.parse(data) match {
      // XXX - What about trackers with peers6?
      case Some(x: Map[String, _]) => x.get("peers") match {
        case Some(x: String) => get_peer_list(x)
        case _ => List()
      }
      case _ => List()
    }
  }

  def fetch(torrent: InputStreamResource): List[String] =
    fetch(torrent.reader.slurp)

  def from_url(torrent: String): List[String] =
    fetch(InputStreamResource.url(torrent))

  def from_file(torrent: String): List[String] =
    fetch(InputStreamResource.file(torrent))

// XXX - Aborted try at getting announce_list to work ..
//   def trackers(tracker_list: Option[_]): Option[_] =
//     tracker_list match {
//       // Probably should make the protocol configurable.
//       case Some(x: List[List[String]]) => Some(x.flatMap(
//         y => y.filter( z => z.startsWith("http"))))
//       case Some(x: String) => Some(List(x))
//       case _ => None
//     }

//   def pick_tracker(tracker_list: Option[_]): String = {
//     val trackers = tracker_list match {
//       case Some(x: List[List[String]]) =>
//       case _ => List("")
//     }
//     trackers( (new Random).nextInt(trackers.length) )
//   }

  // XXX - This needs to GO AWAY it's been moved into Info.scala since this
  // shouldn't be calculating the info hash anyways.
  def get_info_hash(info_obj: Option[_]): String =
    info_obj match {
      case Some(x) => hex_encoder(MessageDigest.getInstance("SHA").digest(
        Bencode.encode(x).getBytes))
      case _ => ""
    }

  def get_peer_list(peers: String): List[String] =
    List.range(0, peers.length/6).map(x => get_ip(peers.slice(0+6*x, 6+6*x)))

  def get_ip(info: String): String =
    InetAddress.getByAddress(
      info.slice(0, 4).toArray.map(_.toByte)).getHostAddress

  // XXX - Remove along with get_info_hash or make a util (if it gets used other places).
  def hex_encoder(input: Array[Byte]): String =
    input.map( x => (0xFF & x) match {
      case x if x < 16 => "0" + Integer.toHexString(x)
      case x => Integer.toHexString(x)
    } ).foldLeft("")( (x, y) => x + "%" + y )
}
