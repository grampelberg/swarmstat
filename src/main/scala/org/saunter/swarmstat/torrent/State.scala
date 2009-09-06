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

import net.liftweb.util.Helpers._

import org.saunter.bencode._
import org.saunter.swarmstat.util._

/*
 * group scrape requests by torrent
 * lookup multiple trackers
 * handle failure reason if it's listed (log)
 *
 * announce specific:
 * set compact=1
 * issue a tracker id?
 * obey interval/min interval
 */
class Scrape(announce_url: String, info_hash: String) {

  val params = List(
    ("info_hash", info_hash)
  )

  // There has gotta be a better way to do this.
  val url =
    WebFetch.appendParams(announce_url.replaceAll("announce", "scrape"),
                          params)

  def fetch: Map[String, _] =
    try {
      println("\tScrape: " + announce_url)
      BencodeDecoder.decode(WebFetch.url(url)) match {
        case Some(x: Map[String, _]) => x.get("files") match {
          case Some(y: Map[String, Map[String, Int]]) => println(y); y
          case _ => Map()
        }
        case _ => Map()
      }
    } catch {
      case e => println("Failed: " + e); Map()
    }

  var data: Map[String, _] = Map()

  def torrent_stats(info_hash: String) =
    data.get(info_hash) match {
      case Some(x: Map[String, Int]) => Some(x)
      case _ => Map()
    }

  def refresh = data = fetch
}

class Announce(announce_url: String, info_hash: String) {

  val random = new Random

  def params = List(
    ("info_hash", info_hash),
    ("numwant", "10000"),
    ("compact", "1"),
    ("peer_id", "-SW0001-" + (1 to 12).map(x => random.nextInt(9)).mkString)
  )

  val url =
    WebFetch.appendParams(announce_url, params)

  def fetch: Map[String, _] =
    try {
      println(url)
      println("\tTracker: " + announce_url)
      BencodeDecoder.decode(WebFetch.url(url)) match {
        case Some(x: Map[String, _]) => println(x); x
        case _ => Map()
      }
    } catch {
      case e => Map()
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
}

class TrackerSnapshot(announce_url: String, info_hash_tmp: String) {
  val info_hash = info_hash_tmp
  val scrape = new Scrape(announce_url, info_hash)
  val announce = new Announce(announce_url, info_hash)
  val hostname = (new URI(announce_url)).getHost

  def scrape_info(stat: String) =
    scrape.torrent_stats(info_hash) match {
      case Some(x: Map[String, Int]) => x.get(stat) match {
        case Some(x) => x
        case _ => 0
      }
      case _ => 0
    }

  def seed_count = scrape_info("complete")
  def peer_count = scrape_info("incomplete")
  def total_count = scrape_info("downloaded")

  def peer_list =
    announce.fetch_peers match {
      case Some(x) => x
      case None => List()
    }

  def refresh = {
    scrape.refresh
    announce.refresh
  }
}

class State(tor: Info) {
  println("Name: " + tor.name)

  val trackers =
    tor.trackers.map(new TrackerSnapshot(_, tor.info_hash_raw))

  def seed_count = trackers.map(_.seed_count).reduceLeft(_+_)
  def peer_count = trackers.map(_.peer_count).reduceLeft(_+_)
  def total_count = trackers.map(_.total_count).reduceLeft(_+_)

  def peer_list =
    trackers.flatMap(_.peer_list)

  def refresh =
    trackers.foreach(_.refresh)
}
