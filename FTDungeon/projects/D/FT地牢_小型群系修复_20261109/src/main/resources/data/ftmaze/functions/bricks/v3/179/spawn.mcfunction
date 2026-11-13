scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 593 31 98 minecraft:air if block 593 32 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 593 31 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 31 115 minecraft:air if block 608 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 608 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 590 31 114 minecraft:air if block 590 32 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 590 31 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 31 95 minecraft:air if block 608 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 608 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 600 31 107 minecraft:air if block 600 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 600 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 609 31 105 minecraft:air if block 609 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 609 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 589 31 104 minecraft:air if block 589 32 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 589 31 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 602 31 99 minecraft:air if block 602 32 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 602 31 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 593 31 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 593 31 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b179 ftm_brick 2
