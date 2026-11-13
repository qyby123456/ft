# S4-12 牢房区 Prison Cellblock

Author self-check: PASS; independent acceptance: NOT PERFORMED.

- Candidate SHA256: 7dca43974592c934678c52cb5e2c5e32e68f63bdd37725bdd58d7ee0bb6cf334
- Size: 31 x 13 x 25
- Real Lodestone previews: complete.png / cutaway.png; display cut only, source has roof and walls.
- Recipe: recipe.mjs + ../common.mjs; Java DataVersion 3465.
- Inventory, native static registry/support, collision routes and hash-bound render evidence: qa/.
- Ports and absolute local center/feet coordinates: qa/inventory.json metadata.

## Differences
- Six cells on both sides rather than perspective-compressed L-shaped illustration.
- Cell doorways have 3-wide open clearance; powered-open iron doors remain at their edge, no keyed lock behavior.
- Dry central boardwalk and bridges across lined water gutters.
- Guard keys approximated by supported chains; cell bunks are timber with carpet, no functional beds.
- Door leaves are decorative beside permanent clear 3-wide entrances; levers are operable but no cell-lock or keyed mechanism is claimed.

## Pending
- No placed-world neighbor/random/fluid tick or fire-spread simulation.
- No independent acceptance or survival/combat playtest.
- Offline native collision samples, no ladder gameplay certification.
