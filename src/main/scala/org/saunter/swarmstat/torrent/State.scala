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

package org.saunter.swarmstat.torrent

import java.net.InetAddress
import java.net.URI
import java.util.Random

import net.lag.logging.Logger
import net.liftweb.util.Helpers._

import org.saunter.bencode._
import org.saunter.swarmstat.util._

class Announce(announce_url: String, info_hash_tmp: String) {
  val info_hash = info_hash_tmp
  val hostname = (new URI(announce_url)).getHost

  val random = new Random

  def params = List(
    ("info_hash", info_hash),
    ("numwant", "10000"),
    ("compact", "1"),
    ("port", "3137"),
    ("peer_id", "-SW0001-" + (1 to 12).map(x => random.nextInt(9)).mkString)
  )

  val url =
    WebFetch.appendParams(announce_url, params)

  def invalid_data_?(parsed: Map[String, _], raw: String) =
    parsed.get("complete") match {
      case Some(_) => None
      case _ => Logger("Announce").debug("URL: %s\n%s", url, raw)
    }

  def fetch: Map[String, _] =
    BencodeDecoder.decode(WebFetch.url(url)) match {
      case Some(x: Map[String, _]) => invalid_data_?(x, raw); x
      case _ => invalid_data_?(Map(), raw); Map()
    }

  var data: Map[String, _] = Map()

  def fetch_peers =
    data match {
      case x: Map[String, String] if x.contains("peers") =>
        Some(get_peer_list(x.get("peers")))
      case x: Map[String, String] if x.contains("peers6") =>
        Some(get_peer_list(x.get("peers6")))
      case _ => None
    }

  def get_peer_list(peers: Option[String]): List[String] =
    peers match {
      case Some(x) =>
        List.range(0, x.length/6).map(
          y => get_ip(x.slice(0+6*y, 6+6*y)))
      case None => List()
    }

  def get_ip(info: String): String =
    InetAddress.getByAddress(
      info.slice(0, 4).toArray.map(_.toByte)).getHostAddress

  def refresh = data = fetch

  def get_value(v: String): Int = data.get(v) match {
    case Some(x: Int) => x
    case Some(x: Long) => x.toInt
    case _ => 0
  }

  def seed_count = get_value("complete")
  def peer_count = get_value("incomplete")
  def total_count = get_value("downloaded")

  def peer_list =
    fetch_peers match {
      case Some(x) => x
      case _ => List()
    }
}

class State(tor: Info) {
  Logger("State").info("%s: initializing", tor.name)

  var trackers =
    tor.trackers.filter(TrackerWatcher !? ValidTracker?(_)).map(
      new TrackerSnapshot(_, tor.info_hash_raw))

  def seed_count = trackers.map(_.seed_count).reduceLeft(_+_)
  def peer_count = trackers.map(_.peer_count).reduceLeft(_+_)
  def total_count = trackers.map(_.total_count).reduceLeft(_+_)

  def peer_list =
    trackers.flatMap(_.peer_list)

  def refresh =
    trackers = trackers.filter(x =>
      try {
        _.refresh
        true
      } catch {
        case e: java.net.SocketTimeoutException =>
          Logger("Announce").info("%s: slow host", url); true
        case e: org.apache.http.NoHttpResponseException =>
          Logger("Announce").info("%s: bad host", url); false
        case e: java.net.SocketException =>
          Logger("Announce").info("%s: bad host", url); false
        case e => Logger("Announce").error(e, "%s: fetch", url); true
      }
    )
}
