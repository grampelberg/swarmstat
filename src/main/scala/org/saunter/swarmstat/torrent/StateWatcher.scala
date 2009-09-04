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
import net.liftweb.util.{ActorPing,Box,Full,Empty,Failure}
import net.liftweb.util.Helpers.TimeSpan
import scala.actors.Actor
import scala.actors.Actor._
import scala.xml._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.util._

case class WatchTorrent(tor: Info)

object StateWatcher extends Actor with Listener {
  private var state_monitors: List[Actor] = List()
  private var max_torrents = 20
  private var watch_counter = 0

  def act = loop {
    react(handler orElse {
      case NewTorrent(x: Info) => state_monitors.foreach(_ ! WatchTorrent(x))
    })
  }

  def new_monitor =
    state_monitors = (new StateWatcher) :: state_monitors

  FeedWatcher ! Add(this)
  new_monitor
  this.start
}

class StateWatcher extends Actor {

  def act = loop {
    react {
      case NewTorrent(x: Info) => get_state(x)
    }
  }

  def get_state(tor: Info) = {
    val state = new State(tor)
    println("Seed: " + state.seed_count + "\tPeer: " + state.seed_count +
            "\tTotal: " + state.total_count)
  }


}
