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

/* Library for doing all kinds of interesting operations on peers.
 */

package org.saunter.swarmstat.torrent

import java.net.InetAddress
import java.security.MessageDigest

import org.saunter.bencode._
import scalax.io.InputStreamResource

class Peers(torrent: Info) {

  def current = {
    val url = (torrent.tracker + "?info_hash=" + torrent.info_hash
               + "&numwant=10000")
    try {
      val data = InputStreamResource.url(url).reader.slurp
      BencodeDecoder.decode(data) match {
        case Some(x: Map[String, _]) => x.get("peers") match {
          case Some(x: String) => get_peer_list(x)
          case _ => List()
        }
        case _ => List()
      }
    } catch {
      case _ => List()
    }
  }

  def get_peer_list(peers: String): List[String] =
    List.range(0, peers.length/6).map(x => get_ip(peers.slice(0+6*x, 6+6*x)))

  def get_ip(info: String): String =
    InetAddress.getByAddress(
      info.slice(0, 4).toArray.map(_.toByte)).getHostAddress

}

object Peers {
  def from_url(torrent: String) =
    new Peers(new Info(torrent))
}
