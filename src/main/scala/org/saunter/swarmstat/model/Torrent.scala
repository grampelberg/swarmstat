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

import net.liftweb._
import net.liftweb.mapper._

// XXX - What do the IdPKs end up being ... need to be UUIDs
class Torrent extends LongKeyedMapper[Torrent] with IdPK {
  def getSingleton = Torrent

  // Fields
  object info_hash extends MappedPoliteString(this, 20)
  object creation extends MappedDateTime(this)
  object name extends MappedPoliteString(this, 128)
  object trackers extends HasManyThrough(this, Tracker, Relationship,
                                         Relationship.torrent,
                                         Relationship.tracker)

  // Convenience Methods
  def addTracker(track: Tracker) = {
    Relationship.create.tracker(track).torrent(this).save
    trackers.reset
  }

  def removeTracker(track: Tracker) = {
    Relationship.find(By(Relationship.torrent, this.id),
                      By(Relationship.tracker, track.id)).foreach(
                        Relationship.delete_!)
    trackers.reset
  }
}

object Torrent extends Torrent with LongKeyedMetaMapper[Torrent]
