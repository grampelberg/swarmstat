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

package org.saunter.swarmstat.snippet

import java.text.SimpleDateFormat
import java.util.Date

import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.{NodeSeq, Text}

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.util._

class TorrentInfo {
  private object selectedTorrent extends RequestVar[Box[Torrent]](Empty)

  def list(html: NodeSeq) = {
    val max_view = 20

    def query = Torrent.findAll()

    def format_list(html: NodeSeq) =
      query.flatMap(td =>
        bind("torrent", html,
             "name" --> SHtml.link("/torrents/detail",
                                   () => selectedTorrent(Full(td)),
                                   Text(td.name)),
             "seed_count" --> td.relationships.foldLeft(0)(
               _+_.states.foldLeft(0)(_+_.seed_count.toInt)),
             "peer_count" --> td.relationships.foldLeft(0)(
               _+_.states.foldLeft(0)(_+_.peer_count.toInt)),
             "downloaded" --> td.relationships.foldLeft(0)(
               _+_.states.foldLeft(0)(_+_.downloaded.toInt)),
             "seen" --> td.relationships.foldLeft(0)(
               _+_.states.foldLeft(0)(_+_.peers.length))))

    format_list(html)
  }

  def detail(html: NodeSeq): NodeSeq = {
    selectedTorrent.is.map(tor =>
      bind("torrent", html,
           "name" -> Text(tor.name),
           "creation" -> Text(tor.creation.toString),
           "sources" -> tor.sources.flatMap(t =>
             bind("t", chooseTemplate("source", "list", html),
                  "hostname" -> Text(Conversion.hostname(t.url.toString)))),
           "trackers" -> tor.trackers.flatMap(t =>
             bind("t", chooseTemplate("tracker", "list", html),
                  "hostname" -> Text(t.hostname.toString)))
         )).openOr(NodeSeq.Empty)
  }

  def date_format(d: Date) =
    (new SimpleDateFormat("k:mm MM/dd")).format(d)

  def states(html: NodeSeq): NodeSeq = {
    selectedTorrent.is.map(tor =>
      tor.relationships.flatMap(rel =>
        rel.states.flatMap(st => {
          bind("state", html,
               "tracker" --> Tracker.find(rel.tracker).map(
                 _.hostname.toString).openOr("XXX"),
               "when" --> st.when,
               "seed_count" --> st.seed_count.toInt,
               "peer_count" --> st.peer_count.toInt,
               "downloaded" --> st.downloaded.toInt,
               "seen" --> st.peers.length)
        }
                         ))).openOr(NodeSeq.Empty)
  }
}
