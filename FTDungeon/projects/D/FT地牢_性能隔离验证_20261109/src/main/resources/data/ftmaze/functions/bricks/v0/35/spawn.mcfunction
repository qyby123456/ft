scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 178 minecraft:air if block 122 152 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 151 189 minecraft:air if block 134 152 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 134 151 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 151 176 minecraft:air if block 135 152 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 135 151 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 151 189 minecraft:air if block 120 152 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 120 151 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 151 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 151 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b35 ftm_brick 2
