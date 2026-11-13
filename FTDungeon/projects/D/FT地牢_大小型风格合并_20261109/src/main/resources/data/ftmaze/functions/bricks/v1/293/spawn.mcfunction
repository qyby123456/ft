scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 189 31 203 minecraft:air if block 189 32 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 189 31 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 31 213 minecraft:air if block 182 32 213 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 182 31 213 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 31 211 minecraft:air if block 194 32 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 194 31 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 31 205 minecraft:air if block 181 32 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 181 31 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 190 31 203 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 190 31 203 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b293 ftm_brick 2
