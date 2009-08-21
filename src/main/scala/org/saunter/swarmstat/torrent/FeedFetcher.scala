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

import net.liftweb.mapper._
import scala.actors.Actor
import scala.actors.Actor._
import scala.xml._

import org.saunter.swarmstat.model._

class Foo extends Actor {
  var watchers: List[Actor] = List()

  def act = loop {
    react {
      case AddFeed(s) => fetch(s)
      case AddFeedWatcher(me) => add_watcher(me)
    }
  }

  def fetch(url: String) =
    (XML.load(url) \\ "item").map(_ \ "link").map(_.text).filter(
      _.startsWith("http://")).map(_+"/download.torrent").foreach(store(_))

  def store(raw: String) = {
    try {
      val torrent = Info.from_url(raw)
      val torrent_model = Torrent.create

      torrent_model.info_hash.apply(torrent.info_hash)
      torrent_model.creation.apply(torrent.creation)
      torrent_model.name.apply(torrent.name)
      torrent_model.save
      println("Added: " + torrent_model.name)
      watchers.foreach(_ ! TorrentUpdateSingle(torrent.name))
    } catch {
      case _ => println("Failed: " + raw)
    }
  }

  def add_watcher(me: Actor) = {
    val entries = Torrent.findAll(OrderBy(Torrent.id, Descending), MaxRows(10))
    reply(TorrentUpdateBatch(entries.map(_.name)))
    watchers = me :: watchers
  }

  this.start
}

object FeedFetcher extends Foo {
  override def fetch(url: String) =
    (XML.load("http://www.ezrss.it/feed") \\ "item").map(_ \ "link").map(
      _.text).filter(_.startsWith("http://")).foreach(store(_))
}

// object EZTVFetcher extends FeedFetcher {
//   override def fetch(url: String) =
//     (XML.load("http://www.ezrss.it/feed") \\ "item").map(_ \ "link").map(
//       _.text).filter(_.startsWith("http://")).foreach(store(_))
// }

case class AddFeed(s: String)
case class AddFeedWatcher(me: Actor)
case class TorrentUpdateSingle(s: String)
case class TorrentUpdateBatch(xs: List[String])
