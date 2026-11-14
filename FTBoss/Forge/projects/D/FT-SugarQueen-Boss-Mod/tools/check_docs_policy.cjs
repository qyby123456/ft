#!/usr/bin/env node
/*
 * Documentation policy checks for FT-SugarQueen-Boss-Mod.
 *
 * Usage:
 *   node tools/check_docs_policy.cjs             structural checks on the docs library
 *   node tools/check_docs_policy.cjs --staged    pre-commit gate for staged docs changes
 *
 * The pre-commit gate enforces the owner rule that only the dedicated docs-writer
 * role may commit changes under docs/. A docs-writer commit must set:
 *   FTBOSS_DOCS_WRITER=1
 * This is a commit-time gate; it is not a substitute for harness-level write
 * permissions, which project-level agent configuration/plugins must provide.
 */
'use strict';

const fs = require('fs');
const path = require('path');
const cp = require('child_process');

const ROOT = path.resolve(__dirname, '..');
const DOCS = path.join(ROOT, 'docs');
const CANONICAL = [
  'README.md',
  'AGENTS.md',
  'WRITING_RULES.md',
  'ARCHITECTURE.md',
  'architecture/FOOD_WEAPONS.md',
  'REFERENCE_INDEX.md',
];
const LEGACY_NAME_PATTERNS = [
  /\d{8}/,             // dated task files
  /^CHANGE-/i,         // change records
  /^SESSION_/i,
  /^THREAD_/i,
  /^.*_HANDOVER/i,
  /^.*_STATUS/i,
];

let failures = 0;

function fail(message) {
  console.error('FAIL: ' + message);
  failures += 1;
}

function info(message) {
  console.log('ok: ' + message);
}

function hasBom(buffer) {
  return buffer.length >= 3 && buffer[0] === 0xef && buffer[1] === 0xbb && buffer[2] === 0xbf;
}

function checkSingleFile(rel, options) {
  const abs = path.join(ROOT, rel);
  const buffer = fs.readFileSync(abs);
  if (hasBom(buffer)) fail(rel + ': UTF-8 BOM found');
  const text = buffer.toString('utf8');
  if (text.includes('\r')) fail(rel + ': CRLF or stray CR found (LF required)');
  if (!text.endsWith('\n')) fail(rel + ': missing final newline');
  const h1 = (text.match(/^#\s+\S/gm) || []).length;
  if (h1 !== 1) fail(rel + ': expected exactly one H1, found ' + h1);
  if (options && options.added) {
    const base = path.basename(rel);
    for (const pattern of LEGACY_NAME_PATTERNS) {
      if (pattern.test(base)) fail(rel + ': new docs file uses a legacy/task naming pattern');
    }
    if (/[\u4e00-\u9fff]/.test(base)) fail(rel + ': new docs filename must be ASCII');
  }
  return text;
}

function relativeLinks(text) {
  const links = [];
  const re = /\[[^\]]*\]\(([^)]+)\)/g;
  let match;
  while ((match = re.exec(text)) !== null) {
    const target = match[1].trim();
    if (!target || target.startsWith('#') || target.startsWith('http') || target.startsWith('mailto:')) continue;
    links.push(target.split('#')[0]);
  }
  return links;
}

function resolveLink(dir, target) {
  let resolved = path.resolve(dir, target);
  if (fs.existsSync(resolved)) return true;
  try {
    resolved = path.resolve(dir, decodeURIComponent(target));
    return fs.existsSync(resolved);
  } catch (ignored) {
    return false;
  }
}

function structural() {
  const docsExists = fs.existsSync(DOCS);
  if (!docsExists) {
    fail('docs/ directory is missing');
    return;
  }
  for (const rel of CANONICAL) {
    const abs = path.join(DOCS, rel);
    if (!fs.existsSync(abs)) {
      fail('canonical page is missing: docs/' + rel);
      continue;
    }
    const text = checkSingleFile('docs/' + rel);
    const dir = path.dirname(abs);
    for (const target of relativeLinks(text)) {
      if (!resolveLink(dir, target)) fail('broken relative link in ' + rel + ' -> ' + target);
    }
    info('docs/' + rel + ' structure');
  }

  const architecturePages = ['docs/ARCHITECTURE.md', 'docs/architecture/FOOD_WEAPONS.md'];
  for (const rel of architecturePages) {
    if (!fs.existsSync(path.join(ROOT, rel))) continue;
    const text = fs.readFileSync(path.join(ROOT, rel), 'utf8');
    const hits = text.match(/[\w/.-]+\.(?:java|json|cjs|gradle):\d+/g);
    if (hits) fail(rel + ' contains implementation file:line references: ' + hits.join(', '));
  }

  const readme = fs.readFileSync(path.join(DOCS, 'README.md'), 'utf8');
  const dirs = fs.readdirSync(DOCS, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => entry.name);
  for (const dir of dirs) {
    if (!readme.includes(dir + '/') && !readme.includes(dir)) {
      fail('docs/README.md does not describe docs directory: ' + dir);
    }
  }
  info('docs/README.md describes all ' + dirs.length + ' docs directories');
}

function stagedChanges() {
  const run = (args) => cp.execFileSync('git', args, { cwd: ROOT, encoding: 'utf8' });
  const result = [];
  const combined = run(['diff', '--cached', '--name-status', '--diff-filter=ACMRD']);
  for (const line of combined.split(/\r?\n/)) {
    if (!line.trim()) continue;
    const parts = line.split('\t');
    const status = parts[0][0];
    const file = parts[1];
    if (!file || !file.startsWith('docs/')) continue;
    result.push({ status, file });
  }
  return result;
}

function staged() {
  let changes = [];
  try {
    changes = stagedChanges();
  } catch (error) {
    fail('cannot inspect staged files (git repository required): ' + error.message);
    return;
  }
  if (!changes.length) {
    info('no staged docs changes');
    return;
  }
  if (process.env.FTBOSS_DOCS_BASELINE === '1') {
    info('baseline import commit: docs policy checks skipped (one-time documented exception)');
    return;
  }
  if (process.env.FTBOSS_DOCS_WRITER !== '1') {
    fail('staged changes under docs/ are only allowed from the docs-writer role.');
    fail('Set FTBOSS_DOCS_WRITER=1 for an approved docs-writer commit; implementation agents and the Lead must keep docs/ out of their commits.');
    for (const change of changes) console.error('  ' + change.status + ' ' + change.file);
    return;
  }
  let addCount = 0;
  let readmeStaged = false;
  for (const change of changes) {
    if (change.file === 'docs/README.md') readmeStaged = true;
    if (change.status === 'A') addCount += 1;
    if (change.status === 'D') {
      info('docs deletion staged: ' + change.file);
      continue;
    }
    const abs = path.join(ROOT, change.file);
    if (!fs.existsSync(abs)) continue;
    const text = checkSingleFile(change.file, { added: change.status === 'A' });
    if (change.status === 'A' && path.dirname(change.file) !== 'docs') {
      info('new docs page staged: ' + change.file);
    }
    void text;
  }
  if (addCount > 0 && !readmeStaged) {
    fail('new docs pages were staged without updating docs/README.md navigation');
  }
  if (failures === 0) info('docs-writer staged changes accepted (' + changes.length + ' file(s))');
}

const mode = process.argv.includes('--staged') ? 'staged' : 'structural';
if (mode === 'staged') staged();
else structural();

if (failures > 0) {
  console.error('documentation policy: ' + failures + ' failure(s)');
  process.exit(1);
}
console.log('documentation policy: OK (' + mode + ')');
