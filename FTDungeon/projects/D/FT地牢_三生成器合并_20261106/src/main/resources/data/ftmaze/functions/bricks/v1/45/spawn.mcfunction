scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 207 151 121 minecraft:air if block 207 152 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 207 151 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 131 minecraft:air if block 218 152 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 151 117 minecraft:air if block 222 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 222 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 131 minecraft:air if block 205 152 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 151 127 minecraft:air if block 211 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 211 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 151 116 minecraft:air if block 212 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 212 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 122 minecraft:air if block 216 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 151 117 minecraft:air if block 202 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 202 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b45 ftm_brick 2
