# S4-14 军械库 Armory

Author self-check: PASS; independent acceptance: NOT PERFORMED.

- Candidate SHA256: 029ceeb627a645ee6b3dbcbadffab0e1101cbf7502788fdc0afd6853f6bc9ec8
- Size: 27 x 13 x 25
- Real Lodestone previews: complete.png / cutaway.png; display cut only, source has roof and walls.
- Recipe: recipe.mjs + ../common.mjs; Java DataVersion 3465.
- Inventory, native static registry/support, collision routes and hash-bound render evidence: qa/.
- Ports and absolute local center/feet coordinates: qa/inventory.json metadata.

## Differences
- Empty cabinet is decorative, no locks, keys or weapon rewards.
- Dry three-wide boardwalk crosses the illustrated surrounding water to preserve accessibility.
- Full roof restored, missing wall only in display copy; no enemy or combat configuration.
- Two real vanilla equipped armor stands on original display bases; no weapons or rewards in containers. Armor/entity payload inserted by qa/postprocess.py.

## Pending
- No placed-world neighbor/random/fluid tick or fire-spread simulation.
- No independent acceptance or survival/combat playtest.
- Offline native collision samples, no ladder gameplay certification.
