scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 885 91 198 minecraft:air if block 885 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 885 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 875 91 202 minecraft:air if block 875 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 875 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 91 202 minecraft:air if block 890 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 890 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 878 91 198 minecraft:air if block 878 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 878 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 91 202 minecraft:air if block 882 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 882 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 879 91 201 minecraft:air if block 879 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 879 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 91 199 minecraft:air if block 881 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 881 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 886 91 201 minecraft:air if block 886 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 886 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 886 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 886 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b368 ftm_brick 2
