# S4-19 瞭望台 Watch Balcony

Author self-check: PASS; independent acceptance: NOT PERFORMED.

- Candidate SHA256: 6218b2ef856da3b40918fd6d7e4b8721148ee6211c7558893427912a85a19211
- Size: 29 x 19 x 25
- Real Lodestone previews: complete.png / cutaway.png; display cut only, source has roof and walls.
- Recipe: recipe.mjs + ../common.mjs; Java DataVersion 3465.
- Inventory, native static registry/support, collision routes and hash-bound render evidence: qa/.
- Ports and absolute local center/feet coordinates: qa/inventory.json metadata.

## Differences
- Raised deck is four blocks above lower approach; eight half-block ramp steps are collision-tested without jumping.
- Enclosing gray glass behind parapet provides a real complete outer boundary; transparent outlook replaces open concept cutaway.
- Spyglass approximated by horizontal lightning rod on desk; no optical item function claimed.
- Upper east interface has feet Y7, lower west Y3; combined building must explicitly ramp adjacent connector.

## Pending
- No placed-world neighbor/random/fluid tick or fire-spread simulation.
- No independent acceptance or survival/combat playtest.
- Offline native collision samples, no ladder gameplay certification.
