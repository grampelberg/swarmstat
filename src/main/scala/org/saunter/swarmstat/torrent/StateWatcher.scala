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
      case WatchTorrent(x: Info) => get_state(x)
    }
  }

  def tracker_id(tracker: String) =
    new_tracker_?(tracker) match {
      case Some(x: Tracker) => x.uuid
      case None => Tracker.create.hostname(tracker).saveMe.uuid
    }

  def relationship_id(tor_id: String, tracker_id: String): Long =
    new_relationship_?(tor_id, tracker_id) match {
      case Some(x: Relationship) => x.id
      case None => Relationship.create.torrent(tor_id).tracker(
        tracker_id).saveMe.id.is
    }

  def save_state(state: org.saunter.swarmstat.torrent.Tracker,
                 info_hash: String) = {
    val rel_id = relationship_id(info_hash, state.hostname)
    TorrentState.create.relationship(rel_id).seeds(
      state.seed_count).peers(state.peer_count).downloaded(
      state.total_count).save
    state.peer_list.foreach(save_peer(_, rel_id))
  }

  def save_peer(ip: String, rel_id: Long) =
    Peer.create.relationship(rel_id).id(Conversion.ip(ip)).save

  def get_state(tor: Info) = {
    val state = new State(tor)
    state.trackers.foreach(save_state(_, tor.info_hash_raw))
  }

  def new_tracker_?(track: String) =
    Tracker.find(By(Tracker.hostname, track)) match {
      case Full(x) => Some(x)
      case _ => None
    }

  def new_relationship_?(torrent_id: String, tracker_id: String) =
    Relationship.find(By(Relationship.torrent, torrent_id),
                      By(Relationship.tracker, tracker_id)) match {
      case Full(x) => Some(x)
      case _ => None
    }

  this.start
}
