scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 909 151 198 minecraft:air if block 909 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 909 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 899 151 202 minecraft:air if block 899 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 899 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 914 151 202 minecraft:air if block 914 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 914 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 902 151 198 minecraft:air if block 902 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 902 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 906 151 202 minecraft:air if block 906 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 906 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 903 151 201 minecraft:air if block 903 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 903 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 905 151 199 minecraft:air if block 905 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 905 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 910 151 201 minecraft:air if block 910 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 910 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 910 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 910 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b381 ftm_brick 2
