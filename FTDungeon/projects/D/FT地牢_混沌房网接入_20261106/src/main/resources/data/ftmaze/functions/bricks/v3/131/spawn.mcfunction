scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 35 minecraft:air if block 194 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 91 48 minecraft:air if block 209 92 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 209 91 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 208 91 30 minecraft:air if block 208 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 208 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 91 48 minecraft:air if block 191 92 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 191 91 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 91 36 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 91 36 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b131 ftm_brick 2
