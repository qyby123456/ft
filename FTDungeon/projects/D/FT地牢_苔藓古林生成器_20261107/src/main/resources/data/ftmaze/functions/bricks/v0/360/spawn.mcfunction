scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 793 91 198 minecraft:air if block 793 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 793 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 783 91 202 minecraft:air if block 783 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 783 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 91 202 minecraft:air if block 798 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 798 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 786 91 198 minecraft:air if block 786 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 786 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 790 91 202 minecraft:air if block 790 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 790 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 787 91 201 minecraft:air if block 787 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 787 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 789 91 199 minecraft:air if block 789 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 789 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 794 91 201 minecraft:air if block 794 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 794 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 794 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 794 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b360 ftm_brick 2
