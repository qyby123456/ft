scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1166 151 31 minecraft:air if block 1166 152 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1166 151 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 151 44 minecraft:air if block 1183 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1183 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 151 25 minecraft:air if block 1181 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1181 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 151 44 minecraft:air if block 1163 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1163 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 151 39 minecraft:air if block 1173 152 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1173 151 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1177 151 32 minecraft:air if block 1177 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1177 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1170 151 25 minecraft:air if block 1170 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1170 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 151 37 minecraft:air if block 1181 152 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1181 151 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1167 151 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1167 151 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b214 ftm_brick 2
