scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 895 91 198 minecraft:air if block 895 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 895 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 885 91 202 minecraft:air if block 885 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 885 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 91 202 minecraft:air if block 900 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 900 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 91 198 minecraft:air if block 888 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 888 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 892 91 202 minecraft:air if block 892 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 892 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 91 201 minecraft:air if block 889 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 889 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 891 91 199 minecraft:air if block 891 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 891 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 896 91 201 minecraft:air if block 896 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 896 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 896 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 896 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b351 ftm_brick 2
