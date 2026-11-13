scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 151 48 minecraft:air if block 172 152 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 172 151 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 57 minecraft:air if block 184 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 41 minecraft:air if block 182 152 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 173 151 57 minecraft:air if block 173 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 173 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 151 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 151 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b120 ftm_brick 2
