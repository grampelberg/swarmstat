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

/* Model to keep peer data.
 */

package org.saunter.swarmstat.model

import net.liftweb._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.util._
import org.saunter.swarmstat.model._

// XXX - Need to get rid of the PK here.
class Peer extends LongKeyedMapper[Peer] with IdPK {
  def getSingleton = Peer

  object torrent extends MappedLongForeignKey(this, Torrent)
  object ip extends MappedInt(this)
  object observed extends MappedDateTime(this) {
    override def defaultValue = Helpers.timeNow
  }
}

object Peer extends Peer with LongKeyedMetaMapper[Peer]
