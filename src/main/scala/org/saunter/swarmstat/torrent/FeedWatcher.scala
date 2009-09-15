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

import net.lag.logging.Logger
import net.liftweb.mapper._
import net.liftweb.util.{ActorPing,Box,Full,Empty,Failure}
import net.liftweb.util.Helpers.TimeSpan
import scala.actors.Actor
import scala.actors.Actor._
import scala.xml._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.torrent.feeds._
import org.saunter.swarmstat.util._

case class NewSource(x: Info)
case class NewTorrent(x: Info)
case class Update

// XXX - How do watchers get removed? I could see this being bad.
object FeedWatcher extends Actor with Listener {
  val feeds: List[Actor] = List(EZTV, Mininova, Isohunt)

  def act = loop {
    react(handler orElse {
      case NewSource(x: Info) => listeners.foreach(_ ! NewSource(x))
      case NewTorrent(x: Info) => listeners.foreach(_ ! NewTorrent(x))
    })
  }

  Logger.get.info("FeedWatcher starting")
  this.start
}

// XXX - Should probably be tracking *what* feed a torrent came from.
trait Feed extends Actor {
  val feed: String
  val startup_delay = 1000 * 5
  val timer = 1000 * 60 * 5 // 5 minutes

  def act = loop {
    react {
      case Update => Logger("Feed").info("%s: Update", feed); update
    }
  }

  // Convenience method to allow friendly fetching across all feeds.
  def get_data(url: String): Option[NodeSeq] =
    try { Some(XML.load(WebFetch.url_stream(url))) } catch {
      case e => {
        Logger("Feed").debug("%s: get_data: %s", feed, e)
        None
      }
    }

  def fetch: Seq[String]

  // XXX - This is bad, shouldn't be hard coded.
  def update =
    fetch.foreach(store(_))

  def store(raw: String): Unit =
    try {
      if (validate(raw) && new_feed_?(raw)) {
        val tor = new Info(raw)
        if (tor.name == "") { return }
        if (new_torrent_?(tor.info_hash_raw)) {
          Torrent.create.info_hash(tor.info_hash_raw).name(tor.name).creation(
            tor.creation).save
          FeedWatcher ! NewTorrent(tor)
        }
        TorrentSource.create.url(raw).torrent(tor.info_hash_raw).save
        FeedWatcher ! NewSource(tor)
      }
    } catch {
      case e: java.net.SocketException =>
        Logger("Feed").info("%s: slow host: %s", feed, raw)
      case e => Logger("Feed").error(e, "%s: store: %s", feed, raw)
    }

  def new_feed_?(tor: String): Boolean =
    TorrentSource.find(By(TorrentSource.url, tor)) match {
      case Full(_) => false
      case Empty => true
      case _ => true
    }

  def new_torrent_?(hash: String): Boolean =
    Torrent.find(By(Torrent.info_hash, hash)) match {
      case Full(_) => false
      case Empty => true
      case _ => true
    }

  def validate(url: String) =
    url.startsWith("http://")

  Logger("Feed").info("%s: starting", feed)
  this.start
  Logger("Feed").info("%s: updates every %s", feed, timer)
  ActorPing.scheduleAtFixedRate(this, Update, TimeSpan(startup_delay),
                                TimeSpan(timer))
}
