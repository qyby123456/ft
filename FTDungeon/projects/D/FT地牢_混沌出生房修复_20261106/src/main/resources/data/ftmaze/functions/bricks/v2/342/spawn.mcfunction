scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 517 91 198 minecraft:air if block 517 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 517 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 507 91 202 minecraft:air if block 507 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 507 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 522 91 202 minecraft:air if block 522 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 522 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 510 91 198 minecraft:air if block 510 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 510 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 514 91 202 minecraft:air if block 514 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 514 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 511 91 201 minecraft:air if block 511 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 511 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 513 91 199 minecraft:air if block 513 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 513 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 518 91 201 minecraft:air if block 518 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 518 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 518 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 518 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b342 ftm_brick 2
