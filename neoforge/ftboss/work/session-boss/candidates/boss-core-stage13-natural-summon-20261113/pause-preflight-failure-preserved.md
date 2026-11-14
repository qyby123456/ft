# Earlier archive preparation failure

The first pause packaging attempt stopped before metadata updates or archive creation with KeyError archiveSha256 because older receipts use sha256. Its already written process audit, pause request and recovery JAR remain in archives/weapons-paused-cf217105-boss-priority-20261113. The v2 attempt accepts the explicitly observed receipt schema, verifies each archive hash, and completed 5807 member checks. This was a packaging preflight failure, not a native acceptance pass. No weapon JVM restarted.
