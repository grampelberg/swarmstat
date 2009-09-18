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
import sha
import urllib
import urllib2

def foo():
    tor = bencode.bdecode(
        urllib2.urlopen("http://www.mininova.org/get/2959179").read())
    info = bencode.bencode(tor['info'])
    info_hash = sha.new(info).digest()
    print urllib.quote(info_hash)

if __name__ == '__main__':
    foo()
