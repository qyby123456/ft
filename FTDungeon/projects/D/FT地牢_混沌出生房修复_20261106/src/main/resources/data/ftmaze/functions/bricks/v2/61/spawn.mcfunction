scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 91 119 minecraft:air if block 150 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 150 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 91 130 minecraft:air if block 166 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 166 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 91 130 minecraft:air if block 146 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 146 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 91 117 minecraft:air if block 163 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 163 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 91 123 minecraft:air if block 158 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 158 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 91 126 minecraft:air if block 151 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 151 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 91 116 minecraft:air if block 155 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 155 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 91 123 minecraft:air if block 147 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 147 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b61 ftm_brick 2
