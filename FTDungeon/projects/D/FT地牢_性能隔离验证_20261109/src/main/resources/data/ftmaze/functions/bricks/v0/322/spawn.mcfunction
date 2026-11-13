scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 792 211 198 minecraft:air if block 792 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 792 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 211 202 minecraft:air if block 782 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 782 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 211 202 minecraft:air if block 797 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 797 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 785 211 198 minecraft:air if block 785 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 785 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 211 202 minecraft:air if block 789 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 789 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 211 201 minecraft:air if block 786 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 786 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 788 211 199 minecraft:air if block 788 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 788 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 793 211 201 minecraft:air if block 793 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 793 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 793 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 793 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b322 ftm_brick 2
