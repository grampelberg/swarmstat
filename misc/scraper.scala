/* Scala based tracker scraper and data fetcher
 * Copyright (C) 2009 Thomas Rampelberg <pyronicide@gmail.com>

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

import java.io._

import org.klomp.snark.bencode._

// import scalax.io._

// XXX - Need to make this polite!!!!!
// def get(uri: String): String = {
//   val client = new DefaultHttpClient()
//   val request = new HttpGet(uri)
//   println("request: " + request.getURI)
//   val response = new BasicResponseHandler[String]
//   val body = client.execute(request)
//   client.getConnectionManager.shutdown
//   response.handleResponse(body)
// }

// def scalax_get(uri: String): String = {
//   InputStreamResource.url(uri).reader.lines.foldLeft("")(_+_)
// }

// def bdecode(file_name: String) = {
//   val decoded_file = (new BDecoder(new FileInputStream(file_name))).bdecode
//   val torrent_list = decoded_file.getMap.get("files").asInstanceOf[BEValue].getMap.entrySet
//   val torrent_iterator = torrent_list.iterator
//   while (torrent_iterator.hasNext) {
//     val torrent_info = torrent_iterator.next
//     val info_hash = torrent_info.getKey
//     val status_map = torrent_info.getValue.asInstanceOf[BEValue].getMap
//     val swarm_total = status_map.get("incomplete").asInstanceOf[BEValue].getInt +
//       status_map.get("complete").asInstanceOf[BEValue].getInt
//     println(swarm_total)
//   }
// }

def bdecode(file_name: String) = {
  val list = (new BDecoder(new FileInputStream(file_name))).bdecode.getMap.get("files").asInstanceOf[BEValue].getMap.entrySet
  println(Set(list : _*))
}

//println(scalax_get("http://google.com"))

bdecode("../btstats/test.scrape")
