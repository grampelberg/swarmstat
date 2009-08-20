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

/* Model to store the available monitoring feeds.
 */

package org.saunter.swarmstat.model

import net.liftweb.mapper._
import net.liftweb.http._

class RSSFeed extends LongKeyedMapper[RSSFeed] with IdPK {
  def getSingleton = RSSFeed

  object url extends MappedPoliteString(this, 256) {
    override def validations = validURL _ :: super.validations

    def validURL(in: String): List[FieldError] =
      if (in.startsWith("http://")) Nil
      else List(FieldError(this, <b>Please enter a valid URL</b>))
  }
}

object RSSFeed extends RSSFeed with LongKeyedMetaMapper[RSSFeed]
