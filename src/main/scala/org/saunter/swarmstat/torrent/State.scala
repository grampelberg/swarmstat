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


import org.saunter.bencode._

/*
 * group scrape requests by torrent
 * lookup multiple trackers
 */
class Scrape(announce_url: String, info_hashes: List[String]) {

  val url =
    announce_url.replaceAll("announce", "scrape") + "?info_hash=" +
      info_hashes.mkString("&info_hash=")

  def fetch =
    BencodeDecoder.decode(WebFetch(url))

  val data =
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
      case Some(x: Map[String, Int]) => x
      case _ => Map()
    }
}


class State(tor: Info) {

  val scrape = tor.url match {
    case Some(x) => new Scrape(x, tor.info_hash_raw)
    case _ => None
  }

  def torrent_info(stat: String) =
    scrape.torrent_stats(tor.info_hash_raw).get("complete") match {
      case Some(x) => x
      case _ => 0
    }

  val seed_count = torrent_info("complete")
  val peer_count = torrent_info("incomplete")
  val total_count = torrent_info("downloaded")

}
