# 囚狱装饰合并

0.20.1将重制后的24款囚狱方块和装饰内置于FT地牢同一个JAR。所有24款加入原有“FT地牢怪物”创造栏，取消独立装饰分页，基础6款仍可从原版建筑方块栏取得。

保留ftdungeonprops命名空间、原有物品/方块ID、模型、材质、碰撞及灯具交互。JAR包含主模组ftdungeonmobs与兼容内容模块ftdungeonprops两个modId，兼容之前独立版放置的方块与获取命令；同一实例不能同时保留独立装饰JAR。

可编辑母版位于model_sources/prison_decor，署名见该目录CREDITS.md。运行资源位于src/main/resources/assets/ftdungeonprops及data/ftdungeonprops。

本次只合并内容和创造栏入口，不替换原有地牢建筑或修改存档。游戏运行中更换JAR后，需要完整退出并重新启动客户端；多人环境服务端也需安装同一合并版。
