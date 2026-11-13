scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 51 91 82 minecraft:air if block 51 92 82 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 51 91 82 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 43 91 85 minecraft:air if block 43 92 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 43 91 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 55 91 87 minecraft:air if block 55 92 87 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 55 91 87 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 45 91 81 minecraft:air if block 45 92 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 45 91 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 51 91 81 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 51 91 81 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b275 ftm_brick 2
