scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 559 31 198 minecraft:air if block 559 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 559 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 549 31 202 minecraft:air if block 549 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 549 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 564 31 202 minecraft:air if block 564 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 564 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 552 31 198 minecraft:air if block 552 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 552 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 556 31 202 minecraft:air if block 556 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 556 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 553 31 201 minecraft:air if block 553 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 553 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 555 31 199 minecraft:air if block 555 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 555 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 560 31 201 minecraft:air if block 560 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 560 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 560 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 560 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b346 ftm_brick 2
