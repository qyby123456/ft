# S4-15 训练场 Training Yard

Author self-check: PASS; independent acceptance: NOT PERFORMED.

- Candidate SHA256: 2c01111ba3528f31893eabf115b507f37f4232d74bd2a591d3df4f5045553173
- Size: 33 x 13 x 27
- Real Lodestone previews: complete.png / cutaway.png; display cut only, source has roof and walls.
- Recipe: recipe.mjs + ../common.mjs; Java DataVersion 3465.
- Inventory, native static registry/support, collision routes and hash-bound render evidence: qa/.
- Ports and absolute local center/feet coordinates: qa/inventory.json metadata.

## Differences
- Three hay-and-pumpkin dummies and native targets; no entity combat logic.
- Obstacle rails are optional side course; all functions reachable on dry bypass without jumping.
- Full timber canopy added because concept cutaway does not authorize missing roof.
- Training floor kept dry; wetland identity in moss, mud and vines rather than flood over gravel.

## Pending
- No placed-world neighbor/random/fluid tick or fire-spread simulation.
- No independent acceptance or survival/combat playtest.
- Offline native collision samples, no ladder gameplay certification.
