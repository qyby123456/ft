scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 209 152 186 minecraft:air if block 209 153 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 209 152 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 152 175 minecraft:air if block 221 153 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 221 152 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 152 175 minecraft:air if block 203 153 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 203 152 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 152 190 minecraft:air if block 220 153 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 220 152 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 152 177 minecraft:air if block 212 153 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 212 152 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 152 190 minecraft:air if block 203 153 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 203 152 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 152 183 minecraft:air if block 215 153 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 215 152 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 204 152 182 minecraft:air if block 204 153 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 204 152 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 210 152 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 210 152 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b47 ftm_brick 2
