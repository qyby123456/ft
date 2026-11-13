# S8-07 derived doorway review

The original and derived NBT were independently decoded and compared by
`review_doorway.py`. Exactly 20 cells differ, matching the explicit ledger. All
12 original stair cells, six original block entities and every cell at Y0..3
are preserved. The four double-door halves retain their original complete
states and move together by -3 on X. Original and derived hashes are recorded
in `doorway-delta-review.json`.

Lodestone previews were generated through the installed minecraft-builder skill
and captured with the same camera and daylight settings. Review copies cut away
Y>8 and Z>8 to expose the entrance facade; these copies are visual aids only.
The images are `original-doorway-cutaway.png` and
`derived-doorway-cutaway.png`. Neither image certifies Minecraft collision.

Visual finding: the pale mineral entrance surround, dark brick facade, purple
inlays, paired lighting and two-step central stair remain recognizable. The
main arch is visibly wider. A new adjacent side opening contains the relocated
paired gate. The changed facade is suitable for continued isolated architecture
and native movement testing. It is not a pixel-identical original-room delivery.

Design cost: the side pair can close its own passage; it cannot close the widened
main arch. This behavior is an explicit part of the derived candidate and must
remain visible in its final usage and limitations. No approval for production
installation or original-catalog promotion is implied.

Native acceptance still requires the original full-width and three-high sweep,
all retained original targets, additional side-gate use/return paths, and a fresh
installed-JAR/save/restart test. The frozen original V4 retains its failure.
