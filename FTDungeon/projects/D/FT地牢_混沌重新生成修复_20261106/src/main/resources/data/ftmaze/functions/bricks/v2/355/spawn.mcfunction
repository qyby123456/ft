scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1063 91 198 minecraft:air if block 1063 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1063 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1053 91 202 minecraft:air if block 1053 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1053 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1068 91 202 minecraft:air if block 1068 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1068 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1056 91 198 minecraft:air if block 1056 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1056 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1060 91 202 minecraft:air if block 1060 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1060 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1057 91 201 minecraft:air if block 1057 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1057 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1059 91 199 minecraft:air if block 1059 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1059 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1064 91 201 minecraft:air if block 1064 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1064 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1064 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1064 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b355 ftm_brick 2
