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

package org.saunter.swarmstat.util

import java.net.InetAddress
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date

object Conversion {

  def ip(i: String): Int =
    ip(InetAddress.getByName(i).getAddress)

  def ip(i: Array[Byte]): Int =
    (i(0) << 24) + (i(1) << 16) + (i(2) << 8) + i(3)

  def ip(i: Int): Array[Byte] = Array(
    ((i & 0xFF000000) >> 24).toByte,
    ((i & 0x00FF0000) >> 16).toByte,
    ((i & 0x0000FF00) >> 8).toByte,
    ((i & 0x000000FF)).toByte)

  def date_format(x: Date): String =
    (new SimpleDateFormat("H:mm MM/dd")).format(x)

  def hostname(x: String): String =
    (new URI(x)).getHost
}
