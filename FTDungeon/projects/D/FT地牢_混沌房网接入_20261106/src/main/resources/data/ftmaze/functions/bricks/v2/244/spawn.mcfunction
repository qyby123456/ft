scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1742 31 31 minecraft:air if block 1742 32 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1742 31 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1759 31 44 minecraft:air if block 1759 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1759 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 31 25 minecraft:air if block 1757 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1757 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1739 31 44 minecraft:air if block 1739 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1739 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1749 31 39 minecraft:air if block 1749 32 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1749 31 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1753 31 32 minecraft:air if block 1753 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1753 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1746 31 25 minecraft:air if block 1746 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1746 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1757 31 37 minecraft:air if block 1757 32 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1757 31 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1743 31 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1743 31 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b244 ftm_brick 2
