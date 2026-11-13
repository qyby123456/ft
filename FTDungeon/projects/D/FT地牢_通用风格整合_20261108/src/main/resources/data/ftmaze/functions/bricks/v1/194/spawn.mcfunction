scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 782 151 24 minecraft:air if block 782 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 782 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 799 151 38 minecraft:air if block 799 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 799 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 151 18 minecraft:air if block 797 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 797 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 779 151 38 minecraft:air if block 779 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 779 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 151 32 minecraft:air if block 789 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 789 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 793 151 25 minecraft:air if block 793 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 793 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 151 18 minecraft:air if block 786 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 786 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 151 30 minecraft:air if block 797 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 797 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 783 151 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 783 151 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b194 ftm_brick 2
