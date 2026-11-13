scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 16 minecraft:air if block 193 92 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 91 24 minecraft:air if block 207 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 207 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 91 16 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 91 16 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b131 ftm_brick 2
