SwarmStat
==========

SwarmStat is a system for tracking what is happening with swarms associated
with specific torrents over time and displaying this information. The concept
behind this project is to understand the dynamics of BitTorrent
swarms. Ideally, the trends that occur from torrents in both gaining
popularity, staying power and loosing popularity will come to the forefront.

Running
=======

For development, I would suggest something along these lines (Note that
run.mode=dev disables user authentication):

    MAVEN_OPTS=-Xmx512m mvn -o -D run.mode=dev jetty:run

Notes
=====

Since I've not noted this anywhere else, to run this you need a patched version
of lift-mapper to get it to work. I'm in the process of getting this pushed to
my own liftweb clone but for now you'll need to ask me if you'd like the
patches.