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

import java.io.InputStreamReader
import java.lang.Integer
import java.net.URLEncoder
import org.apache.commons.codec.net.URLCodec
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._
import org.apache.http.params._

import scalax.io.ReaderResource

object WebFetch {
  val socket_timeout = 3 * 1000
  val connect_timeout = 10 * 1000

  def get_params =
    (new BasicHttpParams).setParameter(
      "http.socket.timeout", socket_timeout) setParameter(
      "http.connection.timeout", connect_timeout)

  def paramsToUrlParams(params: List[(String, String)]): String =
    params.map {
      case (n, v) => escape(n) + "=" + escape(v)
    }.mkString("&")

  def appendParams(url: String, params: Seq[(String, String)]): String =
    params.toList match {
      case Nil => url
      case xs if !url.contains("?") => url + "?" + paramsToUrlParams(xs)
      case xs => url + "&" + paramsToUrlParams(xs)
    }

  def escape(uri: String) =
    (new URLCodec).encode(uri.toArray.map(_.toByte)).map(
      _.toChar).mkString.replaceAll("\\+", "%20")

  def url_stream(uri: String) = {
    val client = new DefaultHttpClient(get_params)
    val get = new HttpGet(uri)
    try {
      new InputStreamReader(client.execute(get).getEntity.getContent)
    } finally {
      // client.getConnectionManager.shutdown
    }
  }

  def url(uri: String) = {
    val instream = url_stream(uri)
    try {
      ReaderResource(instream).slurp
    } catch {
      case _ => ""
    } finally {
      // instream.close
    }
  }
}
