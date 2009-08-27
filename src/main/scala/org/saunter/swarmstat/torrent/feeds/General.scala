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

/* This is going to be general feeds v 0.0001. For now, each feed will
 * implement the MasterFeed.Feed trait and have its own fetch() method. This
 * method will be in charge of returning a list of urls to torrents that get
 * stored in the db.
 *
 * Eventually, I think I'd like this to live entirely in a database. You could
 * add new feeds (html, xml, who cares?) via the gui. While I'm not sure a
 * DSL is perfect for this, it sure does make sense. Either way, this is a
 * feature I'd "like" but since it isn't critical and my sensibilities are only
 * partially offended by the giant list of objects for each feed I'll ignore
 * it for now.
 */

package org.saunter.swarmstat.torrent.feeds

import org.saunter.swarmstat.torrent._

object EZTV extends Feed {
  val feed = "http://www.ezrss.it/feed/"

  def fetch: Seq[String] =
    get_data(feed) match {
      case Some(x) => (x \\ "item").map(_ \ "link").map(_.text)
      case _ => Seq()
    }
}

class BasicFeed extends Feed {
  val feed = ""

  def fetch: Seq[String] =
    get_data(feed) match {
      case Some(x) => (x \\ "item").map(_ \ "enclosure").map(
        x => (x \ "@url").toString)
      case _ => Seq()
    }
}

object Mininova extends BasicFeed {
  override val feed = "http://www.mininova.org/rss.xml"
}

object Isohunt extends BasicFeed {
  override val feed = "http://isohunt.com/js/rss/"
}

