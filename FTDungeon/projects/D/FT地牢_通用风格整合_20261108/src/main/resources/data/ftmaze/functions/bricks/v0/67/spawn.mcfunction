scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 91 178 minecraft:air if block 178 92 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 178 91 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 192 91 189 minecraft:air if block 192 92 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 192 91 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 176 minecraft:air if block 190 92 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 176 91 189 minecraft:air if block 176 92 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 176 91 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b67 ftm_brick 2
