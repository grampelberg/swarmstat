- logging (log4j)
  - feeds
  - peers
  - trackers
  - webfetch
  - comet functionality
  - per page basis?

- httpclient
  - gzip compression
  - etag
  - last-modified
  - custom timeout
  - retries?
  - move to WebFetch
    - Feeds
  - use multi-thread fetching on a per-peerwatcher basis? (maintain one
    connection indefinitely)
  - disable stale connection checks
  - instrument with log4j via. commons-logging
  - disable cookie processing by default (allow enabling easily)
  - allow fetching from udp as well as tcp

- FeedWatcher
  - rename to FeedWatcher (both object and class?)
  - don't silently ignore duplicates, check the hostname. If it's different,
    log that somehow.
  - allow udp torrent sources
  - when new sources are discovered, how does the info object for the torrent
    get updated? Or, should the torrent objects periodically check the db for
    new sources?

- PeerWatcher
  - need to globally disable trackers that aren't up
  - periodically check globally disabled trackers and enable if they're up

- make sure that httpclient dependencies get downloaded/built correctly by maven
  - Is there a way to have dynamic versions for this based off a dynamic
    httpclient version? no reason to stick on a snapshot.

- models
  - PKs should be UUIDs and not something else
  - Peer needs to have no PK
  - TorrentState doesn't need a PK
  - info_hash should be actual binary data to save space
  - use info_hash as the PK instead of an actual PK
  - Peer needs a link to tracker
  - add valdiation inside the models
  - setup the indexes correctly
  - add UUIDKeyedMapper and UUIDKeyedMetaMapper (and allow UUID to be mixed in
    just like IdPK).

- tracker communication
  - for trackers that support it, add multiple info_hash/request
  - handle a dictionary model for peers
  - obey min interval if listed
  - use scrape for trackers that support it to fetch "downloaded" information

- ActorManagement
  - actively remove watchers that are dead.