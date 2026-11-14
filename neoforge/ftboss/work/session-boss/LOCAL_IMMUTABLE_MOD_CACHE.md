# Local archived native dependency cache

After verified Stage19/20/21 archives,324 common dependency JAR paths share27identical C-local immutable files in cache/common-mods-sha256-v1. All paths/SHA256 contents and frozen native inputs remain unchanged;world files,sources and formal JARs untouched. About1.6GiB reclaimed.
Linked cached JARs and all aliases are read-only. Never edit any linked archived JAR in place. Create a separate writable copy in a new runtime from the frozen restore archive when needed. New Boss candidates and the formal Boss JAR are not shared by this cache.
Receipt: reports/boss-common-mod-cache-dedup-stage21-20261113/dedup-receipt.json
Archive: archives/boss-common-mod-cache-dedup-stage21-20261113.zip
Interrupted V1 read-only replacement preserved in recovery-start-v2.json;V2 re-read all324 original path hashes before recovering one verified temporary hardlink. Full verification and archive readback passed.
Full Boss goal active,weapons paused,same sole C writer.


## Stage22 extension

216additional verified archived common mod paths now use the same27immutable cache files;540cumulative original paths retained.All path contents/SHA256 and8native reports reverified.About1.19GiB additional space reclaimed.Worlds,sources,formal Boss/Forge JARs and historical D unchanged.
Receipt: reports/boss-common-mod-cache-dedup-stage22-20261113/dedup-receipt.json
Archive: archives/boss-common-mod-cache-dedup-stage22-20261113.zip
All cache/aliases read-only;never edit in place.Create independent new runtime copies from frozen common-mod restore archives.Full Boss goal active;Stage22current,weapons paused.


## Stage24 extension

297additional verified archived common mod paths now use the same27immutable cache files;837cumulative original paths retained.All path contents/SHA256 and10native reports reverified.About1.61GiB additional space reclaimed.Worlds,sources,formal Boss/Forge JARs and historical D unchanged.
Receipt: reports/boss-common-mod-cache-dedup-stage24-20261113/dedup-receipt.json
Archive: archives/boss-common-mod-cache-dedup-stage24-20261113.zip
All cache/aliases read-only;never edit in place.Create independent new runtime copies from frozen common-mod restore archives.Full Boss goal active;Stage24current,weapons paused.
