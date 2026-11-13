scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 792 31 198 minecraft:air if block 792 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 792 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 31 202 minecraft:air if block 782 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 782 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 31 202 minecraft:air if block 797 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 797 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 785 31 198 minecraft:air if block 785 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 785 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 31 202 minecraft:air if block 789 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 789 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 31 201 minecraft:air if block 786 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 786 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 788 31 199 minecraft:air if block 788 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 788 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 793 31 201 minecraft:air if block 793 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 793 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 793 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 793 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b335 ftm_brick 2
