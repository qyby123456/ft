# 本地编译依赖（不随源码导出捆绑）

将合法取得的 Forge 1.20.1 对应依赖放到此目录，文件名须与 `build.gradle` 的 flatDir 坐标一致：

- `ft-dungeon-mobs-forge-1.20.1-0.5.0.jar`
- `geckolib-4.8.4.jar`
- `irons_spellbooks-1.20.1-3.16.3.jar`
- `curios-forge-5.14.1+1.20.1.jar`
- `player-animation-lib-forge-1.0.2-rc1+1.20.jar`
- `irons_lib-1.20.1-2.1.0.jar`

前3项用于编译；后三项用于隔离运行验证。Forge和Gradle须已在本地缓存才能离线构建。`libs` 中可能保留其他历史参考文件，但它们不参与依赖声明或最终JAR。

正式客户端/服务器需要安装FT地牢怪物0.5.x及其原有依赖，再增加本模块JAR；不要把此目录全部复制进 `mods`，不要安装用户提供的Fabric参考JAR。
