scoreboard players set #r1 s1_enc 2
tellraw @a[gamemode=!creative,gamemode=!spectator,x=30,y=-58,z=2,dx=24,dy=14,dz=24] {"text":"盘根王座 · 已清理","color":"green"}
bossbar set s1_encounters:king visible false
bossbar set s1_encounters:king players
tellraw @a[gamemode=!creative,gamemode=!spectator,x=30,y=-58,z=2,dx=24,dy=14,dz=24] {"text":"盘根骷髅王已击败！本次王座战完成，重进房间不会重复刷王。","color":"gold"}
