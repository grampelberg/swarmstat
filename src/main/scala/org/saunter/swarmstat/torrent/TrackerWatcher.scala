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

package org.saunter.swarmstat.torrent

import net.lag.logging.Logger
import scala.actors.Actor
import scala.actors.Actor._

import org.saunter.swarmstat.util._

case class InvalidateTracker(t: String)
case class CheckTracker(ret: Actor, t: String)
case class ValidTracker(t: String)
case class RemoveTracker(t: String)

object TrackerWatcher extends Actor with Listener {
  private var bad_trackers: List[String] = List()

  def act = loop {
    react(handler orElse {
      case InvalidateTracker(t: String) => {
        Logger("TrackerWatcher").info("Invalidating tracker: "+t)
        bad_trackers = t :: bad_trackers
        listeners.foreach(_ ! RemoveTracker(t))
      }
      case CheckTracker(ret: Actor, t: String) => {
        Logger("TrackerWatcher").info("Checking tracker: " + t)
        if (!bad_trackers.exists(_==t)) ret ! ValidTracker(t)
      }
    })
  }

  Logger("TrackerWatcher").info("starting")
  // Sure enough, it's important to start stuff, eh?
  this.start
}
