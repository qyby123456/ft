scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 759 31 198 minecraft:air if block 759 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 759 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 749 31 202 minecraft:air if block 749 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 749 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 764 31 202 minecraft:air if block 764 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 764 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 752 31 198 minecraft:air if block 752 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 752 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 756 31 202 minecraft:air if block 756 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 756 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 753 31 201 minecraft:air if block 753 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 753 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 755 31 199 minecraft:air if block 755 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 755 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 760 31 201 minecraft:air if block 760 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 760 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 760 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 760 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b361 ftm_brick 2
