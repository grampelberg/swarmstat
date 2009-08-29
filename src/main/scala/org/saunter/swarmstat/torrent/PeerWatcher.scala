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
import net.liftweb.util.{Full,Empty}
import scala.actors.Actor
import scala.actors.Actor._

import org.saunter.swarmstat.model._

// XXX - Need a global "failed tracker" place to do filtering on tracker lists.
// XXX - Why not spawn a bunch of peer watchers for each tracker in the list.
// Theoretically, I'll get back different peers for each tracker. For failed
// trackers, just kill the actor.
class PeerWatcher extends Actor {
  var torrents: List[Info] = List()
  val timer = 1000 * 60 * 5 // 5 minutes

  def act = loop {
    react {
      case Update => update_watched
      case Watch(x: Info) => torrents = x :: torrents
    }
  }

  def update_watched =
    torrents.foreach(save_peers(_))

  def save_peers(x: Info) =
    Torrent.find(By(Torrent.info_hash, x.info_hash)) match {
      case Full(y) => x.current_peers.foreach(z => peer(y, z))
      case Failure => println("You failed.")
      case Empty => println("Not possible .......")
    }

  def peer(torrent: Torrent, ip: String) = {
    Peer.create.torrent.apply(torrent.id).ip.apply(
      ByteBuffer.wrap(InetAddress.getByName(ip).getAddress).getInt).save
    PeerWatcher ! NewPeer(ip)
  }

  this.start

}

object PeerWatcher extends Actor {
  var watchers: List[Actor] = List()
  var peer_monitors: List[Actors] = List()

  def act = loop {
    react {
      case AddWatcher(me: Actor) => watchers = me :: watchers
      case NewPeer(ip: String) => new_peer(ip)
      case NewTorrent(x: Info) => add_torrent(x)
      case Update => update_peers
    }
  }

  def add_torrent(x: Info) =
    peer_monitors(0) ! NewTorrent(x)

  def new_peer(ip: String) = watchers.foreach(_ ! ip)

  def start_peers = {
    peer_monitors = (new PeerWatcher) :: peer_monitors
    Torrent.findAll(MaxRows(100)).foreach(
  }

  def update_peers = None



  start_peers
  MasterFeed ! AddWatcher(this)
  this.start
}

case class NewPeer(x: String)
case class Watch(x: Info)
