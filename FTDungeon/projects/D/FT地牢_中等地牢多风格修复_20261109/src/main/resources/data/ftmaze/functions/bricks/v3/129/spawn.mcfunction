scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 76 91 60 minecraft:air if block 76 92 60 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 76 91 60 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 88 91 74 minecraft:air if block 88 92 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 88 91 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 72 minecraft:air if block 70 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 61 minecraft:air if block 90 92 61 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 61 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 91 68 minecraft:air if block 81 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 81 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 63 minecraft:air if block 70 92 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 91 74 minecraft:air if block 77 92 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 77 91 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 83 91 58 minecraft:air if block 83 92 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 83 91 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 77 91 60 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 77 91 60 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b129 ftm_brick 2
