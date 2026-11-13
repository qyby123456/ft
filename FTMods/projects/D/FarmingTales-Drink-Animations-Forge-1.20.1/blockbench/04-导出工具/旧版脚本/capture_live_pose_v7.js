const { chromium } = require("playwright");

(async () => {
  const endpoint = process.argv[2] || "http://127.0.0.1:9235";
  const browser = await chromium.connectOverCDP(endpoint);
  const page = browser.contexts()[0]?.pages()[0];
  if (!page) throw new Error("Blockbench editor page was not found");

  const result = await page.evaluate(() => {
    const animations = globalThis.Animation?.all || globalThis.Animator?.animations || [];
    const wanted = ["draw", "inspect"];
    const tracks = {};
    for (const shortName of wanted) {
      const animation = animations.find((item) => item.name.endsWith(`.${shortName}`));
      if (!animation) continue;
      tracks[shortName] = Object.values(animation.animators || {})
        .filter((animator) =>
          ["viewmodel_motion", "inspect_tilt", "bottle_turn", "user_bottle_pose", "bottle", "wooden_stopper", "recolored_original_neck"].includes(animator.name),
        )
        .map((animator) => ({
          name: animator.name,
          type: animator.type,
          keyframes: (animator.keyframes || []).map((keyframe) => ({
            channel: keyframe.channel,
            time: keyframe.time,
            interpolation: keyframe.interpolation,
            data_points: (keyframe.data_points || []).map((point) => ({
              x: Number(point.x),
              y: Number(point.y),
              z: Number(point.z),
            })),
          })),
        }));
    }
    return {
      title: document.title,
      timelineTime: globalThis.Timeline?.time,
      selectedAnimation: globalThis.Animation?.selected?.name,
      selectedGroup: globalThis.Group?.selected?.name,
      selectedElements: (globalThis.Outliner?.selected || []).map((item) => item.name),
      tracks,
    };
  });

  console.log(JSON.stringify(result, null, 2));
  process.exit(0);
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
