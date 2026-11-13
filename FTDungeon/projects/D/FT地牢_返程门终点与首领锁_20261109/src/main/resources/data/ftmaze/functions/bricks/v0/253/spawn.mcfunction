scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 232 31 297 minecraft:air if block 232 32 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 232 31 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 242 31 290 minecraft:air if block 242 32 290 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 242 31 290 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 31 299 minecraft:air if block 240 32 299 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 240 31 299 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 234 31 290 minecraft:air if block 234 32 290 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 234 31 290 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 31 297 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 31 297 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b253 ftm_brick 2
