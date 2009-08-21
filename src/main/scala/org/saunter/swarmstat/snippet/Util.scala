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

/* Utilities for the web side of things.
 */

package org.saunter.swarmstat.snippet

import scala.xml.{NodeSeq}
import org.saunter.swarmstat._
import org.saunter.swarmstat.model._

class Util {
  def in(html: NodeSeq) =
    if (User.loggedIn_?) html else NodeSeq.Empty

  def out(html: NodeSeq) =
    if (!User.loggedIn_?) html else NodeSeq.Empty
}
