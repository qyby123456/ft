scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 125 92 102 minecraft:air if block 125 93 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 125 92 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 92 91 minecraft:air if block 137 93 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 92 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 92 91 minecraft:air if block 119 93 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 92 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 92 106 minecraft:air if block 136 93 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 136 92 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 92 93 minecraft:air if block 128 93 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 128 92 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 92 106 minecraft:air if block 119 93 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 92 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 92 99 minecraft:air if block 131 93 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 131 92 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 92 98 minecraft:air if block 120 93 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 120 92 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 126 92 102 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 126 92 102 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b56 ftm_brick 2
