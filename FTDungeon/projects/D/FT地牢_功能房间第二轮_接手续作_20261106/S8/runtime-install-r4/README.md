# S8 冻结候选真实安装与外部接入补证

本目录实际状态：FAILED。完整安装与管理员本人进出已完成，普通玩家注册夹具发生UnsupportedOperationException，未完成整项验证。下文为当时方案草案，最终成功补证以runtime-install-r5为准，不得将本目录说明文字当作通过证据。

独立补证目录 runtime-install-r4；仅制作方补证，不是独立验收放行。候选仍为 4d768bcaa073ba63da090ea9b6f64068c474d8a4c4d627217a81803e0c83fd37，原19纵深、门凹位、20岩壳及全部家具/头颅源NBT均未改动。

本目录 server/world 是实际安装后保存的隔离世界。原 deployment-plan 的24个文件原样复制到世界数据包目录；其中20个分片逐个与冻结分片SHA相同。s8_frozen_install.zip 是实际安装包的归档；s8_admin_access.zip 是独立管理员主持包。精确包SHA、世界区域文件、输入、Java、日志和命令返回值由 INSTALLATION-EVIDENCE.json 绑定。原包 plan.json 的“未执行”是冻结时历史文本，当前隔离执行事实以本补证为准，没有回改旧清单。

在新隔离世界中，先用40条原生命令把整个240×29×145体积填成石头，逐格确认1009200格均为石头；执行原清空函数后再逐格确认1009200格全为空气。随后实际执行管理员安装函数，内部调用原清空函数和原20片place template命令；安装后逐格扫描整个体积，核对原记录、省略空气、屏障与全部56个BE。墙、栅栏、楼梯仅允许原生邻接属性变化，其余属性必须相同；12个紫色床的旧color字段在原文件保留，原生1.20.1按purple_bed状态加载颜色10、保存时省略旧字段，单独记录bed-normalization.json，没有删改源载荷。

## 可执行安装和管理员主持方案

适用 Java 1.20.1、有原版函数与place template权限的独立测试服务器。本轮实际运行在 Forge 47.4.10。把两个ZIP直接放入所选隔离世界 datapacks，再启动世界或执行 /reload。安装函数固定作用于主世界，完整覆盖 [1024,128,1024] 至 [1263,156,1168]，并在外部 [998,128,998] 至 [1002,132,1002] 建平台及净空；所选区域应为空闲测试区。它会保留结构屏障，不开放洞口。

由OP玩家执行：

```mcfunction
/function s8_admin:install
/function s8_admin:enter
/function s8_admin:return
```

install 自动加载覆盖安装区及外部平台的216个区块，建外部落脚平台，按同一原点执行完整空气清空，再放置全部20片。enter 将命令执行者送至安装内受支撑、身体无碰撞的入口落点；return 将执行者送回结构外平台。安装与外部进出是同一套已实测函数，不要求玩家修改内部坐标或破坏屏障。

管理员主持普通玩家时，由管理员执行以下两条，把 S8InstallGuest 替换为在线玩家名：

```mcfunction
/execute as S8InstallGuest run function s8_admin:enter
/execute as S8InstallGuest run function s8_admin:return
```

普通玩家无需OP；管理员的execute权限主持函数，@s保留为所选玩家。测试同时验证管理员本人及无权限2的普通玩家四次进出：位置正确、身体无碰撞、脚下有完整支撑，屏障进出前后均90282格。未提供普通玩家自行执行管理函数的权限系统，也不声称完成客户端联网、人手输入或多人运营测试。

## 实证范围与复现

本轮命令源是实际 net.minecraft.server.level.ServerPlayer，使用真实 ServerGamePacketListenerImpl 与未联网的测试传输；OP名单显式授予主持者等级4，普通测试玩家未授予OP。命令经服务器原生命令分派器执行，@s和execute as解析真实玩家实体；没有用控制台冒充玩家，没有在命令后手动修正传送位置。日志中的S8_INSTALL_COMMAND和S8_INSTALL_PASS可与JSON结果交叉核对。

运行 run.ps1 可启动本次工程，但不要重复使用已有世界作为“全新尝试”。需要全新复现时，用 node prepare.mjs runtime-install-r5 生成同级新目录，再运行新目录 run.ps1；配方脚本复用S8内已有可重建编译输出，保留先前日志/源码。新目录原包内容和输入SHA需再次核对。首次初始化的平坦世界属性不影响固定安装区测试。

runtime-install-r1权限和磁盘失败、r2检查器误报、r3管理员本人成功的历史均保留；旧候选清单和旧部署计划不覆盖。没有重复既有4413点遍历、32容器或6门控制测试，没有触碰正式实例、其他组或生成器。19/20视觉及头颅原生客户端显示仍由父线程独立复核。
