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

import net.liftweb.util.Helpers._

import org.saunter.bencode._

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
class Scrape(announce_url: String, info_hashes: List[String]) {

  // There has gotta be a better way to do this.
  val url =
    appendParams(announce_url.replaceAll("announce", "scrape"),
                 info_hashes.map(("info_hash", _)))

  def fetch =
    BencodeDecoder.decode(WebFetch(url)) match {
      case Some(x: Map[String, _]) => x
      case _ => Map()
    }

  var data =
    fetch match {
      case Some(x: Map[String, _]) => x.get("files") match {
        case Some(x: Map[String, Map[String, Int]]) => x
        case _ => Map()
      }
      case _ => Map()
    }

  def torrent_stats(info_hash: String) = None
  def torrent_stats(info_hash: Seq[Byte]) =
    data.get(info_hash.mkString) match {
      case Some(x: Map[String, Int]) => Some(x)
      case _ => Map()
    }
}


class Announce(announce_url: String, info_hash: String) {

  val url =
    appendParams(announce_url,
                 List(("info_hash", info_hash),
                    ("numwant", 10000), ("compact", 1)))

  def fetch =
    BencodeDecoder.decode(WebFetch(url)) match {
      case Some(x: Map[String, _]) => x
      case _ => Map()
    }

  var data = fetch

  def fetch_peers =
    data.get("peers") match {
      case Some(x: Map[String, _]) => Some(get_peer_list(x))
      case _ => None
    }

  def get_peer_list(peers: String): List[String] =
    List.range(0, peers.length/6).map(x => get_ip(peers.slice(0+6*x, 6+6*x)))

  def get_ip(info: String): String =
    InetAddress.getByAddress(
      info.slice(0, 4).toArray.map(_.toByte)).getHostAddress

}

class State(tor: Info) {

  val scrape = tor.url match {
    case Some(x) => new Scrape(x, List(tor.info_hash))
    case _ => None
  }

  val announce = tor.url match {
    case Some(x) => new Announce(x, tor.info_hash)
    case _ => None
  }

  def scrape_info(stat: String) =
    scrape.torrent_stats(tor.info_hash_raw) match {
      case Some(x) => x.get("complete") match {
        case Some(x) => x
        case _ => 0
      }
      case _ => 0
    }

  def seed_count = scrape_info("complete")
  def peer_count = scrape_info("incomplete")
  def total_count = scrape_info("downloaded")

  def peers =
    announce.fetch_peers
}
