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

package org.saunter.swarmstat.comet

import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import scala.xml._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.torrent._
import org.saunter.swarmstat.util._

class DynamicTorrentView extends CometActor {
  override def defaultPrefix = Full("torrent")
  val max_view = 20
  var torrents: List[String] = List()

  def torrentview(e: String): Node = {
    <li>{e}</li>
  }

  def render =
    bind("view" -> <ul>{torrents.flatMap(e => torrentview(e))}</ul>)

  override def localSetup = {
    FeedWatcher ! Add(this)
    // XXX - This isn't valid anymore!!!!!!!
    torrents = Torrent.findAll(OrderBy(Torrent.info_hash, Descending),
                               MaxRows(max_view)).map(_.name)
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case NewTorrent(x: Info) =>
      torrents = x.name :: torrents.slice(0, max_view-1); reRender(false)
  }
}
