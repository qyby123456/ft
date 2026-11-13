scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1166 151 112 minecraft:air if block 1166 152 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1166 151 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 151 126 minecraft:air if block 1183 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1183 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 151 107 minecraft:air if block 1181 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1181 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 151 126 minecraft:air if block 1163 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1163 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 151 120 minecraft:air if block 1173 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1173 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1171 151 107 minecraft:air if block 1171 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1171 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 151 112 minecraft:air if block 1176 152 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1176 151 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 151 117 minecraft:air if block 1181 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1181 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1167 151 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1167 151 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b209 ftm_brick 2
