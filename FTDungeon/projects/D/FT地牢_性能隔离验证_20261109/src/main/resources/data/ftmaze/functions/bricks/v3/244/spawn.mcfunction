scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 151 31 minecraft:air if block 1742 152 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1742 151 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 151 44 minecraft:air if block 1759 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1759 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 151 25 minecraft:air if block 1757 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1757 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 151 44 minecraft:air if block 1739 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1739 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 151 39 minecraft:air if block 1749 152 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1749 151 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1753 151 32 minecraft:air if block 1753 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1753 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1746 151 25 minecraft:air if block 1746 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1746 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 151 37 minecraft:air if block 1757 152 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1757 151 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 151 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 151 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b244 ftm_brick 2
