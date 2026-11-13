scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 177 151 124 minecraft:air if block 177 152 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 177 151 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 151 133 minecraft:air if block 191 152 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 191 151 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 151 121 minecraft:air if block 188 152 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 188 151 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 151 131 minecraft:air if block 181 152 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 181 151 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b41 ftm_brick 2
