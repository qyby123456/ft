scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1166 31 24 minecraft:air if block 1166 32 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1166 31 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 31 38 minecraft:air if block 1183 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1183 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 31 18 minecraft:air if block 1181 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1181 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 31 38 minecraft:air if block 1163 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1163 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 31 32 minecraft:air if block 1173 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1173 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1177 31 25 minecraft:air if block 1177 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1177 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1170 31 18 minecraft:air if block 1170 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1170 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 31 30 minecraft:air if block 1181 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1181 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1167 31 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1167 31 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b214 ftm_brick 2
