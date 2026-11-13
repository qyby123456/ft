scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 786 151 55 minecraft:air if block 786 152 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 786 151 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 799 151 70 minecraft:air if block 799 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 799 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 779 151 69 minecraft:air if block 779 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 779 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 151 65 minecraft:air if block 790 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 790 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 151 57 minecraft:air if block 798 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 798 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 779 151 58 minecraft:air if block 779 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 779 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 151 72 minecraft:air if block 786 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 786 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 793 151 52 minecraft:air if block 793 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 793 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 786 151 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 786 151 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b191 ftm_brick 2
