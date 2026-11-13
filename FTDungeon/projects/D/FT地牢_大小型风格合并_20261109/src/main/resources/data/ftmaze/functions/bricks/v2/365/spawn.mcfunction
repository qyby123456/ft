scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 885 211 198 minecraft:air if block 885 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 885 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 875 211 202 minecraft:air if block 875 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 875 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 890 211 202 minecraft:air if block 890 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 890 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 878 211 198 minecraft:air if block 878 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 878 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 882 211 202 minecraft:air if block 882 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 882 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 879 211 201 minecraft:air if block 879 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 879 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 211 199 minecraft:air if block 881 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 881 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 886 211 201 minecraft:air if block 886 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 886 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 886 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 886 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b365 ftm_brick 2
