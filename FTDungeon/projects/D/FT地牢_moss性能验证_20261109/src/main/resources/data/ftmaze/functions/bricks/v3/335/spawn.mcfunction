scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 834 151 198 minecraft:air if block 834 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 834 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 824 151 202 minecraft:air if block 824 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 824 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 151 202 minecraft:air if block 839 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 839 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 827 151 198 minecraft:air if block 827 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 827 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 831 151 202 minecraft:air if block 831 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 831 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 828 151 201 minecraft:air if block 828 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 828 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 199 minecraft:air if block 830 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 835 151 201 minecraft:air if block 835 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 835 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 835 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 835 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b335 ftm_brick 2
