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

// XXX - Testing
import org.saunter.bencode._
import scala.io._
import scalax.io._
import scalax.io.Implicits._
import scalax.data.Implicits._
import java.util.Date

class TorrentSnippet {
  // Add a torrent!
  def add(form: NodeSeq) = {
    val torrent = Torrent.create

    def checkAndSave(): Unit =
      torrent.validate match {
        case Nil => torrent.save; S.notice("Added " + torrent.url)
        case xs => S.error(xs); S.mapSnippet("Torrent.add", doBind)
      }

    def doBind(form: NodeSeq) =
      bind("torrent", form,
           "url" -> torrent.url.toForm,
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

  // Let's get a list of all the torrents in the database

  private def doList(reDraw: () => JsCmd)(html: NodeSeq): NodeSeq =
    Torrent.findAll.flatMap(torrent_model => {
      val torrent = new Info(Source.fromURL(torrent_model.url.toString).getLines.mkString)
      bind("torrent", html,
           "name" -> torrent.name,
           "creation" -> (new SimpleDateFormat("mm:hha MM/dd/yyyy")).format(
             new Date())
         )
    })

  def list(html: NodeSeq) = {
    val id = S.attr("all_id").open_!

    def inner(): NodeSeq = {
      def reDraw() = SetHtml(id, inner())

      bind("torrent", html,
           "list" -> doList(reDraw) _)
    }
    inner()
  }
}
