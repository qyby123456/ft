scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1021 91 198 minecraft:air if block 1021 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1021 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1011 91 202 minecraft:air if block 1011 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1011 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1026 91 202 minecraft:air if block 1026 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1026 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1014 91 198 minecraft:air if block 1014 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1014 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1018 91 202 minecraft:air if block 1018 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1018 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1015 91 201 minecraft:air if block 1015 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1015 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1017 91 199 minecraft:air if block 1017 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1017 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 91 201 minecraft:air if block 1022 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1022 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1022 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1022 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b354 ftm_brick 2
