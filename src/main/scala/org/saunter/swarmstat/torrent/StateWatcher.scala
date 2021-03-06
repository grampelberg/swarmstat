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
import org.saunter.swarmstat.util._

case class NewState(seed_count: Int, peer_count: Int, total_count: Int)
case class WatchTorrent(tor: Info)

object StateWatcher extends Actor with Listener {

  def act = loop {
    react(handler orElse {
      case NewState(s: Int, p: Int, t: Int) => {
        Logger("StateWatcher").info("NewState")
        listeners.foreach(_ ! NewState(s, p, t))
      }
      case NewTorrent(x: Info) => {
        Logger("StateWatcher").info("NewTorrent: %s", x.name)
        new State(x)
      }
    })
  }

  Logger("StateWatcher").info("starting")
  FeedWatcher ! Add(this)
  this.start
}
