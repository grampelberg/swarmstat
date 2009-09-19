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
import net.liftweb.util.Helpers._

import org.saunter.swarmstat.util._

class TorrentSource extends LongKeyedMapper[TorrentSource] with IdPK with Ordered[TorrentSource] {
  def getSingleton = TorrentSource

  object torrent extends MappedLongForeignKey(this, Torrent) with LongMappedForeignMapper[TorrentSource, Torrent]
  object url extends MappedPoliteString(this, 128)
  object first_seen extends MappedDateTime(this) {
    override def defaultValue = timeNow
  }

  def compare(that: TorrentSource) = this.url.is.toInt -  that.url.is.toInt
}

object TorrentSource extends TorrentSource
  with LongKeyedMetaMapper[TorrentSource]
