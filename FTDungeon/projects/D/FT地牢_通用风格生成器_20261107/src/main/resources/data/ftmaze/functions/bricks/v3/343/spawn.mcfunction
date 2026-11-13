scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 559 211 198 minecraft:air if block 559 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 559 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 549 211 202 minecraft:air if block 549 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 549 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 564 211 202 minecraft:air if block 564 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 564 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 552 211 198 minecraft:air if block 552 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 552 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 556 211 202 minecraft:air if block 556 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 556 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 553 211 201 minecraft:air if block 553 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 553 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 555 211 199 minecraft:air if block 555 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 555 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 560 211 201 minecraft:air if block 560 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 560 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 560 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 560 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b343 ftm_brick 2
