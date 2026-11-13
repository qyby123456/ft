scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 31 100 minecraft:air if block 641 32 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 641 31 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 31 115 minecraft:air if block 658 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 658 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 657 31 95 minecraft:air if block 657 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 657 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 31 115 minecraft:air if block 638 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 638 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 31 109 minecraft:air if block 648 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 648 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 31 101 minecraft:air if block 651 32 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 651 31 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 31 95 minecraft:air if block 646 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 646 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 31 106 minecraft:air if block 656 32 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 656 31 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 31 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 31 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b178 ftm_brick 2
