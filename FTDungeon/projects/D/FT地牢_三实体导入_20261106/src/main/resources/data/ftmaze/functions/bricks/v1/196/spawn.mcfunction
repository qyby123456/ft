scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 884 151 23 minecraft:air if block 884 152 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 884 151 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 901 151 38 minecraft:air if block 901 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 901 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 151 18 minecraft:air if block 900 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 900 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 151 38 minecraft:air if block 881 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 881 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 891 151 32 minecraft:air if block 891 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 891 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 894 151 24 minecraft:air if block 894 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 894 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 151 18 minecraft:air if block 889 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 889 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 899 151 29 minecraft:air if block 899 152 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 899 151 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 885 151 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 885 151 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b196 ftm_brick 2
