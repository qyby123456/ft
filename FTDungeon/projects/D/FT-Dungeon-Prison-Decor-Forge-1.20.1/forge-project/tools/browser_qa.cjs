const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const {pathToFileURL} = require('node:url');
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');

const root = path.resolve(__dirname, '../..');
const preview = path.join(root, 'preview');
const html = path.join(preview, 'index.html');
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex');

async function main() {
    const browser = await chromium.launch({
        executablePath: process.env.EDGE_EXECUTABLE || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
        headless: true,
        args: ['--enable-unsafe-swiftshader'],
    });
    const context = await browser.newContext({viewport: {width: 1440, height: 1050}, deviceScaleFactor: 1});
    const page = await context.newPage();
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    page.on('console', message => {
        if (message.type() === 'error') errors.push(message.text());
    });
    await page.goto(pathToFileURL(html).href);
    await page.waitForFunction(() => loadedTextures.size === Object.keys(assetData.textures).length);
    assert.equal(await page.locator('.card').count(), 12, 'Initial remake filter');
    await page.selectOption('#family', '');
    assert.equal(await page.locator('.card').count(), 24);
    const models = [];
    const identifiers = await page.locator('.card').evaluateAll(cards => cards.map(card => card.dataset.id));
    for (const identifier of identifiers) {
        await page.locator(`.card[data-id="${identifier}"]`).click();
        await page.waitForTimeout(80);
        const measurement = await page.evaluate(() => {
            const pixels = new Uint8Array(canvas.width * canvas.height * 4);
            graphics.readPixels(0, 0, canvas.width, canvas.height, graphics.RGBA, graphics.UNSIGNED_BYTE, pixels);
            let colored = 0;
            for (let offset = 3; offset < pixels.length; offset += 4) {
                if (pixels[offset] > 0) colored++;
            }
            return {id: selected.id, pixels: colored, error: graphics.getError(), remade: selected.remade};
        });
        assert.equal(measurement.id, identifier);
        assert.ok(measurement.pixels > 1000, 'Visible WebGL geometry: ' + identifier);
        assert.equal(measurement.error, 0, 'No WebGL error: ' + identifier);
        models.push(measurement);
    }
    const lamps = [];
    for (const identifier of ['prison_alchemical_lamp', 'prison_rune_beacon']) {
        await page.locator(`.card[data-id="${identifier}"]`).click();
        await page.evaluate(() => window.scrollTo(0, 0));
        await page.waitForTimeout(80);
        const before = hash(await page.locator('#canvas').screenshot());
        await page.locator('#light').click();
        await page.waitForTimeout(80);
        const after = hash(await page.locator('#canvas').screenshot());
        assert.notEqual(before, after, 'LIT changes visible pixels');
        assert.equal(await page.evaluate(() => lit), false);
        await page.locator('#light').click();
        assert.equal(await page.evaluate(() => lit), true);
        lamps.push({id: identifier, on_png_sha256: before, off_png_sha256: after});
    }
    await page.locator('#turn').click();
    assert.ok(await page.evaluate(() => Math.abs(yaw - (42 * Math.PI / 180 + Math.PI / 2)) < .001));
    await page.locator('#reset').click();
    const canvasBox = await page.locator('#canvas').boundingBox();
    await page.mouse.move(canvasBox.x + 100, canvasBox.y + 100);
    await page.mouse.down();
    await page.mouse.move(canvasBox.x + 180, canvasBox.y + 130);
    await page.mouse.up();
    assert.ok(await page.evaluate(() => Math.abs(yaw - 42 * Math.PI / 180) > .1), 'Drag orbit');
    await page.mouse.wheel(0, 120);
    await page.waitForTimeout(80);
    assert.ok(await page.evaluate(() => zoom !== .075), 'Wheel zoom');
    await page.locator('#reset').click();
    await page.locator('#spin').click();
    assert.equal(await page.evaluate(() => spinning), true);
    await page.locator('#spin').click();
    assert.equal(await page.evaluate(() => spinning), false);
    await page.locator('#reset').click();
    await page.fill('#search', 'prison_ossuary_niche');
    assert.equal(await page.locator('.card').count(), 1, 'Search exact ID');
    await page.fill('#search', '');
    await page.selectOption('#family', 'lighting');
    assert.equal(await page.locator('.card').count(), 8, 'Lighting family');
    await page.selectOption('#family', 'remade');
    assert.equal(await page.locator('.card').count(), 12, 'Final remake filter');
    await page.locator('.card[data-id="prison_alchemical_lamp"]').click();
    await page.evaluate(() => window.scrollTo(0, 0));
    await page.screenshot({path: path.join(preview, 'interactive-desktop.png')});
    await page.setViewportSize({width: 390, height: 860});
    await page.evaluate(() => window.scrollTo(0, 0));
    const mobile = await page.evaluate(() => ({viewport: innerWidth, content: document.documentElement.scrollWidth}));
    assert.ok(mobile.content <= mobile.viewport, 'No mobile horizontal overflow');
    await page.screenshot({path: path.join(preview, 'interactive-mobile.png')});
    assert.equal(errors.length, 0, 'No browser JavaScript or console errors');
    const report = {
        status: 'pass',
        browser: 'Microsoft Edge headless WebGL, software renderer',
        html_sha256: hash(fs.readFileSync(html)),
        models, lamps,
        initial_remake_cards: 12,
        all_cards: 24,
        controls: ['LIT both remade lamps', 'rotate90', 'reset', 'drag', 'wheel', 'spin', 'search', 'family', 'remake filter'],
        mobile, errors,
        limitations: ['Offline viewer only; not Minecraft client, light propagation or FPS validation.'],
    };
    fs.writeFileSync(path.join(root, 'qa/browser-preview.json'), JSON.stringify(report, null, 2) + '\n');
    console.log(JSON.stringify({status: 'pass', models: models.length, remade: models.filter(model => model.remade).length, errors: 0}));
    await browser.close();
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
