scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 91 48 minecraft:air if block 172 92 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 172 91 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 91 57 minecraft:air if block 184 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 184 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 91 41 minecraft:air if block 182 92 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 182 91 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 173 91 57 minecraft:air if block 173 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 173 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 91 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 91 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b134 ftm_brick 2
