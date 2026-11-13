scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 151 68 minecraft:air if block 172 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 172 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 76 minecraft:air if block 184 152 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 62 minecraft:air if block 182 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 76 minecraft:air if block 174 152 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 151 69 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 151 69 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b122 ftm_brick 2
