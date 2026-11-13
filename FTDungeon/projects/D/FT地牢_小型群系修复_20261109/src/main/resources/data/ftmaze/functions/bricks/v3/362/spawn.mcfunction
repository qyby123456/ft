scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 801 31 198 minecraft:air if block 801 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 801 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 791 31 202 minecraft:air if block 791 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 791 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 806 31 202 minecraft:air if block 806 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 806 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 794 31 198 minecraft:air if block 794 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 794 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 31 202 minecraft:air if block 798 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 798 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 795 31 201 minecraft:air if block 795 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 795 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 31 199 minecraft:air if block 797 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 797 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 802 31 201 minecraft:air if block 802 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 802 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 802 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 802 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b362 ftm_brick 2
