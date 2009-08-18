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

/* Library to fetch information on a specific torrent.
 */

package org.saunter.swarmstat.torrent

import java.util.Date
import java.security.MessageDigest

import org.saunter.bencode._
import scala.io._
import scalax.io._
import scalax.io.Implicits._
import scalax.data.Implicits._

class TorrentPart(path: String, size: Int) {
  override def toString() =
    path + ":" + size
}

class Info(encoded_str: String) {

  def this(input: InputStreamResource) =
    this(input.reader.slurp)

  // Not sure the pieces should be kept in memory since they'll probably never
  // be used .... maybe need to sanitize this.
  val struct = Bencode.parse(encoded_str).get
  // This is going to end up getting used all over the place and I'd like to
  // make sure it only gets calculated once.
  val info_hash: String =
    struct match {
      case x: Map[String, _] => x.get("info") match {
        case Some(x) => hex_encoder(MessageDigest.getInstance("SHA").digest(
          Bencode.encode(x).getBytes))
        // XXX - Really need to raise an error in this case.
        case _ => ""
      }
    }
  val peers = new Peers(this)

  def comment = get_value("comment")
  def encoding = get_value("encoding")
  // Maybe I should test the trackers before returning them? Could make this
  // immutable and set at startup if that was the case (especially since some
  // tracker entries don't really exist).
  def trackers = get_value("announce") :: announce_list
  def tracker = trackers(0)
  def creation = struct match {
    case x: Map[String, Int] => x.get("creation date") match {
      // *grumbles about millisecond accuracy*
      case Some(x: Int) => new Date(x * 1000L)
      case _ => new Date()
    }
  }
  def by = get_value("created by")
  def name = get_value("info", "name")
  def files: List[TorrentPart] =
    struct match {
      case x: Map[String, _] => x.get("info") match {
        case Some(x: Map[String, _]) => x.get("files") match {
          case Some(s: List[Map[String, _]]) => s.map(
            y => build_part(y))
          case _ => List()
        }
        case _ => List()
      }
    }

  def announce_list =
    struct match {
      case x: Map[String, _] => x.get("announce-list") match {
        case Some(x: List[List[String]]) => x.flatMap(
          x => x).filter(_.startsWith("http"))
        case _ => List()
      }
    }

  def current_peers = peers.current

  // Different ways to get string values from the torrent struct.
  def get_value[A](key: String, map: Map[String, _]): A =
    map match { case x: Map[String, A] => x.get(key).get }

  def get_value(key: String): String =
    struct match { case x: Map[String, _] => get_value(key, x) }

  // XXX - Needs to be a better way to do this ... why not some kind of
  // factory? Take a look at ImmutableHashMapFactory.
  def get_value(key1: String, key2: String): String =
    struct match {
      case x: Map[String, _] => x.get(key1) match {
        case Some(x: Map[String, _]) => get_value(key2, x)
        case _ => ""
      }
    }

  // I'm really cranky about this but apparently URLEncoder.encode is a pile of
  // crap and does " " -> "+" instead of " " -> "%20" like it should.
  def hex_encoder(input: Array[Byte]): String =
    input.map( x => (0xFF & x) match {
      case x if x < 16 => "0" + Integer.toHexString(x)
      case x => Integer.toHexString(x)
    } ).foldLeft("")( (x, y) => x + "%" + y )

  def build_part(file_list: Map[String, _]) = {
    val path = file_list.get("path") match {
      case Some(x: List[String]) => x.mkString("/")
      case _ => ""
    }
    val size = file_list.get("length") match {
      case Some(x: Int) => x
      case _ => 0
    }
    new TorrentPart(path, size)
  }

}

object Info {
  def from_url(torrent: String) =
    new Info(InputStreamResource.url(torrent))

  def from_file(torrent: String) =
    new Info(InputStreamResource.file(torrent))
}
