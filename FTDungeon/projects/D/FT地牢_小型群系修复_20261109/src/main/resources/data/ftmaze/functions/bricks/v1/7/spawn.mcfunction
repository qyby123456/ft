scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 178 minecraft:air if block 94 212 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 211 188 minecraft:air if block 105 212 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 105 211 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 211 174 minecraft:air if block 109 212 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 109 211 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 211 188 minecraft:air if block 90 212 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 90 211 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 211 172 minecraft:air if block 99 212 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 99 211 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 211 185 minecraft:air if block 97 212 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 97 211 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 211 180 minecraft:air if block 102 212 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 102 211 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 211 182 minecraft:air if block 108 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 108 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 211 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 211 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b7 ftm_brick 2
