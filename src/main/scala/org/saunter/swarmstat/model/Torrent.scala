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

/* Model for torrent information.
 */

package org.saunter.swarmstat.model

import java.net.URI

import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import org.saunter.swarmstat.torrent._
import org.saunter.swarmstat.util._

class Torrent extends KeyedMapper[String, Torrent]
    with OneToMany[String, Torrent] with ManyToMany {
  def getSingleton = Torrent
  def primaryKeyField = info_hash

  // Fields
  object info_hash extends InfoHashPrimaryKey(this)
  object creation extends MappedDateTime(this) {
    override def defaultValue = timeNow
  }
  object name extends MappedPoliteString(this, 128)
  object sources extends MappedOneToMany(
    TorrentSource, TorrentSource.torrent) with Owned[TorrentSource]
  object trackers extends MappedManyToMany(Relationship, Relationship.torrent,
                                           Relationship.tracker, Tracker)
  object relationships extends MappedOneToMany(Relationship,
                                               Relationship.torrent)

  def add_source(url: String) = {
    sources += TorrentSource.create.url(url)
    sources.save
    this
  }

  def add_new_trackers(tracker_list: List[String]) = {
    tracker_list.map(x => (new URI(x)).getHost).map(
      Tracker.getOrCreate).filter(
      new_tracker_?).foreach(add_tracker)
    this
  }

  // XXX - need a NewTracker() event
  def add_tracker(tracker: Tracker) = {
    trackers += tracker
    trackers.save
    this
  }

  def new_tracker_?(tracker: Tracker) = !trackers.contains(tracker)
}

object Torrent extends Torrent with KeyedMetaMapper[String, Torrent] {
  def getOrCreate(tor: Info) =
    find(By(info_hash, tor.info_hash_raw)) getOrElse {
      FeedWatcher ! NewTorrent(tor)
      create.info_hash(tor.info_hash_raw).name(tor.name).creation(
        tor.creation).saveMe
    }
}
