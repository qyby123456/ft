scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 211 93 minecraft:air if block 179 212 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 179 211 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 103 minecraft:air if block 190 212 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 89 minecraft:air if block 194 212 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 211 103 minecraft:air if block 177 212 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 177 211 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 211 99 minecraft:air if block 183 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 183 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 211 88 minecraft:air if block 184 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 184 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 211 94 minecraft:air if block 188 212 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 188 211 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 89 minecraft:air if block 174 212 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 93 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 93 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b16 ftm_brick 2
