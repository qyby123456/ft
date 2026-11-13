scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1187 211 198 minecraft:air if block 1187 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1187 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1179 211 201 minecraft:air if block 1179 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1179 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1191 211 203 minecraft:air if block 1191 212 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1191 211 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 211 197 minecraft:air if block 1181 212 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1181 211 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1187 211 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1187 211 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b358 ftm_brick 2
