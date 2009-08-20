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

package org.saunter.swarmstat.comet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import scala.xml._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.torrent._

class DynamicTorrentView extends CometActor {
  override def defaultPrefix = Full("torrent")
  var torrents: List[String] = List()

  def torrentview(e: String): Node = {
    <li>{e}</li>
  }

  def render =
    bind("view" -> <ul>{torrents.flatMap(e => torrentview(e))}</ul>)

  override def localSetup = {
    (FeedFetcher !? AddFeedWatcher(this)) match {
      case TorrentUpdate(entries) => this.torrents = entries
    }
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case TorrentUpdate(entries) => this.torrents = entries; reRender(false)
  }
}
