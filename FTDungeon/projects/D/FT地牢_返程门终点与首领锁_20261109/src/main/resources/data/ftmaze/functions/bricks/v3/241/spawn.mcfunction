scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 151 71 minecraft:air if block 1742 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1742 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 151 86 minecraft:air if block 1759 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1759 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1758 151 66 minecraft:air if block 1758 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1758 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 151 86 minecraft:air if block 1739 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1739 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 151 80 minecraft:air if block 1749 152 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1749 151 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1752 151 72 minecraft:air if block 1752 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1752 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1747 151 66 minecraft:air if block 1747 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1747 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 151 77 minecraft:air if block 1757 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1757 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 151 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 151 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b241 ftm_brick 2
