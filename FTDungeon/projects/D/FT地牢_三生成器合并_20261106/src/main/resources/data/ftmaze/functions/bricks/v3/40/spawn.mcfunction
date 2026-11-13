scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 151 93 minecraft:air if block 179 152 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 179 151 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 103 minecraft:air if block 190 152 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 151 89 minecraft:air if block 194 152 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 194 151 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 151 103 minecraft:air if block 177 152 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 177 151 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 151 99 minecraft:air if block 183 152 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 183 151 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 88 minecraft:air if block 184 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 151 94 minecraft:air if block 188 152 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 188 151 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 89 minecraft:air if block 174 152 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 93 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 93 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b40 ftm_brick 2
