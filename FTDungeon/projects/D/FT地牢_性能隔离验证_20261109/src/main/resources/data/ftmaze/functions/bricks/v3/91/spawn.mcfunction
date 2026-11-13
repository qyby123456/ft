scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 31 180 minecraft:air if block 179 32 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 179 31 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 31 190 minecraft:air if block 189 32 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 189 31 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 31 180 minecraft:air if block 190 32 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 190 31 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 31 190 minecraft:air if block 179 32 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 179 31 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 180 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 180 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b91 ftm_brick 2
