scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 37 25 minecraft:air if block 641 38 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 641 37 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 37 17 minecraft:air if block 658 38 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 658 37 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 653 37 33 minecraft:air if block 653 38 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 653 37 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 37 16 minecraft:air if block 646 38 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 646 37 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 37 23 minecraft:air if block 651 38 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 651 37 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 37 33 minecraft:air if block 638 38 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 638 37 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 37 18 minecraft:air if block 638 38 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 638 37 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 37 30 minecraft:air if block 646 38 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 646 37 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 37 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 37 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b185 ftm_brick 2
