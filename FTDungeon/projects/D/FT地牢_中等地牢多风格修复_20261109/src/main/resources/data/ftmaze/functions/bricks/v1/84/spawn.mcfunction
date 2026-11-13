scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 31 94 minecraft:air if block 150 32 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 150 31 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 164 31 105 minecraft:air if block 164 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 164 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 31 92 minecraft:air if block 162 32 92 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 162 31 92 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 148 31 105 minecraft:air if block 148 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 148 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 95 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 95 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b84 ftm_brick 2
