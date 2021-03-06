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

/* Ability to manipulate torrents
 */

package org.saunter.swarmstat.snippet

import java.text.SimpleDateFormat
import java.util.Date

import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.{NodeSeq, Text}

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.torrent._
import org.saunter.swarmstat.torrent.feeds._

class TorrentSnippet {

  def viewstate(html: NodeSeq) = {
    <lift:comet type="DynamicStateView" name={toLong(S.param("id")).toString}>
      <state:view>Loading...</state:view>
    </lift:comet>
  }

  def viewlist(html: NodeSeq) = {
    <lift:comet type="DynamicTorrentView" name={toLong(S.param("id")).toString}>
      <torrent:view>Loading...</torrent:view>
    </lift:comet>
  }

  def addTorrent(html: NodeSeq) = {
    var url = ""

    def save() = {
      S.notice("URL: " + url)
      FeedWatcher.feeds(1) match {
        case x: BasicFeed => x.store(url)
        case _ => println("foo!")
      }
    }

    bind("torrent", html,
         "url" -> text(url, url=_),
         "submit" -> submit(?("New"), () => save))
  }
}
