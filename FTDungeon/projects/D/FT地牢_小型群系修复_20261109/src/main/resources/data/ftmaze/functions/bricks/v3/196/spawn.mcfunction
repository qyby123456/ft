scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 894 91 34 minecraft:air if block 894 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 894 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 91 21 minecraft:air if block 880 92 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 880 91 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 91 18 minecraft:air if block 897 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 897 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 91 37 minecraft:air if block 882 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 882 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 886 91 28 minecraft:air if block 886 92 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 886 91 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 91 27 minecraft:air if block 900 92 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 900 91 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 91 19 minecraft:air if block 888 92 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 888 91 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 893 91 24 minecraft:air if block 893 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 893 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 895 91 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 895 91 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b196 ftm_brick 2
