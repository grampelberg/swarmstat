# Scrape a bittorrent tracker and fetch its tracking data for each info_hash.

# Copyright (C) 2009 Thomas Rampelberg <pyronicide@gmail.com>

# This program is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software
# Foundation; either version 2, or (at your option) any later version.

# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.

# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 59 Temple
# Place - Suite 330, Boston, MA 02111-1307, USA.

import bencode
import os
import socket
import traceback
import urllib
import urllib2

class scrape:

    def __init__(self, tracker):
        self.tracker = tracker

    def fetch_scrape(self):
        url = os.path.join(self.tracker, 'scrape')
        return bencode.bdecode(urllib2.urlopen(url).read())

    def fetch_peers(self, info_hash, total_peers):
        url = os.path.join(self.tracker,
                           'announce?info_hash=%s&numwant=%s' % (
            urllib.quote(info_hash), total_peers))
        return bencode.bdecode(urllib2.urlopen(url).read())

    def scrape_info(self, scrape):
        scrape = scrape['files']
        for i, j in scrape.iteritems():
            print i
            try:
                dbconn = MySQLdb.connect(host='localhost', user='stats',
                                         passwd='statsuser', db='trackerdata')
                cursor = dbconn.cursor()
                cursor.execute('select uuid()')
                uuid = cursor.fetchone()
                cursor.execute("insert into torrents (name, uuid) " \
                               "values(%s, unhex(replace(%s, '-', ''))" % (
                    MySQLdb.escape_string(i), uuid))
                tracker_resp = self.fetch_peers(
                    i, j['downloaded'] + j['incomplete'])
                peers = [
                    ''.join(x) for x in
                    zip(*[list(tracker_resp['peer6'][z::6]) for z in xrange(6)])]
                for k in peers:
                    hostorder = struct.pack(
                        'I', socket.ntohl(struct.unpack('I', k[:4])[0]))
                    cursor.execute("insert into peers (torrent, ip, time) " \
                                   "values (unhex(replace(%s, '-', '')), " \
                                   "%s, now())" % (
                        uuid, hostorder))
                    print '\t%s' % (socket.inet_ntoa(hostorder))
            except:
                traceback.print_exc()
            finally:
                cursor.close()
                dbconn.close()

if __name__ == '__main__':
    raw_scrape = open('data/test.scrape').read()
    scrape_obj = bencode.bdecode(raw_scrape)
    for i in scrape_obj['files'].keys():
        print i
    obt = scrape('http://tracker.openbittorrent.com/')
    obt.scrape_info(scrape_obj)
