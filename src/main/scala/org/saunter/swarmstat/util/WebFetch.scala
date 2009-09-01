/*
 * Copyright (C) 2009 Thomas Rampelberg <pyronicide@saunter.com>

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

/*
 * Get a webpage in a friendly, timeout safe manner.
 */

package org.saunter.swarmstat.util

import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._
import org.apache.http.params._

import scalax.io.InputStreamResource

object WebFetch {
  val socket_timeout = 1 * 1000 // 1 second

  def get_params =
    (new BasicHttpParams) setParameter("http.socket.timeout", socket_timeout)

  def url(uri: String) = {
    val client = new DefaultHttpClient(get_params)
    val get = new HttpGet(uri)
    val instream = client.execute(get).getEntity.getContent
    try {
      println("Fetching: " + uri)
      InputStreamResource(instream).reader.slurp
    } catch {
      case _ => println("Failed to fetch: " + uri); ""
    } finally {
      instream.close()
      client.getConnectionManager.shutdown
    }
  }
}
