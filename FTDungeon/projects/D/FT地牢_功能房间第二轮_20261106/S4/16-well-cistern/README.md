# S4-16 水井蓄水池 Well / Cistern

Author self-check: PASS; independent acceptance: NOT PERFORMED.

- Candidate SHA256: 6281c2b5dbead389206b09f44015884936d5ed04f14f21422c975577ff0a7efc
- Size: 25 x 15 x 25
- Real Lodestone previews: complete.png / cutaway.png; display cut only, source has roof and walls.
- Recipe: recipe.mjs + ../common.mjs; Java DataVersion 3465.
- Inventory, native static registry/support, collision routes and hash-bound render evidence: qa/.
- Ports and absolute local center/feet coordinates: qa/inventory.json metadata.

## Differences
- Central cistern is lined and raised, water fully contained; illustrated spillway omitted to avoid uncontrolled spreading.
- Pulley bucket approximated by suspended empty barrel; static construction, no mechanism.
- Dry maintenance loop skirts north and both sides of cistern; south lily pool is optional scenery.
- Complete enclosing walls and roof retained in source.
- Suspended bucket replaced by recognizable native cauldron under chain, shown in dedicated closeup.

## Pending
- No placed-world neighbor/random/fluid tick or fire-spread simulation.
- No independent acceptance or survival/combat playtest.
- Offline native collision samples, no ladder gameplay certification.
