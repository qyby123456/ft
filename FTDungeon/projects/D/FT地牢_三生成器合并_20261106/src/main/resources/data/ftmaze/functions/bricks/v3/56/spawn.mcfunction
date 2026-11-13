scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 94 minecraft:air if block 122 92 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 91 104 minecraft:air if block 135 92 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 135 91 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 91 91 minecraft:air if block 137 92 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 91 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 91 102 minecraft:air if block 119 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 91 97 minecraft:air if block 130 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 130 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 91 90 minecraft:air if block 127 92 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 127 91 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 91 99 minecraft:air if block 124 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 124 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 91 98 minecraft:air if block 136 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 136 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 91 95 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 91 95 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b56 ftm_brick 2
