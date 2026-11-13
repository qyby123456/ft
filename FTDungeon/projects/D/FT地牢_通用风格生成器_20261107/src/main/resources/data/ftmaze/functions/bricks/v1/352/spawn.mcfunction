scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 811 31 198 minecraft:air if block 811 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 811 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 801 31 202 minecraft:air if block 801 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 801 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 816 31 202 minecraft:air if block 816 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 816 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 804 31 198 minecraft:air if block 804 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 804 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 808 31 202 minecraft:air if block 808 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 808 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 805 31 201 minecraft:air if block 805 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 805 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 807 31 199 minecraft:air if block 807 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 807 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 812 31 201 minecraft:air if block 812 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 812 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 812 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 812 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b352 ftm_brick 2
