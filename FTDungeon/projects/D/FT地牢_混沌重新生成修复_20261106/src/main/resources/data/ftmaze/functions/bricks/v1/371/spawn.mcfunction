scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1011 91 198 minecraft:air if block 1011 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1011 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1001 91 202 minecraft:air if block 1001 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1001 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1016 91 202 minecraft:air if block 1016 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1016 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1004 91 198 minecraft:air if block 1004 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1004 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1008 91 202 minecraft:air if block 1008 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1008 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1005 91 201 minecraft:air if block 1005 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1005 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1007 91 199 minecraft:air if block 1007 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1007 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1012 91 201 minecraft:air if block 1012 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1012 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1012 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1012 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b371 ftm_brick 2
