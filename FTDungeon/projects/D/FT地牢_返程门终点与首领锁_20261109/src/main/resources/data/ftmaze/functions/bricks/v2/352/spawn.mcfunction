scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 937 91 198 minecraft:air if block 937 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 937 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 927 91 202 minecraft:air if block 927 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 927 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 942 91 202 minecraft:air if block 942 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 942 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 930 91 198 minecraft:air if block 930 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 930 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 934 91 202 minecraft:air if block 934 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 934 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 931 91 201 minecraft:air if block 931 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 931 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 933 91 199 minecraft:air if block 933 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 933 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 938 91 201 minecraft:air if block 938 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 938 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 938 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 938 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b352 ftm_brick 2
