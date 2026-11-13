scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 125 92 158 minecraft:air if block 125 93 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 125 92 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 92 147 minecraft:air if block 137 93 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 137 92 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 92 147 minecraft:air if block 119 93 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 92 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 92 162 minecraft:air if block 136 93 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 136 92 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 92 149 minecraft:air if block 128 93 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 128 92 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 92 162 minecraft:air if block 119 93 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 119 92 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 92 155 minecraft:air if block 131 93 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 131 92 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 92 154 minecraft:air if block 120 93 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 120 92 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 126 92 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 126 92 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b58 ftm_brick 2
