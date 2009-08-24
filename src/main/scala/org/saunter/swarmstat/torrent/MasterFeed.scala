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
import net.liftweb.util.{Box,Full,Empty,Failure}
import scala.actors.Actor
import scala.actors.Actor._
import scala.xml._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.torrent.feeds._

// XXX - How do watchers get removed? I could see this being bad.
object MasterFeed extends Actor {
  var watchers: List[Actor] = List()
  val feeds: List[Actor] = List(EZTV, Mininova, Isohunt)

  def act = loop {
    react {
      case AddWatcher(me: Actor) => watchers = me :: watchers
      case UpdateFeeds => update_feeds
      case ActiveFeeds => feeds
      case NewTorrent(x: Info) => new_torrent(x)
    }
  }

  def update_feeds =
    feeds.foreach(_ ! Update)

  def new_torrent(x: Info) =
    watchers.foreach(_ ! NewTorrent(x))

  this.start
}

case class AddWatcher(me: Actor)
case class NewTorrent(x: Info)
case class UpdateFeeds
case class ActiveFeeds

// XXX - Should probably be tracking *what* feed a torrent came from.
trait Feed extends Actor {

  def act = loop {
    react {
      case Update => update
    }
  }

  // Convenience method to allow friendly fetching across all feeds.
  def get_data(url: String): NodeSeq =
    XML.load(url)

  def fetch: Seq[String]

  // XXX - This is bad, shouldn't be hard coded.
  def update =
    fetch.foreach(store(_))

  def store(raw: String): Unit = {
    try {
      if (validate(raw)) {
        val tor = Info.from_url(raw)
        if (save(tor)) MasterFeed ! NewTorrent(tor)
      }
      else println("Invalid URL received: " + raw)
    } catch {
      // XXX - This needs to be converted to logging. I have no idea how that
      // works. There should be some kind of failure count. If this
      // fails too many times, something isn't working, the actor should be
      // shut down
      case e => println("Failed: " + raw + "\n\tBecause: " + e)
    }
  }

  def save(obj: Info) =
    Torrent.find(By(Torrent.info_hash, obj.info_hash)) match {
      case Full(_) => println("Duplicate: " + obj.name); false
      case Empty => Torrent.create.info_hash.apply(
        obj.info_hash).creation.apply(obj.creation).name.apply(obj.name).save; true
      case Failure(msg, _, _) => println(
        "Failure: " + obj.name + "\n\tBecause: " + msg); false
    }

  def validate(url: String) =
    url.startsWith("http://")

  this.start
}


case class Update
