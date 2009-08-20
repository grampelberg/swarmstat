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

class TorrentSnippet {
  // Add a torrent!
  def add(form: NodeSeq): NodeSeq = {
    object url extends RequestVar(Full(""))

    // XXX - Need more validation ... should check and see if it's a valid
    // torrent.
    def checkAndSave(): Unit =
      url.get match {
        case Full(x) if x.startsWith("http") => populate_torrent(x)
        case Full(x) => S.notice("Invalid url: " + x)
      }


    def populate_torrent(url: String) = {
      val torrent_model = Torrent.create
      val torrent = Info.from_url(url)
      torrent_model.info_hash.apply(torrent.info_hash)
      torrent_model.creation.apply(torrent.creation)
      torrent_model.name.apply(torrent.name)
      torrent_model.save
      S.notice("Added: " + torrent.name)
    }

    def doBind(form: NodeSeq) =
      bind("torrent", form,
           "url" -> text(url.openOr(""),
                         v => url(Full(v))
                       ) % ("size" -> "50") % ("id" -> "urlField"),
           "submit" -> submit("New", checkAndSave))

    doBind(form)
  }

  def purge(form: NodeSeq) = {
    def all() =
      Torrent.findAll.map( x => x.delete_!)

    def doBind(form: NodeSeq) =
      bind("torrent", form,
           "purge" -> submit("Purge", all))

    doBind(form)
  }

  def viewlist(html: NodeSeq) = {
    <lift:comet type="DynamicTorrentView" name={toLong(S.param("id")).toString}>
      <torrent:view>Loading...</torrent:view>
    </lift:comet>
  }

  // Let's get a list of all the torrents in the database

//   private def doList(reDraw: () => JsCmd)(html: NodeSeq): NodeSeq =
//     Torrent.findAll.flatMap(torrent => {
//       bind("torrent", html,
//            "name" -> torrent.name,
//            "creation" -> torrent.creation
//            // "creation" -> (new SimpleDateFormat("hh:mma MM/dd/yyyy")).format(
//            //   torrent.creation)
//          )
//     })

//   def list(html: NodeSeq) = {
//     val id = S.attr("all_id").open_!

//     def inner(): NodeSeq = {
//       def reDraw() = SetHtml(id, inner())

//       bind("torrent", html,
//            "list" -> doList(reDraw) _)
//     }
//     inner()
//   }
}
