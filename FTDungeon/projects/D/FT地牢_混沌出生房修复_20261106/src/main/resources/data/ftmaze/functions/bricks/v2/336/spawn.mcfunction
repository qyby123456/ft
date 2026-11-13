scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 834 31 198 minecraft:air if block 834 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 834 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 824 31 202 minecraft:air if block 824 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 824 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 31 202 minecraft:air if block 839 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 839 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 827 31 198 minecraft:air if block 827 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 827 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 831 31 202 minecraft:air if block 831 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 831 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 828 31 201 minecraft:air if block 828 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 828 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 31 199 minecraft:air if block 830 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 830 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 835 31 201 minecraft:air if block 835 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 835 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 835 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 835 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b336 ftm_brick 2
