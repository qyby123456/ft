scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 91 120 minecraft:air if block 93 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 93 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 91 130 minecraft:air if block 109 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 109 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 91 119 minecraft:air if block 106 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 106 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 91 130 minecraft:air if block 96 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 96 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 91 124 minecraft:air if block 100 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 100 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 91 127 minecraft:air if block 91 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 91 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 91 118 minecraft:air if block 99 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 99 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 91 129 minecraft:air if block 102 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 102 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 91 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 91 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b53 ftm_brick 2
