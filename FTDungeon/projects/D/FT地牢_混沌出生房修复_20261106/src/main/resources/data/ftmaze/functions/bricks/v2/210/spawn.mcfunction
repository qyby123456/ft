scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1270 91 98 minecraft:air if block 1270 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1270 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1282 91 115 minecraft:air if block 1282 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1282 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 91 112 minecraft:air if block 1263 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1263 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1282 91 95 minecraft:air if block 1282 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1282 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 91 108 minecraft:air if block 1274 92 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1274 91 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 91 105 minecraft:air if block 1283 92 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1283 91 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 91 101 minecraft:air if block 1263 92 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1263 91 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1270 91 115 minecraft:air if block 1270 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1270 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1270 91 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1270 91 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b210 ftm_brick 2
