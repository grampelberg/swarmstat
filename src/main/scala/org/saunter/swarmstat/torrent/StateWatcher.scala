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
  private var state_monitors: List[Actor] = List()
  private var max_torrents = 20
  private var watch_counter = 0

  def act = loop {
    react(handler orElse {
      case NewState(s: Int, p: Int, t: Int) => {
        Logger("StateWatcher").info("NewState")
        listeners.foreach(_ ! NewState(s, p, t))
      }
      case NewTorrent(x: Info) => {
        Logger("StateWatcher").info("NewTorrent: %s", x.name)
        state_monitors.foreach(_ ! WatchTorrent(x))
      }
    })
  }

  def new_monitor = {
    Logger("StateWatcher").info("launching new monitor")
    state_monitors = (new StateWatcher) :: state_monitors
  }

  Logger("StateWatcher").info("starting")
  FeedWatcher ! Add(this)
  new_monitor
  this.start
}

class StateWatcher extends Actor {
  var torrent_state: List[State] = List()
  val startup_delay = 30*1000
  val timer = 5*60*1000

  def act = loop {
    react {
      case Update => Logger("StateMonitor").info("Update"); update
      case WatchTorrent(x: Info) => {
        Logger("StateMonitor").info("WatchTorrent")
        track_state(x)
      }
    }
  }

  def update =
    torrent_state.foreach(refresh_state(_))

  def refresh_state(st: State) =
    st.trackers.foreach(x => {
      x.refresh
      save_state(x)
      StateWatcher ! NewState(x.seed_count, x.peer_count, x.total_count)
    })

  def track_state(tor: Info) =
    torrent_state = (new State(tor)) :: torrent_state

  def tracker_id(tracker: String) =
    Tracker.getOrCreate(tracker).uuid.is

  def save_state(state: Announce) = {
    Logger("StateMonitor").info("%s:%s: Seeds:%s\tPeers:%s\tTotal:%s\tCount:%s",
                                WebFetch.escape(state.info_hash),
                                state.hostname, state.seed_count,
                                state.peer_count, state.total_count,
                                state.peer_list.length)
    TorrentState.create.set_relationship(state.info_hash, tracker_id(
      state.hostname)).seed_count(state.seed_count).peer_count(
      state.peer_count).downloaded(state.total_count).saveMe.add_peers(
        state.peer_list)
  }

  def save_peer(ip: String, rel_id: Long) =
    Peer.create.state(rel_id).ip(Conversion.ip(ip)).save

  Logger("StateMonitor").info("starting")
  this.start
  ActorPing.scheduleAtFixedRate(this, Update, TimeSpan(startup_delay),
                                TimeSpan(timer))
}
