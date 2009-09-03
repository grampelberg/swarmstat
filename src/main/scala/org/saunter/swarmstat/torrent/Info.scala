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

/* Library to fetch information on a specific torrent.
 */

package org.saunter.swarmstat.torrent

import java.net.URI
import java.util.Date
import java.util.Random
import java.security.MessageDigest

import org.saunter.bencode._
import org.saunter.swarmstat.util._
import scalax.io.ReaderResource

class TorrentPart(the_path: String, the_size: Long) {
  val path = the_path
  val size = the_size

  override def toString() =
    path + ":" + size
}

class Info(url_ext: List[String]) {

  def this(url_ext: String) =
    this(List(url_ext))

  var urls = url_ext
  def url =
    if (urls.length > 0) {
      Some(urls((new Random) nextInt urls.length))
    } else { None }

  val encoded_str: String =
    url match {
      case Some(x) =>
        try {
          WebFetch.url(x)
        } catch { case _ => urls -= x; encoded_str }
      case None => ""
    }

  // Not sure the pieces should be kept in memory since they'll probably never
  // be used .... maybe need to sanitize this.
  val struct = BencodeDecoder.decode(encoded_str).get
  // This is going to end up getting used all over the place and I'd like to
  // make sure it only gets calculated once.
  def info_hash: String = WebFetch.escape(info_hash_raw)
  val info_hash_raw: String =
    struct match {
      case x: Map[String, _] => x.get("info") match {
        case Some(x) => MessageDigest.getInstance("SHA").digest(
          BencodeEncoder.encode(x).getBytes).mkString
        // XXX - Really need to raise an error in this case.
        case _ => ""
      }
    }

  def comment = get_value("comment")
  def encoding = get_value("encoding")
  // Maybe I should test the trackers before returning them? Could make this
  // immutable and set at startup if that was the case (especially since some
  // tracker entries don't really exist).
  var trackers = get_value("announce") :: announce_list
  def tracker =
    if (trackers.length > 0) {
      Some(trackers((new Random) nextInt trackers.length))
    } else { None }

  def creation = struct match {
    case x: Map[String, Long] => x.get("creation date") match {
      // *grumbles about millisecond accuracy*
      case Some(x: Long) => new Date(x * 1000L)
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

  def size = files.foldLeft(0L)( (x,y) => x + y.size )

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

  def build_part(file_list: Map[String, _]) = {
    val path = file_list.get("path") match {
      case Some(x: List[String]) => x.mkString("/")
      case _ => ""
    }
    val size = file_list.get("length") match {
      case Some(x: Long) => x
      case _ => 0L
    }
    new TorrentPart(path, size)
  }
}

