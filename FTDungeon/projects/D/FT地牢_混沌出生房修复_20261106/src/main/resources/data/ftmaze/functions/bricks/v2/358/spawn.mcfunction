scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1187 91 198 minecraft:air if block 1187 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1187 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1179 91 201 minecraft:air if block 1179 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1179 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1191 91 203 minecraft:air if block 1191 92 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1191 91 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 91 197 minecraft:air if block 1181 92 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1181 91 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1187 91 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1187 91 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b358 ftm_brick 2
