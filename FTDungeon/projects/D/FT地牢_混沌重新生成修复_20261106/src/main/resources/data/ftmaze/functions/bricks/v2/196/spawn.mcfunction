scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 894 151 34 minecraft:air if block 894 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 894 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 151 21 minecraft:air if block 880 152 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 880 151 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 151 18 minecraft:air if block 897 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 897 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 151 37 minecraft:air if block 882 152 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 882 151 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 886 151 28 minecraft:air if block 886 152 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 886 151 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 151 27 minecraft:air if block 900 152 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 900 151 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 151 19 minecraft:air if block 888 152 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 888 151 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 893 151 24 minecraft:air if block 893 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 893 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 895 151 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 895 151 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b196 ftm_brick 2
