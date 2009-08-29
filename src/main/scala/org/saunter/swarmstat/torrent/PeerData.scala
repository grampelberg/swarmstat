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


object PeerData extends Actor {
  def act = loop {
    react {
      NewTorrent(x: Info) => watch_peers(x)
    }
  }

  def watch_peers(torrent: Info) =
    torrent.peers.foreach(save_peer(_))

  def save_peer(peer: String) = {
    Peer.create.ip.apply(
  }

  MasterFeed ! AddWatcher(this)
  this.start
}

case class NewPeer(x: String)
