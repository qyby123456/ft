scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 25 212 156 minecraft:air if block 25 213 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 25 212 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 212 145 minecraft:air if block 37 213 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 37 212 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 212 145 minecraft:air if block 19 213 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 19 212 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 212 160 minecraft:air if block 36 213 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 36 212 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 212 147 minecraft:air if block 28 213 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 28 212 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 212 160 minecraft:air if block 19 213 160 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 19 212 160 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 212 153 minecraft:air if block 31 213 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 31 212 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 20 212 152 minecraft:air if block 20 213 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 20 212 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 26 212 156 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 26 212 156 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b140 ftm_brick 2
