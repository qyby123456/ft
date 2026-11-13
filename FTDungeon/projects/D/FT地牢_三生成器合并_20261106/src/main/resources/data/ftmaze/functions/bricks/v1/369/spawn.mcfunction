scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 927 91 198 minecraft:air if block 927 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 927 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 917 91 202 minecraft:air if block 917 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 917 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 932 91 202 minecraft:air if block 932 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 932 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 920 91 198 minecraft:air if block 920 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 920 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 924 91 202 minecraft:air if block 924 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 924 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 921 91 201 minecraft:air if block 921 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 921 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 923 91 199 minecraft:air if block 923 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 923 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 928 91 201 minecraft:air if block 928 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 928 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 928 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 928 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b369 ftm_brick 2
