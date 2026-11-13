scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 811 91 198 minecraft:air if block 811 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 811 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 801 91 202 minecraft:air if block 801 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 801 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 816 91 202 minecraft:air if block 816 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 816 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 804 91 198 minecraft:air if block 804 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 804 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 808 91 202 minecraft:air if block 808 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 808 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 805 91 201 minecraft:air if block 805 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 805 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 807 91 199 minecraft:air if block 807 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 807 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 812 91 201 minecraft:air if block 812 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 812 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 812 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 812 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b349 ftm_brick 2
