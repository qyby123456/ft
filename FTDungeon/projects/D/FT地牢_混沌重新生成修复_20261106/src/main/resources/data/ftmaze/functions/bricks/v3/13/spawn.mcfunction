scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 211 121 minecraft:air if block 150 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 150 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 165 211 132 minecraft:air if block 165 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 165 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 211 119 minecraft:air if block 163 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 163 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 211 132 minecraft:air if block 147 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 147 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 211 126 minecraft:air if block 158 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 158 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 211 128 minecraft:air if block 151 212 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 151 211 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 118 minecraft:air if block 155 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 132 minecraft:air if block 155 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b13 ftm_brick 2
