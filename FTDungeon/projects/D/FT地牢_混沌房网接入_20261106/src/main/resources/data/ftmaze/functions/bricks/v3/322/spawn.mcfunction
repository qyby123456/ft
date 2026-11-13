scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 792 91 198 minecraft:air if block 792 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 792 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 91 202 minecraft:air if block 782 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 782 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 91 202 minecraft:air if block 797 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 797 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 785 91 198 minecraft:air if block 785 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 785 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 91 202 minecraft:air if block 789 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 789 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 91 201 minecraft:air if block 786 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 786 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 788 91 199 minecraft:air if block 788 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 788 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 793 91 201 minecraft:air if block 793 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 793 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 793 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 793 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b322 ftm_brick 2
