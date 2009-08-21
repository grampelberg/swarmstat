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

/* Add new RSS feeds to monitor
 */

package org.saunter.swarmstat.snippet

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

class RSSSnippet {
  def add(form: NodeSeq): NodeSeq = {
    val feed = RSSFeed.create

    def checkAndSave(): Unit = {
      FeedFetcher ! AddFeed(feed.url)
      feed.validate match {
        case Nil => feed.save; S.notice("Now watching: "+feed.url)
        case xs => S.error(xs); S.mapSnippet("RSSSnippet.add", doBind)
      }
    }

    def doBind(form: NodeSeq) =
      bind("feed", form,
           "url" -> feed.url.toForm,
           "submit" -> submit("Add", checkAndSave))

    doBind(form)
  }

  def purge(form: NodeSeq) =
    bind("feed", form,
         "purge" -> submit("Purge",
                           () => RSSFeed.findAll.map( x => x.delete_!)))

  private def drawList(reDraw: () => JsCmd)(html: NodeSeq): NodeSeq =
    RSSFeed.findAll(OrderBy(RSSFeed.url, Ascending)).flatMap(feed => {
      bind("feed", html,
           "url" -> feed.url)
    })

  def list(html: NodeSeq) = {
    val id = S.attr("all_id").open_!

    def inner(): NodeSeq = {
      def reDraw() = SetHtml(id, inner())

      bind("feed", html,
           "list" -> drawList(reDraw) _)
    }
    inner()
  }
}
