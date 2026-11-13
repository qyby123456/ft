scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 91 150 minecraft:air if block 66 92 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 66 91 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 91 160 minecraft:air if block 79 92 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 79 91 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 91 147 minecraft:air if block 81 92 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 81 91 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 91 158 minecraft:air if block 63 92 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 63 91 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 153 minecraft:air if block 74 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 91 146 minecraft:air if block 71 92 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 71 91 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 68 91 155 minecraft:air if block 68 92 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 68 91 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 91 154 minecraft:air if block 80 92 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 80 91 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 91 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 91 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b50 ftm_brick 2
