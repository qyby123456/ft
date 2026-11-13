scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 211 121 minecraft:air if block 151 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 151 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 211 131 minecraft:air if block 162 212 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 162 211 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 211 117 minecraft:air if block 166 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 166 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 211 131 minecraft:air if block 149 212 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 149 211 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 127 minecraft:air if block 155 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 211 116 minecraft:air if block 156 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 156 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 211 122 minecraft:air if block 160 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 160 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 211 117 minecraft:air if block 146 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 146 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b13 ftm_brick 2
