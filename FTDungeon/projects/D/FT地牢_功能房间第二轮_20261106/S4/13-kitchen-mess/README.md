# S4-13 厨房食堂 Kitchen & Mess

Author self-check: PASS; independent acceptance: NOT PERFORMED.

- Candidate SHA256: b0d467372ca1b5fa9a9e508d722435611ac5963167f4d6bc45dc2532d4c36f71
- Size: 29 x 16 x 27
- Real Lodestone previews: complete.png / cutaway.png; display cut only, source has roof and walls.
- Recipe: recipe.mjs + ../common.mjs; Java DataVersion 3465.
- Inventory, native static registry/support, collision routes and hash-bound render evidence: qa/.
- Ports and absolute local center/feet coordinates: qa/inventory.json metadata.

## Differences
- Hearth glow is enclosed froglight, not damaging live fire; ovens unlit and cauldron empty.
- Terracotta flower pots approximate jars and plates; no item-frame entities.
- Gabled full timber ceiling and chimney added; fluid kept in lined pools away from dining routes.
- Timber benches are slabs; dining is decorative, furnaces and barrels remain empty.
- Hanging cauldron sits above nonflammable froglight; no actual ignition source in furnace area.

## Pending
- No placed-world neighbor/random/fluid tick or fire-spread simulation.
- No independent acceptance or survival/combat playtest.
- Offline native collision samples, no ladder gameplay certification.
