scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 33 34 minecraft:air if block 1785 34 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1785 33 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 33 24 minecraft:air if block 1802 34 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1802 33 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1798 33 44 minecraft:air if block 1798 34 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1798 33 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 33 24 minecraft:air if block 1782 34 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1782 33 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 33 44 minecraft:air if block 1782 34 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1782 33 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 33 27 minecraft:air if block 1792 34 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1792 33 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1799 33 33 minecraft:air if block 1799 34 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1799 33 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 33 40 minecraft:air if block 1790 34 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1790 33 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 33 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 33 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b245 ftm_brick 2
