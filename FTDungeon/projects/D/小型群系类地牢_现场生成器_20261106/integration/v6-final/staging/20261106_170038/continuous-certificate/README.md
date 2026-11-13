# 有界原生外伸支持 v6

替代full-width-certificate-v5的单格AABB限制。旧v5支持目录、九房候选及其ready保持冻结。本目录新contract/schema/script须作为一套消费，不混用旧contractSHA。

## 实际检查结论

逐格解码原01、派生07对应12场景的4416个采集条目，**wall/fence/fence_gate命中0**。当前房间不会因墙/栅栏1.5高形状触发旧限制，但该限制确实不适合泛化到合法原生外伸，现修正为显式有界策略。

nativeShapeBoundsByBlockName列出当前真实源palette的单格包络及精确命名白名单墙/栅栏/栅栏门的[0,0,0]..[1,1.5,1]包络，不按任意名字后缀放行。只检验边界不生成形状；native boxes仍必须integration在世界中通过玩家CollisionContext取回。新增blockName字段必须匹配contract内sourceBlockNames原格身份，不能将原石砖冒称栅栏。

4416集合已有X/Z相邻格余量、Y0至最高脚面+4范围。当前所有脚面最低Y1；若墙/栅栏最高外伸1.5，y<0的来源最高到0.5，不可能侵入脚Y>=1的身体；源Y高于上界也不会向下外伸。已命名策略无横向外伸，原水平邻格集合已覆盖可能侵入footprint的全部来源。因此**当前这些条带无需增加格数**，collection-audit.json记录理由及0命中结果。若其他条带更低、出现横向/向下/更高外伸，必须重新计算扩张requiredBlocks，再冻结新契约；未知形状/超界形状继续fail closed，不能裁成单格或删掉它。

完整原生AABB不裁剪：合法1.5高栅栏数据能进入计算，若其真的撞上身体/三高通道，会得到HEADROOM_COLLISION等几何FAIL，**不会被误报为非法单格数据**。检测到未知块名或超过声明包络则拒绝输入，要求扩展策略和采集集合。

## 文件消费

integration使用本目录contract.json、native-input.schema.json及certificate.py；native输出新增blockName，scene保留quarterTurns/nativeContext/sourceSHA、原绝对局部AABB和fluidEmpty。运行：

`python -B certificate.py --contract contract.json --native <integration/native-shapes.json> --out <integration/finite-certificate.json>`

原有限区间方法不变：native水平边界±0.3形成精确有理数开区间和边界单点，完整覆盖闭合原宽条带；脚高按真实原生最高支撑分段、三高、0.6步高、原始端点不变。side doublegate仍单独窄路，不能封中央拱洞。原36案例2200检查与原宽/三高要求不减。

即便全部静态箱覆盖通过，仍只报告CONDITIONAL_FINITE_COVERAGE_PASS；原生双向step/碰撞上下文/动态状态证据必须补齐，不能把有限箱覆盖或合成测试冒称世界运动已通过。当前没有真实native-shapes输入。

9项测试通过：原6项合成覆盖正负例，加合法1.5高外伸可被正常计算且输入未裁剪/修改、未知与超包络拒绝、源身份不匹配拒绝。测试使用合成数据，非Minecraft世界实测。旧支持交付仍冻结，integration请改读新v6消费路径。
