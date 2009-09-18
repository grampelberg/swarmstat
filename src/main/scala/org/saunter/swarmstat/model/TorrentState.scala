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

class TorrentState extends LongKeyedMapper[TorrentState] with IdPK
    with OneToMany[Long, TorrentState] {
  def getSingleton = TorrentState

  object relationship extends MappedLongForeignKey(this, Relationship)
  object seed_count extends MappedInt(this)
  object peer_count extends MappedInt(this)
  object downloaded extends MappedInt(this)
  object when extends MappedDateTime(this) {
    override def defaultValue = timeNow
  }
  object peers extends MappedOneToMany(Peer, Peer.state)
}

object TorrentState extends TorrentState with LongKeyedMetaMapper[TorrentState]
