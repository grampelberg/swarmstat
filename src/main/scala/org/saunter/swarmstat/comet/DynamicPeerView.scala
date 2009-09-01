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

import java.net.InetAddress
import java.nio.ByteBuffer

import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import scala.xml._

import org.saunter.swarmstat.model._
import org.saunter.swarmstat.torrent._
import org.saunter.swarmstat.util._

class DynamicPeerView extends CometActor {
  override def defaultPrefix = Full("peer")
  val max_view = 20
  var peers: List[String] = List()

  def peerview(e: String): Node = {
    <li>{e}</li>
  }

  def render =
    bind("view" -> <ul>{peers.flatMap(e => peerview(e))}</ul>)

  def ip_string(ip: Int) =
    ByteBuffer.allocate(4).putInt(ip).array.map(_.toInt).mkString(".")

  override def localSetup = {
    PeerWatcher ! Add(this)
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case NewPeer(x: String) =>
      peers = x :: peers.slice(0, max_view-1); reRender(false)
  }
}
