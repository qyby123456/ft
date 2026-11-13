scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 31 151 minecraft:air if block 93 32 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 93 31 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 31 162 minecraft:air if block 107 32 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 107 31 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 31 149 minecraft:air if block 105 32 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 105 31 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 162 minecraft:air if block 94 32 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 31 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 31 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b78 ftm_brick 2
