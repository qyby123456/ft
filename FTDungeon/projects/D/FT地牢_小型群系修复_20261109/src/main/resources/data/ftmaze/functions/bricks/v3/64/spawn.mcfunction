scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 91 93 minecraft:air if block 179 92 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 179 91 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 103 minecraft:air if block 190 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 89 minecraft:air if block 194 92 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 91 103 minecraft:air if block 177 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 177 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 99 minecraft:air if block 183 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 91 88 minecraft:air if block 184 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 184 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 91 94 minecraft:air if block 188 92 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 188 91 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 89 minecraft:air if block 174 92 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 93 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 93 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b64 ftm_brick 2
