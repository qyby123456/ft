scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 31 178 minecraft:air if block 122 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 122 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 31 188 minecraft:air if block 136 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 136 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 31 176 minecraft:air if block 134 32 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 134 31 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 31 188 minecraft:air if block 120 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 120 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 31 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 31 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b83 ftm_brick 2
