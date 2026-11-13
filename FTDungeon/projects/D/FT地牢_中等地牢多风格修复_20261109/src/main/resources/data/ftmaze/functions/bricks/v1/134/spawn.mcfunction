scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 91 41 minecraft:air if block 172 92 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 172 91 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 91 52 minecraft:air if block 184 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 184 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 91 34 minecraft:air if block 183 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 183 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 172 91 52 minecraft:air if block 172 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 172 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 91 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 91 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b134 ftm_brick 2
