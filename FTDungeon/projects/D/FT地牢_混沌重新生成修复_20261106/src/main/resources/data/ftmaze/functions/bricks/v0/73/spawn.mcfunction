scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 31 125 minecraft:air if block 66 32 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 66 31 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 31 134 minecraft:air if block 78 32 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 78 31 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 76 31 121 minecraft:air if block 76 32 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 76 31 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 67 31 134 minecraft:air if block 67 32 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 67 31 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 31 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 31 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b73 ftm_brick 2
