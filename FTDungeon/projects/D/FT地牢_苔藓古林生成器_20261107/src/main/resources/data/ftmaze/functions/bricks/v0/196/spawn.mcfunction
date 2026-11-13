scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 894 211 34 minecraft:air if block 894 212 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 894 211 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 211 21 minecraft:air if block 880 212 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 880 211 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 211 18 minecraft:air if block 897 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 897 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 211 37 minecraft:air if block 882 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 882 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 886 211 28 minecraft:air if block 886 212 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 886 211 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 211 27 minecraft:air if block 900 212 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 900 211 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 211 19 minecraft:air if block 888 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 888 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 893 211 24 minecraft:air if block 893 212 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 893 211 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 895 211 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 895 211 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b196 ftm_brick 2
