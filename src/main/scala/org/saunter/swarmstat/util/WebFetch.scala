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
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._
import org.apache.http.params._

import scalax.io.ReaderResource

object WebFetch {
  val socket_timeout = 1 * 1000 // 1 second
  val connect_timeout = 5 * 1000 // 1 second

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
    URLEncoder.encode(uri).replaceAll("\\+", "%20")

  // I'm really cranky about this but apparently URLEncoder.encode is a pile of
  // crap and does " " -> "+" instead of " " -> "%20" like it should.
  def hex_encoder(input: Array[Byte]): String =
    input.map( x => (0xFF & x) match {
      case x if x < 16 => "0" + Integer.toHexString(x)
      case x => Integer.toHexString(x)
    } ).foldLeft("")( (x, y) => x + y )

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
