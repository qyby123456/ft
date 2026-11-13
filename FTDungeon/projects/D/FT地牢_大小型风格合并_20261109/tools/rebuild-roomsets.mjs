import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = dirname(dirname(fileURLToPath(import.meta.url)));
const styles = ['clockwork', 'forest', 'desert', 'frost', 'swamp', 'coral', 'lava', 'amethyst'];
const selected = process.argv.slice(2);
assert(selected.every(style => styles.includes(style)), 'Use one or more known style names, or no arguments for all eight');
for (const style of selected.length ? selected : styles) {
    const recipe = join(root, 'tools', `rebuild-${style}.mjs`);
    assert(existsSync(recipe), `Style recipe is not ready: ${style}`);
    const result = spawnSync(process.execPath, [recipe], { cwd: root, stdio: 'inherit', windowsHide: true });
    if (result.error) throw result.error;
    assert.equal(result.status, 0, `Style recipe failed: ${style}`);
}
