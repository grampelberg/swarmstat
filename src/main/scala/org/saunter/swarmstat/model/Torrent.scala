/* Copyright (C) 2009 Thomas Rampelberg <pyronicide@gmail.com>

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

package org.saunter.model

import net.liftweb._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.SHtml._
import net.liftweb.util._

// XXX - What do the IdPKs end up being ... need to be UUIDs
class Torrent extends LongKeyedMapper[Torrent] with IdPK {
  def getSingleton = Torrent

  object name extends MappedPoliteString(this, 256)
  object start extends MappedDateTime(this) {
    override def defaultValue = Helpers.timeNow
  }
}

object Torrent extends Torrent with LongKeyedMetaMapper[Torrent]
