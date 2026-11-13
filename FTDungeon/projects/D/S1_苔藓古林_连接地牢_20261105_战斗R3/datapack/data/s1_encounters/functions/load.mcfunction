scoreboard objectives add s1_enc dummy
scoreboard players add #enabled s1_enc 0
execute unless data storage s1_encounters:state installed run scoreboard players set #enabled s1_enc 1
data modify storage s1_encounters:state installed set value 1b
scoreboard players add #clock s1_enc 0
bossbar add s1_encounters:king {"text":"盘根骷髅王","color":"dark_green"}
bossbar set s1_encounters:king color green
bossbar set s1_encounters:king visible false
scoreboard players add #r2 s1_enc 0
scoreboard players add #r3 s1_enc 0
scoreboard players add #r4 s1_enc 0
scoreboard players add #r5 s1_enc 0
scoreboard players add #r6 s1_enc 0
scoreboard players add #r8 s1_enc 0
scoreboard players add #r9 s1_enc 0
scoreboard players add #r1 s1_enc 0
