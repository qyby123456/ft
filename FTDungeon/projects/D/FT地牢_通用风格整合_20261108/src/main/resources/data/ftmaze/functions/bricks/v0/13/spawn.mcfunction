scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 211 123 minecraft:air if block 150 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 150 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 211 130 minecraft:air if block 166 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 166 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 211 118 minecraft:air if block 161 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 161 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 211 131 minecraft:air if block 154 212 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 154 211 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 211 129 minecraft:air if block 146 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 146 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 158 211 125 minecraft:air if block 158 212 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 158 211 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 211 118 minecraft:air if block 146 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 146 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 154 211 119 minecraft:air if block 154 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 154 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b13 ftm_brick 2
