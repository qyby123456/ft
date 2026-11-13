scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 207 minecraft:air if block 70 32 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 197 minecraft:air if block 74 32 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 212 minecraft:air if block 74 32 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 200 minecraft:air if block 70 32 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 204 minecraft:air if block 74 32 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 211 minecraft:air if block 70 32 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 203 minecraft:air if block 71 32 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 31 209 minecraft:air if block 72 32 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 31 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 31 208 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 31 208 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b274 ftm_brick 2
