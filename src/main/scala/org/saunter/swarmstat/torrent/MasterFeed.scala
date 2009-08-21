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

import scala.actors.Actor
import scala.actors.Actor._

import org.saunter.swarmstat.model._

// XXX - How do watchers get removed? I could see this being bad.
object MasterFeed extends Actor {
  var watchers: List[Actor] = List()

  def act = loop {
    react {
      case AddWatcher(me: Actor) => watchers = me :: watchers
    }
  }

  this.start
}

case class AddWatcher(me: Actor)
case class NewTorrent(s: Either[String, List[String]])
case class UpdateFeeds()
case class ActiveFeeds()

trait Feed extends Actor {
  def act = loop {
    react {
      case Update => update
    }
  }

  def fetch: List[String]

  def update =
    fetch.foreach(store(_))

  def store(raw: String) = {
    try {
      save(Info.from_url(raw))
    } catch {
      // XXX - This needs to be converted to logging. I have no idea how that
      // works. There should be some kind of failure count. If this
      // fails too many times, something isn't working, the actor should be
      // shut down and
      case e => println("Failed: " + raw + "\nBecause: " + e)
    }
  }

  def save(obj: Info) =
    Torrent.create.info_hash.apply(obj.info_hash).creation.apply(
      obj.creation).name.apply(obj.name).save

  this.start
  MasterFeed ! AddWatcher(this)
}


case class Update
