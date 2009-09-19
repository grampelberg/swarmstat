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

import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import org.saunter.swarmstat.util._

class Torrent extends LongKeyedMapper[Torrent] with IdPK
    with OneToMany[Long, Torrent] {
  def getSingleton = Torrent

  // Fields
  object info_hash extends MappedPoliteString(this, 20)
  object creation extends MappedDateTime(this) {
    override def defaultValue = timeNow
  }
  object name extends MappedPoliteString(this, 128)
  object sources extends MappedOneToMany(TorrentSource, TorrentSource.torrent, OrderBy(TorrentSource.url, Ascending)) with Owned[TorrentSource] with Cascade[TorrentSource]
}

object Torrent extends Torrent with LongKeyedMetaMapper[Torrent]
