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

import java.net.InetAddress
import java.nio.ByteBuffer

import net.liftweb.mapper._
import net.liftweb.util.{ActorPing,Failure,Full,Empty}
import net.liftweb.util.Helpers.TimeSpan
import scala.actors.Actor
import scala.actors.Actor._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.util._

case class NewPeer(ip: String)

// XXX - Need a global "failed tracker" place to do filtering on tracker lists.
// XXX - Why not spawn a bunch of peer watchers for each tracker in the list.
// Theoretically, I'll get back different peers for each tracker. For failed
// trackers, just kill the actor.
object PeerWatcher extends Actor with Listener {
  private var peer_monitors: List[Actor] = List()
  // Total number of torrents each monitor should watch
  private val max_torrents = 20
  private var watch_counter = 0

  // XXX - Should updates be forceable?
  def act = loop {
    react(handler orElse {
      case NewPeer(ip: String) => notify_listeners(NewPeer(ip))
      case NewTorrent(tor: Info) => watch_torrent(tor)
    })
  }

  def notify_listeners(x: Any) =
    listeners.foreach(_ ! x)

  def total_watched(me: Actor) =
    (me !? TotalWatched) match {
      case Some(x: Int) => x
      case _ => max_torrents
    }

  def watch_torrent(tor: Info) = {
    if (peer_monitors.length == 0 || watch_counter >= max_torrents) {
      watch_counter = 0
      peer_monitors = (new PeerWatcher) :: peer_monitors
    }
    watch_counter += 1
    peer_monitors(0) ! WatchTorrent(tor)
  }

  FeedWatcher ! Add(this)
}

case class TotalWatched
case class WatchTorrent(tor: Info)

class PeerWatcher extends Actor {
  private var watched_torrents: List[Info] = List()

  def act = loop {
    react {
      case TotalWatched => Some(watched_torrents.length)
      // case WatchTorrent(tor: Info) =>
      //   watched_torrents = tor :: watched_torrents
      case WatchTorrent(tor: Info) => fetch_peers(tor)
    }
  }

  def fetch_peers(tor: Info) =
    tor.current_peers.foreach(ip => {
      println("Adding peer: " + ip)
      PeerWatcher ! NewPeer(ip)
    })

  this.start
}
