(async () => {
const { chromium } = require("playwright");

const endpoint = process.argv[2] || "http://127.0.0.1:9235";
const screenshotPath = process.argv[3];
const modelPath = process.argv[4];
const browser = await chromium.connectOverCDP(endpoint);
const contexts = browser.contexts();
if (!contexts.length || !contexts[0].pages().length) {
  throw new Error("Blockbench did not expose an editor page");
}

const page = contexts[0].pages()[0];
await page.waitForTimeout(800);

if (modelPath) {
  const confirmButton = page.getByText("确定", { exact: true });
  if (await confirmButton.count()) {
    await confirmButton.first().click();
  }
  await page.evaluate(async (path) => {
    if (typeof globalThis.Blockbench?.read !== "function") {
      throw new Error("Blockbench.read is unavailable");
    }
    await new Promise((resolve, reject) => {
      globalThis.Blockbench.read([path], { readtype: "text" }, (files) => {
        try {
          if (!files?.length) throw new Error("Blockbench returned no files");
          if (typeof globalThis.loadModelFile !== "function") {
            throw new Error("loadModelFile is unavailable");
          }
          globalThis.loadModelFile(files[0]);
          resolve();
        } catch (error) {
          reject(error);
        }
      });
    });
  }, modelPath);
  await page.waitForTimeout(1200);
}

const imported = await page.evaluate(() => {
  const animations = globalThis.Animation?.all || globalThis.Animator?.animations || [];
  const outliner = globalThis.Outliner?.elements || [];
  return {
    title: document.title,
    projectName: globalThis.Project?.name || null,
    format: globalThis.Format?.id || null,
    mode: globalThis.Modes?.selected?.id || null,
    animationNames: animations.map((animation) => animation.name),
    animationLengths: Object.fromEntries(
      animations.map((animation) => [animation.name, animation.length]),
    ),
    selectedAnimation: globalThis.Animation?.selected?.name || null,
    cameraElements: outliner
      .filter((element) => element.type === "camera" || element.name === "camera_first_person")
      .map((element) => ({ name: element.name, type: element.type })),
    cubeCount: outliner.filter((element) => element.type === "cube").length,
  };
});

const playback = {};
for (const shortName of ["preview_complete", "inspect", "drink", "walk", "sprint", "jump"]) {
  playback[shortName] = await page.evaluate(async (requestedName) => {
    const animations = globalThis.Animation?.all || globalThis.Animator?.animations || [];
    const animation = animations.find((item) => item.name.endsWith(`.${requestedName}`));
    if (!animation) return { found: false };
    animation.select();
    globalThis.Timeline.time = Math.min(animation.length * 0.42, animation.length - 0.01);
    globalThis.Animator.preview();
    const selectedAt = globalThis.Animation.selected?.name || null;
    const timeAt = globalThis.Timeline.time;
    if (typeof globalThis.Animator.start === "function") {
      globalThis.Animator.start();
      await new Promise((resolve) => setTimeout(resolve, 240));
      if (typeof globalThis.Animator.stop === "function") globalThis.Animator.stop();
    }
    return {
      found: true,
      selectedAt,
      previewTime: timeAt,
      finalTime: globalThis.Timeline.time,
    };
  }, shortName);
}

playback.drinkTransition = await page.evaluate(() => {
  const animations = globalThis.Animation?.all || globalThis.Animator?.animations || [];
  const animation = animations.find((item) => item.name.endsWith(".drink"));
  if (!animation) return { found: false };
  animation.select();
  const probes = [5.84, 6.24, 6.33, 6.42, 6.88];
  return {
    found: true,
    length: animation.length,
    probes: probes.map((time) => {
      globalThis.Timeline.time = time;
      globalThis.Animator.preview();
      return { time, previewedAt: globalThis.Timeline.time };
    }),
  };
});

if (screenshotPath) {
  await page.screenshot({ path: screenshotPath });
}

console.log(JSON.stringify({ imported, playback }, null, 2));
process.exit(0);
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
