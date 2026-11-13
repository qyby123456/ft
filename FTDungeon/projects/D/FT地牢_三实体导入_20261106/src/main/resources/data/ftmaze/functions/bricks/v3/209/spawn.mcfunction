scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1168 211 119 minecraft:air if block 1168 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1168 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 211 109 minecraft:air if block 1184 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1184 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 211 129 minecraft:air if block 1181 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1181 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1171 211 109 minecraft:air if block 1171 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1171 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1166 211 129 minecraft:air if block 1166 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1166 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1179 211 118 minecraft:air if block 1179 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1179 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 211 125 minecraft:air if block 1173 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1173 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 211 122 minecraft:air if block 1184 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1184 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1169 211 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1169 211 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b209 ftm_brick 2
