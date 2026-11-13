scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 918 151 198 minecraft:air if block 918 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 918 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 908 151 202 minecraft:air if block 908 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 908 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 923 151 202 minecraft:air if block 923 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 923 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 911 151 198 minecraft:air if block 911 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 911 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 915 151 202 minecraft:air if block 915 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 915 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 912 151 201 minecraft:air if block 912 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 912 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 914 151 199 minecraft:air if block 914 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 914 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 919 151 201 minecraft:air if block 919 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 919 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 919 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 919 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b337 ftm_brick 2
