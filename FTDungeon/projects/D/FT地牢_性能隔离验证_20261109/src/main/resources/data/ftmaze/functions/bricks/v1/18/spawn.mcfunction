scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 211 151 minecraft:air if block 178 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 178 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 211 158 minecraft:air if block 194 212 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 194 211 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 211 146 minecraft:air if block 189 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 189 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 211 159 minecraft:air if block 182 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 182 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 157 minecraft:air if block 174 212 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 211 153 minecraft:air if block 186 212 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 186 211 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 211 146 minecraft:air if block 174 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 174 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 211 147 minecraft:air if block 182 212 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 182 211 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b18 ftm_brick 2
