scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 792 151 198 minecraft:air if block 792 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 792 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 151 202 minecraft:air if block 782 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 782 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 151 202 minecraft:air if block 797 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 797 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 785 151 198 minecraft:air if block 785 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 785 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 151 202 minecraft:air if block 789 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 789 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 151 201 minecraft:air if block 786 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 786 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 788 151 199 minecraft:air if block 788 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 788 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 793 151 201 minecraft:air if block 793 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 793 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 793 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 793 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b325 ftm_brick 2
