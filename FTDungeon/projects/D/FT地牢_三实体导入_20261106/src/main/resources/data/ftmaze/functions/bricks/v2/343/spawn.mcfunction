scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 559 91 198 minecraft:air if block 559 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 559 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 549 91 202 minecraft:air if block 549 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 549 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 564 91 202 minecraft:air if block 564 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 564 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 552 91 198 minecraft:air if block 552 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 552 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 556 91 202 minecraft:air if block 556 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 556 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 553 91 201 minecraft:air if block 553 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 553 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 555 91 199 minecraft:air if block 555 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 555 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 560 91 201 minecraft:air if block 560 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 560 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 560 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 560 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b343 ftm_brick 2
