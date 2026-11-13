scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 591 91 63 minecraft:air if block 591 92 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 591 91 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 91 77 minecraft:air if block 608 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 608 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 91 57 minecraft:air if block 608 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 608 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 588 91 77 minecraft:air if block 588 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 588 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 91 71 minecraft:air if block 598 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 598 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 66 minecraft:air if block 605 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 91 57 minecraft:air if block 597 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 597 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 588 91 57 minecraft:air if block 588 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 588 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 592 91 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 592 91 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b181 ftm_brick 2
