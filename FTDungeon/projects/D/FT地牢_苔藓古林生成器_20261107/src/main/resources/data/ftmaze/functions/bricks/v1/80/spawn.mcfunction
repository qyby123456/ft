scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 121 31 92 minecraft:air if block 121 32 92 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 121 31 92 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 31 102 minecraft:air if block 137 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 137 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 31 91 minecraft:air if block 134 32 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 134 31 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 31 102 minecraft:air if block 124 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 124 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 31 96 minecraft:air if block 128 32 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 128 31 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 31 99 minecraft:air if block 119 32 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 119 31 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 31 90 minecraft:air if block 127 32 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 127 31 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 31 101 minecraft:air if block 130 32 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 130 31 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 31 92 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 31 92 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b80 ftm_brick 2
