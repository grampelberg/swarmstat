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

package org.saunter.swarmstat.snippet

import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.{NodeSeq, Text}

import org.saunter.swarmstat.model._

object TorrentOverview {

  def sitemap: List[Menu] =
    List(Menu(Loc("Torrents", List("torrents"), S.?("Torrents"),
                  User.testLogginIn)))
}

class TorrentOverivew {
  def top(html: NodeSeq) = {
    val max_view = 20 // XXX - Needs to be configurable
    html
  }
}
