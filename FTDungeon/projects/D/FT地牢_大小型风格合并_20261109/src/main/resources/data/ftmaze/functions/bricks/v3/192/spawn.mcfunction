scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 91 76 minecraft:air if block 835 92 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 835 91 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 91 88 minecraft:air if block 850 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 850 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 91 68 minecraft:air if block 849 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 849 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 91 88 minecraft:air if block 830 92 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 91 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 91 68 minecraft:air if block 830 92 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 91 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 91 81 minecraft:air if block 843 92 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 843 91 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 91 85 minecraft:air if block 837 92 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 837 91 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 91 69 minecraft:air if block 838 92 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 838 91 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 91 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 91 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b192 ftm_brick 2
