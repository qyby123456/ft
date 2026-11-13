scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 204 31 214 minecraft:air if block 204 32 214 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 204 31 214 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 213 31 199 minecraft:air if block 213 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 213 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 31 211 minecraft:air if block 216 32 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 216 31 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 204 31 202 minecraft:air if block 204 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 204 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 205 31 214 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 205 31 214 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b251 ftm_brick 2
