scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 125 212 158 minecraft:air if block 125 213 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 125 212 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 212 147 minecraft:air if block 137 213 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 137 212 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 212 147 minecraft:air if block 119 213 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 119 212 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 212 162 minecraft:air if block 136 213 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 136 212 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 212 149 minecraft:air if block 128 213 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 128 212 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 212 162 minecraft:air if block 119 213 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 119 212 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 212 155 minecraft:air if block 131 213 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 131 212 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 212 154 minecraft:air if block 120 213 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 120 212 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 126 212 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 126 212 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b10 ftm_brick 2
