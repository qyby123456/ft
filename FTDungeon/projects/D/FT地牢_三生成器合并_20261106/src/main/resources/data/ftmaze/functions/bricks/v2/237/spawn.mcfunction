scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 31 148 minecraft:air if block 1785 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1785 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 31 163 minecraft:air if block 1800 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1800 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 31 143 minecraft:air if block 1800 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1800 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1784 31 163 minecraft:air if block 1784 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1784 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 31 156 minecraft:air if block 1792 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1792 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 31 153 minecraft:air if block 1802 32 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1802 31 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 31 155 minecraft:air if block 1782 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1782 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 31 143 minecraft:air if block 1790 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1790 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 31 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 31 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b237 ftm_brick 2
