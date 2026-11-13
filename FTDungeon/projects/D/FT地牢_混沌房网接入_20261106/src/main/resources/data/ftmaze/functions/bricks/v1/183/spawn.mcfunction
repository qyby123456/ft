scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 692 91 62 minecraft:air if block 692 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 692 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 708 91 77 minecraft:air if block 708 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 708 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 708 91 57 minecraft:air if block 708 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 708 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 688 91 77 minecraft:air if block 688 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 688 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 91 71 minecraft:air if block 698 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 698 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 91 66 minecraft:air if block 705 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 705 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 697 91 57 minecraft:air if block 697 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 697 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 688 91 57 minecraft:air if block 688 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 688 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 692 91 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 692 91 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b183 ftm_brick 2
