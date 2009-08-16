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

package org.saunter.torrent

import java.net._
import java.util.Random

import scalax.io._

object Peers {

  /**
   * Take a torrent in string representation and return a list of all its peers.
   *
   * @param torrent a bencoded string representation of a torrent.
   * @return        a list of ip addresses in their string representation.
   */
  def fetch(torrent: String): List[String] = {
    val torrent_object = Bencode.parse(torrent)
    val tracker = torrent_object match {
      case Some(x: Map[String, _]) => pick_tracker(x.get("announce-list"))
      case _ => ""
    }
  }

  def fetch(torrent: URL): List[String] =
    fetch(InputStreamResource.url(torrent).reader.lines.mkString("\n"))

  def fetch(torrent: Source): List[String] =
    fetch(torrent.getLines.mkString)

  def pick_tracker(tracker_list: Option[_]): String = {
    val trackers = tracker_list match {
      // Probably should make the protocol configurable.
      case Some(x: List[List[String]]) => x.flatMap(
        y => y.filter( z => z.startsWith("http")))
      case _ => List("")
    }
    trackers( (new Random).nextInt(trackers.length) )
  }
}
