scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 52 91 41 minecraft:air if block 52 92 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 52 91 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 91 52 minecraft:air if block 64 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 64 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 91 34 minecraft:air if block 63 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 63 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 52 91 52 minecraft:air if block 52 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 52 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 52 91 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 91 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b125 ftm_brick 2
