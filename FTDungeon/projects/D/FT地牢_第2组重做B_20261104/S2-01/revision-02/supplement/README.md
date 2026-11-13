# S2-01 revision-02：仅补Lodestone四向内部近景

状态：SUPPLEMENT_READY_PENDING_IMAGE_REVIEW_HOLD。等待独立复核聊天图像判定；不是ACCEPTED，S2-02继续锁定。

本目录只补证，未更改候选几何、原33项清单、既有截图或加载校验。源NBT仍为336edadd450ee6bd1bf52abe737a483110823b1cf8625e3687d866a26a1aeb4a。原33项及清单本身哈希均在补证后复核一致。

## 观看入口

- lodestone-interior-north.png：从北侧看，保留外接平台、入口台阶及扶墙；展示南拱、东西侧拱框、祭坛与绕行区。
- lodestone-interior-south.png：从南侧看，检查北门内侧、两侧拱框和祭坛阶梯。
- lodestone-interior-west.png / lodestone-interior-east.png：分别剖去对应近墙，检查对面拱门、相邻两侧拱框、柱头与挑檐。
- lodestone-four-interior-contact-sheet.png仅把四张原始截图缩小并列标注，单张原图保留，不是另一个渲染器生成的检查图。
- 完整外壳沿用../incoming/G2B-01-R2/lodestone-exterior.png，未裁切/未覆盖；路径与哈希收录registration.json。

## 源/派生和裁切规则

prepare.py直接解码最终源NBT，不重跑建筑recipe。仅在新派生件中将屋顶/垛口(y>=16,z>=6)及各自近侧墙带替换为空气。北墙带z6..9、南墙带z>=27、西墙带x<=3且z>=6、东墙带x>=21且z>=6；近墙裁切仅对y>=4生效。其余方块逐点核对不变。
所有z<6的外接平台、入口台阶和扶墙在四个派生件中均逐点保留。每个方向只移除一个近侧墙带，保留另外三侧墙及门洞（共同边角交集除外）。源NBT完整屋顶和入口从未被裁掉。
derivation.json给出源/派生SHA-256、删去的非空气方块数及裁切规则；screenshots.json给出截图SHA-256、相机参数、派生和最终源哈希。preview-only-*.nbt四个文件均严禁放置、部署、进入房间候选池。

## 截图真实性与边界

截图来自bundled minecraft-builder-skill的实际Lodestone WebGL浏览器页面，使用headless Edge和原查看器拖动/滚轮，不是软件材质图，不是Minecraft实机截图。保留查看器文件名和尺寸UI，单张完整1600×1200视口，无后期局部裁切或生成式修图。
逐向调整近景与相机，结构和入口不截出画框；北向图可直接检查三级入口，侧向图便于核查内拱/柱檐。所有WebGL页面无pageerror。没有改变已有世界放置、标签存活和碰撞独立复核结论，也没有新增实机验收声明。

registration.json是本次补证待审登记；SUPPLEMENT_MANIFEST.json仅管理本目录新文件，不替换或扩充原FILE_MANIFEST.json。
