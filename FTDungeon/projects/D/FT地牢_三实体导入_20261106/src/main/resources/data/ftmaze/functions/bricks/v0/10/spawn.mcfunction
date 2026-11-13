scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 121 211 148 minecraft:air if block 121 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 121 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 211 158 minecraft:air if block 137 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 137 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 211 147 minecraft:air if block 134 212 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 134 211 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 211 158 minecraft:air if block 124 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 124 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 211 152 minecraft:air if block 128 212 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 128 211 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 211 155 minecraft:air if block 119 212 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 119 211 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 211 146 minecraft:air if block 127 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 127 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 211 157 minecraft:air if block 130 212 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 130 211 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 211 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 211 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b10 ftm_brick 2
