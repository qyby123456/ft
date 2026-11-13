scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1166 91 24 minecraft:air if block 1166 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1166 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 91 38 minecraft:air if block 1183 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1183 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 91 18 minecraft:air if block 1181 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1181 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 91 38 minecraft:air if block 1163 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1163 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 91 32 minecraft:air if block 1173 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1173 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1177 91 25 minecraft:air if block 1177 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1177 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1170 91 18 minecraft:air if block 1170 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1170 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 91 30 minecraft:air if block 1181 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1181 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1167 91 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1167 91 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b214 ftm_brick 2
