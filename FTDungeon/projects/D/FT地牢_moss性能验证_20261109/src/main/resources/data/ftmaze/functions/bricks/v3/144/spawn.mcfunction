scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 25 152 204 minecraft:air if block 25 153 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 25 152 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 152 193 minecraft:air if block 37 153 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 37 152 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 152 193 minecraft:air if block 19 153 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 19 152 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 152 208 minecraft:air if block 36 153 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 36 152 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 28 152 195 minecraft:air if block 28 153 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 28 152 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 19 152 208 minecraft:air if block 19 153 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 19 152 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 152 201 minecraft:air if block 31 153 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 31 152 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 20 152 200 minecraft:air if block 20 153 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 20 152 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 26 152 204 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 26 152 204 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b144 ftm_brick 2
