scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1035 91 198 minecraft:air if block 1035 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1035 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1025 91 202 minecraft:air if block 1025 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1025 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 91 202 minecraft:air if block 1040 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1040 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1028 91 198 minecraft:air if block 1028 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1028 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 91 202 minecraft:air if block 1032 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1032 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1029 91 201 minecraft:air if block 1029 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1029 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1031 91 199 minecraft:air if block 1031 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1031 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1036 91 201 minecraft:air if block 1036 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1036 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1036 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1036 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b380 ftm_brick 2
