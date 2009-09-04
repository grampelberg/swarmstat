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

package org.saunter.swarmstat.model

import net.liftweb.mapper._

import org.saunter.swarmstat.util._

class Tracker extends KeyedMapper[String, Tracker] {
  def getSingleton = Tracker
  def primaryKeyField = uuid

  // Fields
  object uuid extends UUID(this)
  object announce_url extends MappedPoliteString(this, 128)
  object torrents extends HasManyThrough(this, Torrent, Relationship,
                                         Relationship.torrent,
                                         Relationship.tracker)

  // Convenience methods
  def addTorrent(tor: Torrent) = {
    Relationship.create.tracker(this).torrent(tor).save
    torrents.reset
  }

  def removeTorrent(tor: Torrent) = {
    Relationship.find(By(Relationship.torrent, tor.info_hash),
                      By(Relationship.tracker, this.uuid)).foreach(
                        Relationship.delete_!)
    torrents.reset
  }
}

object Tracker extends Tracker with KeyedMetaMapper[String, Tracker]
