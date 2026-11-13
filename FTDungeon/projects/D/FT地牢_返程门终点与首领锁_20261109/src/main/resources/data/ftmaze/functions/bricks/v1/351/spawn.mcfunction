scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 769 31 198 minecraft:air if block 769 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 769 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 759 31 202 minecraft:air if block 759 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 759 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 774 31 202 minecraft:air if block 774 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 774 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 762 31 198 minecraft:air if block 762 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 762 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 766 31 202 minecraft:air if block 766 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 766 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 763 31 201 minecraft:air if block 763 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 763 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 765 31 199 minecraft:air if block 765 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 765 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 770 31 201 minecraft:air if block 770 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 770 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 770 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 770 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b351 ftm_brick 2
