scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 801 91 198 minecraft:air if block 801 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 801 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 791 91 202 minecraft:air if block 791 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 791 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 806 91 202 minecraft:air if block 806 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 806 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 794 91 198 minecraft:air if block 794 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 794 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 91 202 minecraft:air if block 798 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 798 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 795 91 201 minecraft:air if block 795 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 795 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 91 199 minecraft:air if block 797 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 797 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 802 91 201 minecraft:air if block 802 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 802 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 802 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 802 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b366 ftm_brick 2
