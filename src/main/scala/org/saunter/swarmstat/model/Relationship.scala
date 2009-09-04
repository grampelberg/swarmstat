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

// XXX - Get rid of the PK and replace with a uuid.
class Relationship extends LongKeyedMapper[Relationship] with IdPK {
  def getSingleton = Relationship

  // Fields
  object torrent extends UUIDForeignKey(this, Torrent)
  object tracker extends UUIDForeignKey(this, Tracker)
}

object Relationship extends Relationship with LongKeyedMetaMapper[Relationship]
