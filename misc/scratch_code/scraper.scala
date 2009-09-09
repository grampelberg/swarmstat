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

/* Simple script to fetch a scrape.
 * Usage:
 * scraper.scala remote_source local_file
 */

// Scala std lib
import scala.io._

// Other libs
import scalax.io._
import scalax.io.Implicits._
import scalax.data.Implicits._

def scrape_source(remote: String, local: String) = {
  val remote_reader = InputStreamResource.url(remote).reader
  for (w <- local.toFile.writer; line <- remote_reader.lines) {
    w.write(line + "\n")
  }
}

println("Remote Source=" + args(0))
println("Local Path=" + args(1))
println("Fetching ......")
scrape_source(args(0), args(1))
println("Completed Fetch!")
