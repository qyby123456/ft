scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 183 211 154 minecraft:air if block 183 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 183 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 145 minecraft:air if block 194 212 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 145 minecraft:air if block 174 212 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 211 158 minecraft:air if block 193 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 193 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 175 211 158 minecraft:air if block 175 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 175 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 211 144 minecraft:air if block 184 212 144 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 184 211 144 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 211 150 minecraft:air if block 189 212 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 189 211 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 177 211 151 minecraft:air if block 177 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 177 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 184 211 154 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 184 211 154 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b18 ftm_brick 2
