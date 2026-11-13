'use strict';
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..', '..');
const FILE = {
  well: path.join(ROOT, 'integration', 'kubejs', 'server_scripts', 'block_events', 'wishing_well.js'),
  coins: path.join(ROOT, 'integration', 'kubejs', 'server_scripts', 'item_events', 'economy', 'coins.js'),
  tooltip: path.join(ROOT, 'integration', 'kubejs', 'client_scripts', 'tooltips', 'wishing_well.js'),
  wellOrig: path.join(ROOT, 'reference', 'shop-pre-fix-wishing_well.js'),
  coinsOrig: path.join(ROOT, 'reference', 'shop-pre-fix-coins.js')
};
const CAND_SHA = { well: '3584FE6F9524427290B52E574A40BA2A508A843C90AEF75B588B0DD930BAE646', coins: '8DE1E09EE9FF3E30CC5B2EBD5D5FDF806B749B88984F548BD7A4CA32CB6E7618' };
const ORIG_SHA = { well: 'C4C54ABEF55F7166945C01252427C202429C46653435E7AC01B9AFBC91F7EC47', coins: 'DA13EB06B0A2B9B3BDD8CDECC0B38BAB8799406327C37D749E96C65B5632E7BE' };
const TOOLTIP_SHA = '1D525C1E0AFCFAA5CD226765B0F4F82BCF74132B65D82AB103AD407949C33830';
const Q = String.fromCharCode(34);
const MAXSAFE = 9007199254740991;
const SCROLL_NBT = { spellContainer: true };
let pass = 0; let fail = 0; const failures = [];
function ok(c, label, extra) { if (c) { pass++; return true; } fail++; failures.push(label + (extra === undefined ? '' : ' :: ' + extra)); return false; }
function eq(a, b, label) { return ok(a === b, label, 'actual=' + JSON.stringify(a) + ' expected=' + JSON.stringify(b)); }
function near(a, b, tol, label) { return ok(Math.abs(a - b) <= tol, label, 'actual=' + a + ' expected=' + b + ' tol=' + tol); }
function sha(buf) { return crypto.createHash('sha256').update(buf).digest('hex').toUpperCase(); }
function section(t) { console.log('== ' + t); }
function q(s) { return s.split('~').join(Q); }
function mulberry32(a) { return function () { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }

function nbtText(value) {
  if (value === undefined || value === null) return '';
  if (typeof value === 'string') return value;
  const keys = Object.keys(value).sort();
  const parts = [];
  for (let i = 0; i < keys.length; i++) parts.push(keys[i] + '=' + JSON.stringify(value[keys[i]]));
  return parts.join(';');
}

class Stack {
  constructor(id, count, nbt) {
    this.id = id === undefined ? 'minecraft:air' : String(id);
    this.count = count === undefined ? 1 : Number(count);
    this.nbt = nbt || {};
    this.maxCount = this.id === 'irons_spellbooks:scroll' ? 1 : 64;
  }
  isEmpty() { return this.count <= 0 || this.id === 'minecraft:air'; }
  copy() { const c = new Stack(this.id, this.count, JSON.parse(JSON.stringify(this.nbt))); c.maxCount = this.maxCount; if (this.scroll) c.scroll = { spellId: this.scroll.spellId, level: this.scroll.level }; return c; }
  shrink(n) { this.count -= n; return this; }
  get displayName() { return { getString: () => this.id }; }
  getItem() { return this.id; }
  getId() { return this.id; }
  getMaxStackSize() { return this.maxCount; }
}

class JavaStack {
  constructor(id, count, nbt) { this.id = String(id); this.count = count === undefined ? 1 : Number(count); this.nbt = nbt || {}; }
  getId() { return this.id; }
  getCount() { return this.count; }
  getNbt() { return this.nbt; }
  copy() { return new JavaStack(this.id, this.count, JSON.parse(JSON.stringify(this.nbt))); }
}

function canItemStacksStack(first, second) {
  if (!first || !second) return false;
  if (first.isEmpty() || second.isEmpty()) return false;
  if (String(first.id) !== String(second.id)) return false;
  return nbtText(first.nbt) === nbtText(second.nbt);
}

class CustomChestMenu {
  constructor(id, data) { this.containerId = id; this.data = data; }
}

class MainInvWrapper {
  constructor(inv) { this.inv = inv; this.slotCount = 36; this.inserts = []; this.setChangedCount = 0; }
  getSlots() { return this.slotCount; }
  getStackInSlot(slot) { return this.inv.getStackInSlot(slot); }
  setChanged() { this.setChangedCount++; this.inv.setChanged(); }
  insertItem(stack, simulate) {
    this.inserts.push({ simulate: !!simulate, id: stack.id, count: stack.count });
    if (this.inv.mode === 'full') return stack.copy();
    if (!simulate && this.inv.mode === 'realfail') return stack.copy();
    if (!simulate && this.inv.mode === 'noop') return new Stack('minecraft:air', 0);
    const incoming = stack.copy();
    for (let slot = 0; slot < this.slotCount; slot++) {
      const existing = this.inv.getStackInSlot(slot);
      if (existing.isEmpty() || !canItemStacksStack(existing, incoming)) continue;
      const room = Math.min(existing.getMaxStackSize(), incoming.getMaxStackSize()) - existing.count;
      if (room <= 0) continue;
      const moved = Math.min(room, incoming.count);
      if (!simulate) existing.count += moved;
      incoming.count -= moved;
      if (incoming.count <= 0) return new Stack('minecraft:air', 0);
    }
    for (let slot = 0; slot < this.slotCount; slot++) {
      if (!this.inv.getStackInSlot(slot).isEmpty()) continue;
      const placed = incoming.copy();
      placed.count = Math.min(placed.count, placed.getMaxStackSize());
      incoming.count -= placed.count;
      if (!simulate) this.inv.setStackInSlot(slot, placed);
      if (incoming.count <= 0) return new Stack('minecraft:air', 0);
    }
    return incoming;
  }
}

class Inv {
  constructor(n) { this.slots = n === undefined ? 41 : n; this.stacks = []; for (let i = 0; i < this.slots; i++) this.stacks.push(new Stack('minecraft:air', 0)); this.inserts = []; this.setCount = 0; this.changed = 0; this.mode = 'normal'; this.failSlot = -1; }
  getStackInSlot(i) { return this.stacks[i]; }
  setStackInSlot(i, s) { if (i === this.failSlot) throw new Error('slot write refused (test)'); this.stacks[i] = s; this.setCount++; }
  setChanged() { this.changed++; }
  insertItem(stack, simulate) {
    const incoming = stack.copy();
    const limit = 36;
    for (let slot = 0; slot < limit; slot++) {
      const existing = this.stacks[slot];
      if (existing.isEmpty() || !canItemStacksStack(existing, incoming)) continue;
      const room = Math.min(existing.getMaxStackSize(), incoming.getMaxStackSize()) - existing.count;
      if (room <= 0) continue;
      const moved = Math.min(room, incoming.count);
      if (!simulate) existing.count += moved;
      incoming.count -= moved;
      if (incoming.count <= 0) return new Stack('minecraft:air', 0);
    }
    if (this.mode === 'full') return incoming;
    if (!simulate && this.mode === 'realfail') return incoming;
    if (!simulate && this.mode === 'noop') return new Stack('minecraft:air', 0);
    for (let slot = 0; slot < limit; slot++) {
      if (!this.stacks[slot].isEmpty()) continue;
      const placed = incoming.copy();
      placed.count = Math.min(placed.count, placed.getMaxStackSize());
      incoming.count -= placed.count;
      if (!simulate) this.setStackInSlot(slot, placed);
      if (incoming.count <= 0) return new Stack('minecraft:air', 0);
    }
    return incoming;
  }
  add(id, count, nbt) {
    for (let i = 0; i < this.slots; i++) if (this.stacks[i].id === id && this.stacks[i].count > 0) { this.stacks[i].count += count; return this.stacks[i]; }
    for (let i = 0; i < this.slots; i++) if (this.stacks[i].count === 0) { const s = new Stack(id, count, nbt); this.stacks[i] = s; return s; }
    throw new Error('test inventory full while seeding');
  }
}

function makeText(v) {
  const t = { value: String(v), getString: () => t.value, append: o => { t.value += o && o.getString ? o.getString() : String(o); return t; }, hover: () => t, clickRunCommand: () => t, copy: () => makeText(t.value) };
  return t;
}

function blueprint(dx, dy, dz) {
  const ax = Math.abs(dx); const az = Math.abs(dz);
  if (dy === -1) {
    if (ax <= 2 && az <= 2) return 'minecraft:stone_bricks';
    if ((dx === -3 && dz === 0) || (dx === 3 && dz === 0) || (dx === 0 && dz === -3) || (dx === 0 && dz === 3)) return 'minecraft:stone_brick_stairs';
    return 'minecraft:air';
  }
  if (dy === 0) {
    if (dx === 0 && dz === 0) return 'farmingtales:wishing_well_core';
    if (Math.max(ax, az) === 2) return (dx === 0 || dz === 0) ? 'minecraft:stone_brick_slab' : 'minecraft:stone_brick_wall';
    if (Math.max(ax, az) === 1) return 'minecraft:water';
    return 'minecraft:air';
  }
  if (dy === 1 || dy === 2) return (ax === 2 && az === 2) ? 'minecraft:stone_brick_wall' : 'minecraft:air';
  if (dy === 3) { if (Math.max(ax, az) !== 2) return 'minecraft:air'; return (dx === 0 || dz === 0) ? 'minecraft:emerald_block' : 'minecraft:stone_brick_slab'; }
  return 'minecraft:air';
}
function makeLevel(server, core) {
  const lv = {
    dimension: 'minecraft:overworld', overrides: {}, entities: [], server: server, core: core,
    blockId(x, y, z) {
      const key = (x - core.x) + ',' + (y - core.y) + ',' + (z - core.z);
      if (Object.prototype.hasOwnProperty.call(lv.overrides, key)) return lv.overrides[key];
      return blueprint(x - core.x, y - core.y, z - core.z);
    },
    getBlock(x, y, z) { return { id: lv.blockId(x, y, z), x: x, y: y, z: z }; },
    getEntitiesWithin(query) { return lv.entities.filter(e => (!query.type || e.type === query.type) && Math.abs(e.x - query.x) <= query.radius && Math.abs(e.y - query.y) <= query.radius && Math.abs(e.z - query.z) <= query.radius); }
  };
  return lv;
}
function makePlayer(server, o) {
  o = o || {};
  const p = {
    type: 'minecraft:player',
    uuid: o.uuid === undefined ? 'uuid-default' : o.uuid, username: o.username || 'Player',
    x: o.x === undefined ? 0.5 : o.x, y: o.y === undefined ? 64 : o.y, z: o.z === undefined ? 0.5 : o.z,
    level: o.level || { dimension: 'minecraft:overworld' },
    inventory: o.inventory || new Inv(41), creative: !!o.creative, shift: !!o.shift,
    messages: [], statuses: [], commands: [], cooldowns: [], data: [], given: [], swings: 0,
    gui: null, guiOpens: 0, throwOnGui: false, throwOnTell: false, throwOnInfo: false,
    openMenu: null, syncs: 0, openInventory: undefined,
    isShiftKeyDown() { return p.shift; }, isCreative() { return p.creative; },
    tell(m) { if (p.throwOnTell) throw new Error('tell failed (test)'); p.messages.push(m && m.getString ? m.getString() : String(m)); },
    setStatusMessage(m) { p.statuses.push(m && m.getString ? m.getString() : String(m)); },
    runCommandSilent(c) { p.commands.push(String(c)); },
    displayClientMessage(m) { p.messages.push(m && m.getString ? m.getString() : String(m)); },
    addItemCooldown(id, t) { p.cooldowns.push([String(id), Number(t)]); },
    swing() { p.swings++; }, give(s) { p.given.push({ id: s.id, count: s.count, nbt: s.nbt }); },
    sendData(k, v) { p.data.push([String(k), String(v)]); },
    sendInventoryUpdate() { p.syncs++; },
    isAlive() { return !p.dead && !p.removed; },
    isSpectator() { return !!p.spectator; },
    closeContainer() {
      if (p.throwOnClose) throw new Error('closeContainer refused (test)');
      p.closed++;
      p.openInventory = server.inventoryMenu;
      if (p.guiCloseHook) p.guiCloseHook();
    }
  };
  p.server = server;
  p.closed = 0;
  p.openInventory = server.inventoryMenu;
  p.openChestGUI = function (title, rows, cb) {
    p.guiOpens++;
    if (p.throwOnGui) throw new Error('GUI open failed (test)');
    let captured = null;
    let capturedContainer = null;
    const buttons = [];
    const gui = { playerSlots: false, closed: null, anyClicked: null, inventoryClicked: null, button: (x, y, stack, text, click) => buttons.push({ x: x, y: y, stack: stack, text: text, click: click }) };
    cb(gui);
    if (server.nativeInventoryLifecycle) {
      captured = server.captureInventory(p);
      capturedContainer = new Inv(36);
      captured.forEach((stack, slot) => capturedContainer.setStackInSlot(slot, stack.copy()));
      p.openInventory = new CustomChestMenu(p.guiOpens + 100, capturedContainer);
      p.guiCapture = captured;
    }
    p.guiCloseHook = () => { if (gui.closed) gui.closed(); };
    p.gui = { title: String(title && title.getString ? title.getString() : title), rows: rows, buttons: buttons, playerSlots: gui.playerSlots, capturedInventory: capturedContainer, close: () => { p.closeContainer(); } };
  };
  return p;
}
function makeEntity(o) {
  const e = {
    type: 'minecraft:item', x: o.x, y: o.y, z: o.z, item: new Stack(o.id, o.count), level: o.level,
    owner: o.owner || null, removed: false, discarded: 0,
    getOwner() { return e.owner; }, isRemoved() { return e.removed; }, isAlive() { return !e.removed; },
    discard() { e.removed = true; e.discarded++; }, setItem(s) { e.item = s; }, getItem() { return e.item; }
  };
  return e;
}
function create(opts) {
  opts = opts || {};
  const sandbox = {};
  const logs = { info: [], warn: [], error: [] };
  const handlers = { blockRight: null, tick: null, spawned: null, itemRight: {}, tooltip: null, commands: null, recipes: null };
  const raw = new Map();
  const saves = [];
  const wrappers = [];
  const inventoryMenu = { menuId: 'inventory-menu' };
  const restoreInventories = new Map();
  const capturedInventories = new Map();
  const restoreCounts = new Map();
  const server = {
    players: [], commands: [], saves: saves, raw: raw, failSave: false, wrappers: wrappers,
    inventoryMenu: inventoryMenu, restoreInventories: restoreInventories, capturedInventories: capturedInventories, restoreCounts: restoreCounts,
    nativeInventoryLifecycle: false,
    runCommandSilent(cmd) { server.commands.push(String(cmd)); },
    captureInventory(p) {
      const main = p.inventory;
      const snapshot = new Map();
      for (let slot = 0; slot < 36; slot++) {
        const existing = main.getStackInSlot(slot);
        if (existing && existing.count > 0) snapshot.set(slot, existing);
        main.setStackInSlot(slot, new Stack('minecraft:air', 0));
      }
      if (snapshot.size > 0) {
        restoreInventories.set(String(p.uuid), snapshot);
        capturedInventories.set(String(p.uuid), snapshot);
      }
      return snapshot;
    },
    restoreInventory(p) {
      restoreInventories.delete(String(p.uuid));
      const restore = restoreCounts.get(String(p.uuid)) || 0;
      restoreCounts.set(String(p.uuid), restore + 1);
      const main = p.inventory;
      for (let slot = 0; slot < 36; slot++) main.setStackInSlot(slot, new Stack('minecraft:air', 0));
      const snapshot = capturedInventories.get(String(p.uuid));
      if (!snapshot) return [];
      const restored = [];
      snapshot.forEach((stack, slot) => { main.setStackInSlot(slot, stack); restored.push(slot); });
      return restored;
    },
    persistentData: {
      getString(k) { return raw.has(k) ? String(raw.get(k)) : ''; },
      putString(k, v) { if (server.failSave) throw new Error('persistent data write failed'); raw.set(k, String(v)); saves.push([k, String(v)]); }
    }
  };
  const wallet = { balances: new Map(), get: 0, set: 0, add: 0 };
  const walletApi = {
    getMoney(p) { wallet.get++; return wallet.balances.get(p.uuid) || 0; },
    setMoney(p, v) { wallet.set++; wallet.balances.set(p.uuid, Number(v)); },
    addMoney(p, v) { wallet.add++; wallet.balances.set(p.uuid, (wallet.balances.get(p.uuid) || 0) + Number(v)); }
  };
  const entries = opts.catalogEntries || [
    { id: 'ftboss:gourmet_blank_blade', nbt: null },
    { id: 'ftboss:dora_nova_launcher', nbt: { FTBossManorWeapon: 'dora' } },
    { id: 'ftboss:wenxuanlan_flywheel', nbt: { XuanlanWhite: true } },
    { id: 'tridents_n_stuff:stormseeker', nbt: null }
  ];
  entries.forEach(e => { e.item = () => e.id; e.form = () => 'DEFAULT'; });
  const catalogApi = {
    ENTRIES: entries,
    stack(entry) { if (entry.id === 'ftboss:unregistered') return new Stack('minecraft:air', 0); return new Stack(entry.id, 1, entry.nbt ? JSON.parse(JSON.stringify(entry.nbt)) : {}); }
  };
  const spells = opts.spells || [
    { id: 'irons_spellbooks:fireball', lootable: true, min: 1, max: 5 },
    { id: 'irons_spellbooks:ice_block', lootable: true, min: 2, max: 3 },
    { id: 'irons_spellbooks:blood_slash', lootable: false, min: 1, max: 4 },
    { id: 'irons_spellbooks:heal', lootable: true, min: 0, max: 3 },
    { id: 'irons_spellbooks:bad_range', lootable: true, min: 4, max: 2 }
  ];
  spells.forEach(s => { s.getSpellId = () => s.id; s.allowLooting = () => s.lootable; s.getMinLevel = () => s.min; s.getMaxLevel = () => s.max; s.getSpellName = () => s.id; });
  const registryApi = { getEnabledSpells: () => spells };
  const container = { mode: opts.containerMode || 'ok' };
  const containerApi = {
    createScrollContainer(spell, level, stack) { stack.scroll = { spellId: String(spell.getSpellId()), level: Number(level) }; stack.nbt = { spellContainer: true, spellId: String(spell.getSpellId()), level: Number(level) }; return stack; },
    get(stack) {
      if (container.mode === 'missing' || !stack.scroll) return null;
      const data = { id: stack.scroll.spellId, level: stack.scroll.level };
      if (container.mode === 'wrongspell') data.id = 'irons_spellbooks:not_the_rolled_one';
      if (container.mode === 'wronglevel') data.level = data.level + 1;
      return { getAllSpells() { return container.mode === 'empty' ? [] : [{ getSpell: () => ({ getSpellId: () => data.id }), getLevel: () => data.level }]; } };
    }
  };
  const handlerUtils = { canItemStacksStack: (a, b) => canItemStacksStack(a, b) };
  const goldCategories = opts.goldPoolCategories || ['iron_spell', 'food', 'coin', 'geode', 'mineral', 'xp_book', 'manor_reward'];
  const goldPool = { mode: opts.goldPoolMode || 'ok', calls: [], seq: 0, lastArgs: null };
  const goldWishesApi = {
    rollRewards(level, goldPots, amount) {
      goldPool.lastArgs = { level: level, goldPots: Number(goldPots), amount: Number(amount) };
      goldPool.calls.push(goldPool.lastArgs);
      const grantable = Math.min(Number(amount), Number(goldPots) + Number(amount) - 100);
      const rolls = [];
      for (let index = 0; index < grantable; index++) {
        const category = String(goldCategories[(goldPool.seq + index) % goldCategories.length]);
        const id = category === 'manor_reward' ? 'ftboss:dora_nova_launcher' : 'minecraft:golden_apple';
        const nbt = { GoldPoolRoll: goldPool.seq + index, GoldPoolCategory: category };
        rolls.push({ category: () => category, stack: () => new JavaStack(id, 1, nbt) });
      }
      goldPool.seq += rolls.length;
      if (goldPool.mode === 'short') return rolls.slice(0, Math.max(0, rolls.length - 1));
      return rolls;
    }
  };
  sandbox.global = sandbox;
  sandbox.console = {
    info: m => { logs.info.push(String(m)); if (opts.throwOnInfo) throw new Error('info log failed (test)'); },
    warn: m => logs.warn.push(String(m)),
    error: m => logs.error.push(String(m)),
    log: m => logs.info.push(String(m))
  };
  sandbox.Math = Object.create(Math);
  sandbox.Math.random = opts.rng || Math.random;
  sandbox.Text = { of: v => makeText(v) };
  sandbox.Item = { of: (id, count) => {
    if (id instanceof JavaStack) return new Stack(id.getId(), id.getCount(), JSON.parse(JSON.stringify(id.getNbt())));
    if (id instanceof Stack) return id.copy();
    return new Stack(id, count === undefined ? 1 : count);
  } };
  sandbox.Java = { loadClass(name) {
    if (name === 'net.sixik.sdmshoprework.SDMShopR') return walletApi;
    if (name === 'net.farmingtales.ftboss.item.ManorWeaponCatalog') return catalogApi;
    if (name === 'io.redspace.ironsspellbooks.api.registry.SpellRegistry') return registryApi;
    if (name === 'io.redspace.ironsspellbooks.api.spells.ISpellContainer') return containerApi;
    if (name === 'dev.latvian.mods.kubejs.item.ItemHandlerUtils') return handlerUtils;
    if (name === 'dev.latvian.mods.kubejs.gui.chest.CustomChestMenu') return CustomChestMenu;
    if (name === 'dev.latvian.mods.kubejs.item.PlayerMainInvWrapper') {
      if (opts.wrapperMissing) throw new Error('PlayerMainInvWrapper missing (test)');
      return class PlayerMainInvWrapper { constructor(inv) { const w = new MainInvWrapper(inv); wrappers.push(w); return w; } };
    }
    if (name === 'cn.farmingtales.qysevent.wishingwell.GoldPotWishes') {
      if (opts.goldPoolMissing) throw new Error('GoldPotWishes missing (test)');
      return goldWishesApi;
    }
    throw new Error('unexpected Java.loadClass: ' + name);
  } };
  const node = () => { const n = { requires: () => n, executes: () => n, then: () => n }; return n; };
  sandbox.Commands = { literal: () => node() };
  sandbox.ItemEvents = { rightClicked: (id, fn) => { handlers.itemRight[String(id)] = fn; }, tooltip: fn => { handlers.tooltip = fn; } };
  sandbox.BlockEvents = { rightClicked: (id, fn) => { handlers.blockRight = fn; } };
  sandbox.EntityEvents = { spawned: fn => { handlers.spawned = fn; } };
  sandbox.ServerEvents = { tick: fn => { handlers.tick = fn; }, commandRegistry: fn => { handlers.commands = fn; }, recipes: fn => { handlers.recipes = fn; } };
  const vmCtx = vm.createContext(sandbox);
  server.nativeInventoryLifecycle = !!opts.nativeInventoryLifecycle;
  return { sandbox: sandbox, vmCtx: vmCtx, logs: logs, handlers: handlers, server: server, wallet: wallet, entries: entries, spells: spells, container: container, raw: raw, saves: saves, wrappers: wrappers, goldPool: goldPool };
}
function load(c) {
  vm.runInContext(fs.readFileSync(FILE.well, 'utf8'), c.vmCtx, { filename: 'wishing_well.js' });
  vm.runInContext(fs.readFileSync(FILE.coins, 'utf8'), c.vmCtx, { filename: 'coins.js' });
  const shaped = { id: () => shaped };
  c.handlers.commands({ commands: c.sandbox.Commands, register: () => {} });
  c.handlers.recipes({ shaped: () => shaped });
}
function loadTooltip(c) { vm.runInContext(fs.readFileSync(FILE.tooltip, 'utf8'), c.vmCtx, { filename: 'tooltip.js' }); }
function K(c, n) { return vm.runInContext(n, c.vmCtx); }
function key(c, uuid) { return K(c, 'FTWW_PROGRESS_PREFIX') + uuid; }
function prog(c, uuid) { const text = c.server.persistentData.getString(key(c, uuid)); return text ? JSON.parse(text) : null; }
function putRaw(c, uuid, text) { c.raw.set(key(c, uuid), text); }
function world(c, o) {
  o = o || {};
  const core = { x: o.cx === undefined ? 100 : o.cx, y: o.cy === undefined ? 64 : o.cy, z: o.cz === undefined ? 100 : o.cz };
  const level = makeLevel(c.server, core);
  level.overrides = o.overrides || {};
  c.core = core; c.level = level; c.server.players = [];
  return level;
}
function player(c, o) { const p = makePlayer(c.server, Object.assign({ level: c.level }, o || {})); c.server.players.push(p); c.level.entities.push(p); return p; }
function coreBlock(c, p) { const lv = p.level; return lv.getBlock(c.core.x, c.core.y, c.core.z); }
function rcBlock(c, p, item, hand) {
  const ev = { player: p, item: item, hand: hand || 'MAIN_HAND', level: p.level, block: coreBlock(c, p), server: c.server, cancelled: false };
  ev.cancel = () => { ev.cancelled = true; };
  c.handlers.blockRight(ev);
  return ev;
}
function rcItem(c, p, item, hand) {
  const ev = { player: p, item: item, hand: hand || 'MAIN_HAND', server: c.server, cancelled: false };
  ev.cancel = () => { ev.cancelled = true; };
  c.handlers.itemRight[String(item.id)](ev);
  return ev;
}
function nativeTick(c) {
  const players = c.server.players;
  for (let index = 0; index < players.length; index++) {
    const p = players[index];
    const key = String(p.uuid);
    if (!c.server.restoreInventories.has(key)) continue;
    if (!p.isAlive() || p.isSpectator()) continue;
    if (p.openInventory && p.openInventory instanceof CustomChestMenu) continue;
    c.server.restoreInventory(p);
  }
}
function ticks(c, n) { for (let i = 0; i < (n || 1); i++) { nativeTick(c); c.handlers.tick({ server: c.server }); } }
function openShopGui(c, empty, who) {
  const actor = who || c.p;
  rcBlock(c, actor, empty || new Stack('minecraft:air', 0), 'MAIN_HAND');
  return actor.gui;
}
function fillMain(c, id, count) {
  for (let slot = 0; slot < 36; slot++) c.p.inventory.setStackInSlot(slot, new Stack(id, count === undefined ? 1 : count));
}
function spawn(c, entity) { c.handlers.spawned({ entity: entity }); }
function wrapper(c) { return c.wrappers.length ? c.wrappers[c.wrappers.length - 1] : null; }
function countMain(c, id) { let total = 0; for (let i = 0; i < 36; i++) { const s = c.p.inventory.getStackInSlot(i); if (s && s.id === id && s.count > 0) total += s.count; } return total; }
function countAll(c, id) { let total = 0; for (let i = 0; i < c.p.inventory.slots; i++) { const s = c.p.inventory.getStackInSlot(i); if (s && s.id === id && s.count > 0) total += s.count; } return total; }
console.log('harness root=' + ROOT);
console.log('candidate well=' + FILE.well);
section('T01 candidate/original hashes and script load');
eq(sha(fs.readFileSync(FILE.well)), CAND_SHA.well, 'candidate wishing_well.js hash');
eq(sha(fs.readFileSync(FILE.coins)), CAND_SHA.coins, 'candidate coins.js hash');
eq(sha(fs.readFileSync(FILE.wellOrig)), ORIG_SHA.well, 'live copy equals the pre-fix baseline hash');
eq(sha(fs.readFileSync(FILE.coinsOrig)), ORIG_SHA.coins, 'original coins.js untouched');
eq(sha(fs.readFileSync(FILE.tooltip)), TOOLTIP_SHA, 'tooltip candidate equals original');
{
  const c = create(); load(c);
  eq(c.logs.error.length, 0, 'no errors logged while loading candidates');
  eq(typeof c.sandbox.ftwwProgress, 'function', 'ftwwProgress loaded from candidate');
  eq(typeof c.sandbox.ftwwDonatePots, 'function', 'ftwwDonatePots loaded from candidate');
  eq(typeof c.sandbox.ftwwPurchase, 'function', 'ftwwPurchase loaded from candidate');
  eq(typeof c.sandbox.ftwwMainInventoryWrapper, 'function', 'ftwwMainInventoryWrapper loaded from candidate');
  eq(typeof c.sandbox.ftwwCountMainInventory, 'function', 'ftwwCountMainInventory loaded from candidate');
  eq(typeof c.sandbox.ftwwRestoreMainInventory, 'function', 'ftwwRestoreMainInventory loaded from candidate');
  eq(typeof c.sandbox.ftwwInsertHandler, 'undefined', 'no synthetic simulate handler exists in the candidate');
  eq(typeof c.sandbox.ftwwPurchaseTarget, 'undefined', 'no captured-inventory fallback target exists in the candidate');
  eq(c.handlers.blockRight !== null, true, 'BlockEvents.rightClicked(core) registered');
  eq(c.handlers.tick !== null, true, 'ServerEvents.tick registered');
  eq(c.handlers.spawned !== null, true, 'EntityEvents.spawned registered');
  eq(!!c.handlers.itemRight['farmingtales:gold_bucket'], true, 'coins.js item handler registered');
  eq(K(c, 'FTWW_SUMMONS').length, 11, 'FTWW_SUMMONS has 11 non-manor summons');
  eq(K(c, 'FTWW_WEAPON_PRICE'), 50000000, 'weapon price 50000000');
  eq(K(c, 'FTWW_SPELL_PRICE'), 5000000, 'spell price 5000000');
  eq(K(c, 'FTWW_LEVEL_THRESHOLDS').join(','), '0,1,10,100', 'level thresholds 0/1/10/100');
  eq(K(c, 'FTWW_OUTCOME_TYPES').length, 10, 'ten outcome types');
  eq(c.sandbox.ftwwMainInventorySlots(), 36, 'main backpack slot count is fixed at 36');
}
section('T02 thresholds and useful chances');
{
  const c = create(); load(c);
  [[0, 0], [1, 1], [9, 1], [10, 2], [99, 2], [100, 3], [150, 3]].forEach(pair => eq(c.sandbox.ftwwLevel({ emeralds: 0, goldPots: pair[0] }), pair[1], 'ftwwLevel(' + pair[0] + ')'));
  eq(c.sandbox.ftwwUsefulChance(0), 31.1, 'useful chance Lv0 = 31.1');
  eq(c.sandbox.ftwwUsefulChance(1), 45, 'useful chance Lv1 = 45');
  eq(c.sandbox.ftwwUsefulChance(2), 60, 'useful chance Lv2 = 60');
  eq(c.sandbox.ftwwUsefulChance(3), 80, 'useful chance Lv3 = 80');
}
section('T03 odds tables and observed distribution');
{
  const c = create(); load(c);
  const odds = K(c, 'FTWW_LEVEL_ODDS');
  const expected = [[39.9, 22, 10, 10, 7, 5, 3, 2.5, 0.5, 0.1], [32, 18, 14, 14, 5, 8, 4, 4, 0.8, 0.2], [22, 14, 17, 17, 4, 11, 6, 7, 1.5, 0.5], [10, 8, 20, 20, 2, 16, 10, 10, 3, 1]];
  eq(odds.length, 4, 'four level rows');
  odds.forEach((row, i) => {
    eq(row.length, 10, 'row ' + i + ' bucket count');
    near(row.reduce((a, b) => a + b, 0), 100, 1e-9, 'row ' + i + ' sums to 100');
    row.forEach((v, j) => eq(v, expected[i][j], 'odds[' + i + '][' + j + ']'));
  });
  const types = K(c, 'FTWW_OUTCOME_TYPES');
  const c2 = create({ rng: mulberry32(20261012) }); load(c2);
  const p = makePlayer(c2.server, { uuid: 'freq' });
  putRaw(c2, 'freq', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  const count = {};
  const N = 20000;
  for (let i = 0; i < N; i++) { const outcome = c2.sandbox.ftwwRollOutcome(c2.server, p); count[outcome.type] = (count[outcome.type] || 0) + 1; }
  types.forEach((t, j) => near(100 * (count[t] || 0) / N, odds[3][j], 0.9, 'Lv3 observed ' + t));
  const nonUseful = 100 * ((count.nothing || 0) + (count.junk || 0) + (count.poop || 0)) / N;
  near(100 - nonUseful, 80, 1.0, 'Lv3 observed useful percentage');
}
section('T04/T05/T06 pot donation hand1, shift64, click cooldown 12');
{
  const c = create(); load(c); world(c);
  const p = player(c, { uuid: 'u-hand1', username: 'Hand1', x: 100.5, y: 64.5, z: 100.5 });
  const held = p.inventory.add('farmingtales:gold_bucket', 1);
  const ev = rcBlock(c, p, held, 'MAIN_HAND');
  eq(ev.cancelled, true, 'pot right-click cancelled');
  eq(prog(c, 'u-hand1').goldPots, 1, 'plain right-click donates exactly 1');
  eq(held.count, 0, 'single held pot consumed');
  eq(c.server.saves.length, 1, 'exactly one progress save');
  eq(c.sandbox.global.ftwwGoldUseGuards['u-hand1'], Number(c.sandbox.global.ftwwDropScanTick) + 1, 'gold use guard = interaction tick + 1');
  eq(c.sandbox.global.ftwwRightClickCooldowns['u-hand1'], Number(c.sandbox.global.ftwwDropScanTick) + 12, 'click cooldown stays 12 ticks');
  const p2 = player(c, { uuid: 'u-shift', username: 'Shift', x: 100.5, y: 64.5, z: 100.5 });
  p2.shift = true;
  const held2 = p2.inventory.add('farmingtales:gold_bucket', 64);
  rcBlock(c, p2, held2, 'MAIN_HAND');
  eq(prog(c, 'u-shift').goldPots, 64, 'shift right-click donates the whole stack');
  eq(held2.count, 0, 'all 64 pots consumed');
  eq(c.sandbox.ftwwLevel(prog(c, 'u-shift')), 2, '64 pots is Lv2');
  eq(p2.messages.some(m => m.indexOf('Lv.2') >= 0), true, 'Lv2 upgrade message shown');
  const p3 = player(c, { uuid: 'u-cd', username: 'Cd', x: 100.5, y: 64.5, z: 100.5 });
  const held3 = p3.inventory.add('farmingtales:gold_bucket', 5);
  rcBlock(c, p3, held3, 'MAIN_HAND');
  eq(prog(c, 'u-cd').goldPots, 1, 'first click donates 1');
  const savesAfterFirst = c.server.saves.length;
  rcBlock(c, p3, held3, 'MAIN_HAND');
  eq(prog(c, 'u-cd').goldPots, 1, 'second click inside cooldown rejected');
  eq(held3.count, 4, 'rejected click consumes nothing');
  eq(c.server.saves.length, savesAfterFirst, 'rejected click writes nothing');
  ticks(c, 12);
  rcBlock(c, p3, held3, 'MAIN_HAND');
  eq(prog(c, 'u-cd').goldPots, 2, 'click accepted again after 12 ticks');
}
section('T07/T08 gold-use guard for both hands and next-tick ordinary coin deposit');
{
  const c = create(); load(c); world(c);
  const p = player(c, { uuid: 'u-guard', username: 'Guard', x: 100.5, y: 64.5, z: 100.5 });
  const pot = p.inventory.add('farmingtales:gold_bucket', 3);
  const ev = rcBlock(c, p, pot, 'OFF_HAND');
  eq(ev.cancelled, true, 'offhand well click cancelled');
  eq(c.sandbox.global.ftwwGoldUseGuards['u-guard'], Number(c.sandbox.global.ftwwDropScanTick) + 1, 'offhand click arms the same guard');
  eq(prog(c, 'u-guard'), null, 'offhand well click does not donate');
  const mainClick = rcItem(c, p, pot, 'MAIN_HAND');
  eq(mainClick.cancelled, true, 'same-tick main-hand coin use blocked');
  const offClick = rcItem(c, p, pot, 'OFF_HAND');
  eq(offClick.cancelled, true, 'same-tick offhand coin use blocked');
  eq(pot.count, 3, 'guarded coin use consumes no pot');
  eq(c.wallet.add, 0, 'guarded coin use adds no SDM money');
  eq(c.logs.info.some(l => l.indexOf('gold pot use blocked by well guard') >= 0), true, 'guard block logged with hand');
  eq(c.sandbox.ftwwGoldUseBlocked(p), true, 'guard reports blocked on the armed tick');
  ticks(c, 1);
  eq(c.sandbox.ftwwGoldUseBlocked(p), false, 'guard expired on the next tick');
  const deposit = rcItem(c, p, pot, 'MAIN_HAND');
  eq(deposit.cancelled, false, 'ordinary deposit not cancelled on next tick');
  eq(pot.count, 2, 'ordinary deposit consumes one pot');
  eq(c.wallet.add, 1, 'ordinary deposit credits SDM once');
  eq(c.wallet.balances.get('u-guard'), 10000, 'one gold pot credits 10000 coins');
  eq(prog(c, 'u-guard'), null, 'ordinary coin deposit writes no well progress');
  eq(c.sandbox.ftwwGoldUseBlocked({}), true, 'unresolvable identity blocks gold pot use');
}
section('T09 emerald right-click roll');
{
  const c = create({ rng: () => 0.99 }); load(c); world(c);
  const p = player(c, { uuid: 'u-em', username: 'Em', x: 100.5, y: 64.5, z: 100.5 });
  const held = p.inventory.add('minecraft:emerald', 64);
  rcBlock(c, p, held, 'MAIN_HAND');
  eq(held.count, 63, 'one emerald consumed from a stack of 64');
  eq(prog(c, 'u-em').emeralds, 1, 'emerald counted once');
  eq(p.given.length, 1, 'exactly one reward delivered');
  eq(c.logs.info.some(l => l.indexOf('outcome=mineral') >= 0), true, 'rng 0.99 lands in the Lv0 mineral bucket');
  eq(p.cooldowns.length, 1, 'emerald item cooldown applied once');
  eq(p.cooldowns[0][1], 12, 'emerald item cooldown is 12');
  eq(c.sandbox.global.ftwwRightClickCooldowns['u-em'], Number(c.sandbox.global.ftwwDropScanTick) + 12, 'local well cooldown 12');
}
section('T10/T11 dropped emerald and dropped pot: owner, nearest player, nobody');
{
  const c = create({ rng: () => 0.99 }); load(c); const lv = world(c);
  const p = player(c, { uuid: 'u-drop', username: 'Drop', x: 100.5, y: 64.5, z: 100.5 });
  const e1 = makeEntity({ id: 'minecraft:emerald', count: 3, x: 101, y: 64, z: 101, level: lv, owner: p });
  spawn(c, e1);
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 1, 'emerald drop queued');
  ticks(c, 2);
  eq(e1.item.count, 2, 'dropped emerald consumes exactly one');
  eq(prog(c, 'u-drop').emeralds, 1, 'dropped emerald counted for its owner');
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 0, 'consumed drop no longer tracked');
  const e2 = makeEntity({ id: 'minecraft:emerald', count: 1, x: 101, y: 64, z: 101, level: lv, owner: null });
  spawn(c, e2); ticks(c, 2);
  eq(prog(c, 'u-drop').emeralds, 2, 'ownerless drop uses the nearest player in range');
  eq(e2.removed, true, 'single emerald entity discarded');
  eq(e2.discarded, 1, 'single emerald entity discarded once');
  c.server.players = []; lv.entities = [];
  const e3 = makeEntity({ id: 'minecraft:emerald', count: 1, x: 101, y: 64, z: 101, level: lv, owner: null });
  spawn(c, e3); ticks(c, 2);
  eq(e3.item.count, 1, 'ownerless drop with nobody nearby is not consumed');
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 1, 'unconsumed drop stays tracked');
  c.sandbox.global.ftwwPendingEmeraldDrops = [];
  lv.entities = [p]; c.server.players = [p];
  const pot = makeEntity({ id: 'farmingtales:gold_bucket', count: 2, x: 101, y: 64, z: 101, level: lv, owner: p });
  spawn(c, pot); ticks(c, 2);
  eq(prog(c, 'u-drop').goldPots, 2, 'dropped pot stack donates to its owner');
  eq(pot.removed, true, 'fully donated pot entity discarded');
  const pot2 = makeEntity({ id: 'farmingtales:gold_bucket', count: 1, x: 101, y: 64, z: 101, level: lv, owner: null });
  c.server.players = []; lv.entities = [];
  spawn(c, pot2); ticks(c, 2);
  eq(prog(c, 'u-drop').goldPots, 2, 'ownerless pot drop is not donated');
  eq(pot2.item.count, 1, 'ownerless pot drop is not consumed');
}
section('T12 private UUID persistence and no shared unknown bucket');
{
  const c = create(); load(c); const lv = world(c);
  const a = player(c, { uuid: 'uuid-a', username: 'A', x: 100.5, y: 64.5, z: 100.5 });
  const b = player(c, { uuid: 'uuid-b', username: 'B', x: 100.5, y: 64.5, z: 100.5 });
  rcBlock(c, a, a.inventory.add('minecraft:emerald', 1), 'MAIN_HAND');
  eq(prog(c, 'uuid-a').emeralds, 1, 'player A record written');
  eq(prog(c, 'uuid-b'), null, 'player B record untouched');
  const bPot = b.inventory.add('farmingtales:gold_bucket', 5);
  b.shift = true;
  rcBlock(c, b, bPot, 'MAIN_HAND');
  eq(prog(c, 'uuid-b').goldPots, 5, 'player B record written separately');
  eq(prog(c, 'uuid-a').emeralds, 1, 'player A emeralds intact');
  eq(prog(c, 'uuid-a').goldPots, 0, 'player A pots untouched');
  const ghost = makePlayer(c.server, { uuid: '', username: 'Ghost', level: lv, x: 100.5, y: 64.5, z: 100.5 });
  let threw = false;
  try { c.sandbox.ftwwPlayerKey(ghost); } catch (e) { threw = true; }
  eq(threw, true, 'uuid-less player key throws instead of a shared fallback');
  const ghostHeld = ghost.inventory.add('minecraft:emerald', 1);
  rcBlock(c, ghost, ghostHeld, 'MAIN_HAND');
  eq(ghostHeld.count, 1, 'uuid-less player consumes nothing');
  eq(c.raw.has(K(c, 'FTWW_PROGRESS_PREFIX') + 'unknown'), false, 'no shared unknown progress key exists');
  eq(c.saves.length, 2, 'no write attempted for the uuid-less player');
  eq(ghost.messages.some(m => m.indexOf('无法识别玩家身份') >= 0), true, 'uuid-less player gets a clear error and no payment');
  eq(c.logs.error.some(l => l.indexOf('interaction rejected identity') >= 0), true, 'uuid-less rejection logged');
}
section('T13 malformed records: reject before payment, never overwrite');
{
  const bad = ['{not json', '5', '[1,2]', 'null', 'true', q('~text~'), q('{~emeralds~:~3~,~goldPots~:0}'), q('{~emeralds~:-1,~goldPots~:0}'), q('{~emeralds~:1.5,~goldPots~:0}'), q('{~emeralds~:9007199254740992,~goldPots~:0}'), q('{~emeralds~:null,~goldPots~:0}'), q('{~emeralds~:true,~goldPots~:0}'), q('{~emeralds~:1,~goldPots~:-2}')];
  bad.forEach(text => {
    const c = create(); load(c); world(c);
    const p = player(c, { uuid: 'u-bad', username: 'Bad', x: 100.5, y: 64.5, z: 100.5 });
    putRaw(c, 'u-bad', text);
    const held = p.inventory.add('minecraft:emerald', 2);
    rcBlock(c, p, held, 'MAIN_HAND');
    eq(held.count, 2, 'no emerald debited for ' + text);
    eq(c.server.persistentData.getString(key(c, 'u-bad')), text, 'stored record untouched for ' + text);
    eq(c.saves.length, 0, 'no save for ' + text);
    eq(p.messages.some(m => m.indexOf('数据异常') >= 0), true, 'clear player error for ' + text);
    let threw = false;
    try { c.sandbox.ftwwProgress(c.server, p); } catch (e) { threw = true; }
    eq(threw, true, 'ftwwProgress rejects ' + text);
    eq(c.sandbox.ftwwSafeLevel(c.server, p), 0, 'ftwwSafeLevel falls back to 0 for ' + text);
    ticks(c, 12);
    const pot = p.inventory.add('farmingtales:gold_bucket', 2);
    p.shift = true;
    rcBlock(c, p, pot, 'MAIN_HAND');
    eq(pot.count, 2, 'no pot debited for ' + text);
    eq(c.server.persistentData.getString(key(c, 'u-bad')), text, 'pot path also leaves record untouched for ' + text);
  });
  const c2 = create(); load(c2);
  const p2 = makePlayer(c2.server, { uuid: 'u-ok' });
  eq(c2.sandbox.ftwwProgress(c2.server, p2).goldPots, 0, 'missing record defaults to zero');
  putRaw(c2, 'u-ok', '{}');
  eq(c2.sandbox.ftwwProgress(c2.server, p2).emeralds, 0, 'empty object defaults to zero');
  putRaw(c2, 'u-ok', q('{~emeralds~:3}'));
  eq(c2.sandbox.ftwwProgress(c2.server, p2).goldPots, 0, 'absent field defaults to zero');
}
section('T13b counter overflow blocks payment before any debit');
{
  const c = create(); load(c); world(c);
  const p = player(c, { uuid: 'u-max', username: 'Max', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-max', JSON.stringify({ emeralds: MAXSAFE, goldPots: 0 }));
  const held = p.inventory.add('minecraft:emerald', 1);
  rcBlock(c, p, held, 'MAIN_HAND');
  eq(held.count, 1, 'maxed emerald counter does not debit');
  eq(c.saves.length, 0, 'maxed emerald counter does not save');
  eq(p.messages.some(m => m.indexOf('上限') >= 0), true, 'emerald overflow message shown');
  const c2 = create(); load(c2); world(c2);
  const p2 = player(c2, { uuid: 'u-max2', username: 'Max2', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c2, 'u-max2', JSON.stringify({ emeralds: 0, goldPots: MAXSAFE - 1 }));
  const pot = p2.inventory.add('farmingtales:gold_bucket', 2);
  p2.shift = true;
  rcBlock(c2, p2, pot, 'MAIN_HAND');
  eq(pot.count, 2, 'pot overflow does not debit');
  eq(c2.saves.length, 0, 'pot overflow does not save');
  eq(p2.messages.some(m => m.indexOf('上限') >= 0), true, 'pot overflow message shown');
}
section('T14 save failure rollback');
{
  const c = create(); load(c); const lv = world(c);
  const p = player(c, { uuid: 'u-save', username: 'Save', x: 100.5, y: 64.5, z: 100.5 });
  c.server.failSave = true;
  const held = p.inventory.add('minecraft:emerald', 5);
  rcBlock(c, p, held, 'MAIN_HAND');
  eq(held.count, 5, 'emerald restored after save failure');
  eq(prog(c, 'u-save'), null, 'no record after save failure');
  eq(p.messages.some(m => m.indexOf('退回') >= 0), true, 'rollback message shown to the player');
  eq(c.logs.error.some(l => l.indexOf('payment rolled back') >= 0), true, 'payment rollback logged');
  ticks(c, 12);
  const pot = p.inventory.add('farmingtales:gold_bucket', 4);
  p.shift = true;
  rcBlock(c, p, pot, 'MAIN_HAND');
  eq(pot.count, 4, 'held pot restored after save failure');
  eq(c.logs.error.some(l => l.indexOf('Donation rolled back') >= 0), true, 'donation rollback logged');
  const e = makeEntity({ id: 'farmingtales:gold_bucket', count: 3, x: 101, y: 64, z: 101, level: lv, owner: p });
  spawn(c, e); ticks(c, 2);
  eq(e.item.count, 3, 'dropped pot stack restored after save failure');
  eq(e.removed, false, 'dropped pot entity kept after save failure');
  c.server.failSave = false;
}
section('T15 Lv100 GUI failure after a committed donation must not re-consume');
{
  const c = create(); load(c); const lv = world(c);
  const p = player(c, { uuid: 'u-gui', username: 'Gui', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-gui', JSON.stringify({ emeralds: 0, goldPots: 99 }));
  p.throwOnGui = true;
  const held = p.inventory.add('farmingtales:gold_bucket', 1);
  const ev = rcBlock(c, p, held, 'MAIN_HAND');
  eq(ev.cancelled, true, 'level-3 donation click cancelled');
  eq(held.count, 0, 'pot consumed exactly once');
  eq(prog(c, 'u-gui').goldPots, 100, 'donation committed at 100 pots');
  eq(c.sandbox.ftwwLevel(prog(c, 'u-gui')), 3, 'level is 3 after 100 pots');
  eq(c.server.saves.length, 1, 'exactly one commit save');
  eq(p.messages.some(m => m.indexOf('升至 Lv.3') >= 0), true, 'level-up message shown');
  eq(p.messages.some(m => m.indexOf('商店界面打开失败') >= 0), true, 'GUI failure reported to player');
  eq(c.logs.error.some(l => l.indexOf('shop open failed after commit') >= 0), true, 'GUI failure logged');
  ticks(c, 6);
  eq(prog(c, 'u-gui').goldPots, 100, 'progress unchanged across later ticks');
  eq(c.server.saves.length, 1, 'no re-consumption or re-save after GUI failure');
  const p2 = player(c, { uuid: 'u-gui2', username: 'Gui2', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-gui2', JSON.stringify({ emeralds: 0, goldPots: 99 }));
  p2.throwOnGui = true;
  const held2 = p2.inventory.add('farmingtales:gold_bucket', 1);
  const committed = c.sandbox.ftwwDonatePots({ server: c.server, level: lv, block: c.core, player: p2 }, held2, 1, null);
  eq(committed, true, 'ftwwDonatePots returns true despite GUI failure');
  eq(held2.count, 0, 'direct donation consumes the pot once');
  eq(prog(c, 'u-gui2').goldPots, 100, 'direct donation committed once');
  const p3 = player(c, { uuid: 'u-gui3', username: 'Gui3', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-gui3', JSON.stringify({ emeralds: 0, goldPots: 99 }));
  p3.throwOnGui = true;
  const e = makeEntity({ id: 'farmingtales:gold_bucket', count: 1, x: 101, y: 64, z: 101, level: lv, owner: p3 });
  spawn(c, e); ticks(c, 2);
  eq(prog(c, 'u-gui3').goldPots, 100, 'dropped pot committed at Lv3');
  eq(e.discarded, 1, 'dropped pot discarded exactly once');
  ticks(c, 4);
  eq(prog(c, 'u-gui3').goldPots, 100, 'dropped pot not re-donated across ticks');
  eq(e.discarded, 1, 'no second discard');
}
function shopSetup(o) {
  const c = create(o); load(c); const lv = world(c);
  const p = player(c, { uuid: 'u-shop', username: 'Shop', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-shop', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  c.p = p; c.lv = lv; c.block = coreBlock(c, p);
  return c;
}
function buy(c, kind, itemId) { return c.sandbox.ftwwPurchase({ player: c.p, level: c.lv, block: c.block, server: c.server }, kind, itemId); }
section('T16 shop: summon 1 pot no SDM, 50m weapon, 5m spell, real delivery, balances, cooldown');
{
  const c = shopSetup(); const p = c.p;
  const pot = p.inventory.add('farmingtales:gold_bucket', 4);
  const summon = K(c, 'FTWW_SUMMONS')[0];
  eq(countMain(c, summon), 0, 'summon not owned before the purchase');
  eq(buy(c, 'summon', summon), true, 'summon purchase succeeds at Lv3');
  eq(countMain(c, summon), 1, 'summon really lands in the main backpack');
  eq(pot.count, 3, 'summon costs exactly one gold pot');
  eq(p.syncs, 1, 'summon purchase syncs the real inventory once');
  eq(p.given.length, 0, 'summon delivery never falls back to give()');
  eq(c.wallet.get + c.wallet.set + c.wallet.add, 0, 'summon never touches the SDM wallet');
  eq(buy(c, 'summon', K(c, 'FTWW_SUMMONS')[1]), false, 'immediate second buy blocked by shop cooldown');
  eq(pot.count, 3, 'cooldown-blocked buy consumes nothing');
  ticks(c, 10);
  eq(buy(c, 'summon', 'ftboss:not_listed_as_summon'), false, 'unlisted summon rejected');
  eq(pot.count, 3, 'rejected summon consumes nothing');
  ticks(c, 10);
  c.wallet.balances.set('u-shop', K(c, 'FTWW_WEAPON_PRICE') - 1);
  eq(buy(c, 'weapon'), false, 'weapon purchase fails when balance is short');
  eq(c.wallet.set, 0, 'no debit on short balance');
  eq(p.messages.some(m => m.indexOf('余额不足') >= 0), true, 'insufficient balance message');
  ticks(c, 10);
  c.wallet.balances.set('u-shop', K(c, 'FTWW_WEAPON_PRICE'));
  eq(buy(c, 'weapon'), true, 'weapon purchase succeeds at exactly 50m');
  eq(c.wallet.balances.get('u-shop'), 0, 'exact weapon balance leaves zero');
  eq(c.wallet.set, 1, 'weapon debited exactly once');
  const weapons = [];
  for (let slot = 0; slot < 36; slot++) { const s = p.inventory.getStackInSlot(slot); if (s.count > 0 && s.id !== summon && s.id !== 'farmingtales:gold_bucket' && s.id !== 'irons_spellbooks:scroll') weapons.push(s); }
  eq(weapons.length, 1, 'one weapon delivered into the main backpack');
  eq(weapons[0].id === 'ftboss:gourmet_blank_blade', false, 'delivered weapon is not the excluded blade');
  let catalogNbt = null;
  for (let attempt = 0; attempt < 400; attempt++) {
    const sample = c.sandbox.ftwwRandomWeapon();
    if (sample.id === weapons[0].id) { catalogNbt = nbtText(sample.nbt); break; }
  }
  eq(catalogNbt !== null, true, 'delivered weapon id exists in the manor catalog');
  eq(nbtText(weapons[0].nbt), catalogNbt, 'delivered weapon keeps the catalog NBT after insertion');
  ticks(c, 10);
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  eq(buy(c, 'spell'), true, 'spell purchase succeeds at exactly 5m');
  eq(c.wallet.balances.get('u-shop'), 0, 'exact spell balance leaves zero');
  eq(p.given.length, 0, 'scroll delivery never falls back to give()');
  eq(countMain(c, 'irons_spellbooks:scroll'), 1, 'scroll really lands in the main backpack');
  let scroll = null;
  for (let slot = 0; slot < 36; slot++) { const s = p.inventory.getStackInSlot(slot); if (s.id === 'irons_spellbooks:scroll' && s.count > 0) scroll = s; }
  eq(!!scroll && !!scroll.scroll, true, 'delivered scroll carries a written container');
  eq(nbtText(scroll.nbt).indexOf('spellContainer') >= 0, true, 'delivered scroll keeps its spell NBT after insertion');
  const allowed = { 'irons_spellbooks:fireball': [1, 5], 'irons_spellbooks:ice_block': [2, 3] };
  eq(!!allowed[scroll.scroll.spellId], true, 'scroll spell is enabled and lootable');
  eq(scroll.scroll.level >= allowed[scroll.scroll.spellId][0] && scroll.scroll.level <= allowed[scroll.scroll.spellId][1], true, 'scroll level is legal');
}
section('T16b full bag rejected without debit and real insertion failure rolled back');
{
  const c = shopSetup(); c.p.inventory.mode = 'full';
  c.wallet.balances.set('u-shop', K(c, 'FTWW_WEAPON_PRICE'));
  eq(buy(c, 'weapon'), false, 'full bag rejects the purchase');
  eq(c.wallet.set, 0, 'full bag does not debit');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_WEAPON_PRICE'), 'balance untouched on full bag');
  eq(c.p.messages.some(m => m.indexOf('背包空间不足') >= 0), true, 'full bag message shown');
  eq(c.p.syncs, 0, 'rejected full bag purchase never syncs');
  const c2 = shopSetup(); c2.p.inventory.mode = 'realfail';
  c2.wallet.balances.set('u-shop', K(c2, 'FTWW_WEAPON_PRICE'));
  eq(buy(c2, 'weapon'), false, 'real insertion failure returns false');
  eq(c2.wallet.set, 2, 'wallet debited then restored');
  eq(c2.wallet.balances.get('u-shop'), K(c2, 'FTWW_WEAPON_PRICE'), 'wallet rolled back after insertion failure');
  eq(c2.logs.error.some(l => l.indexOf('Purchase failed') >= 0), true, 'insertion failure logged');
  eq(c2.p.messages.some(m => m.indexOf('购买失败') >= 0), true, 'purchase failure message shown');
}
section('T16c scroll container validation failures block the debit');
{
  ['wrongspell', 'wronglevel', 'missing', 'empty'].forEach(mode => {
    const c = shopSetup({ containerMode: mode });
    c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
    eq(buy(c, 'spell'), false, 'spell purchase rejected for container mode ' + mode);
    eq(c.wallet.set, 0, 'no debit for container mode ' + mode);
  });
  const c = shopSetup({ containerMode: 'wrongspell' });
  let threw = false;
  try { c.sandbox.ftwwRandomSpell(); } catch (e) { threw = true; }
  eq(threw, true, 'ftwwRandomSpell validates the written scroll contents');
}
section('T16d shop availability: core block, distance, dimension, level, GUI with real player slots');
{
  const c = shopSetup();
  const pot = c.p.inventory.add('farmingtales:gold_bucket', 2);
  c.lv.overrides['0,0,0'] = 'minecraft:stone';
  eq(buy(c, 'summon', K(c, 'FTWW_SUMMONS')[0]), false, 'wrong core block rejected');
  eq(c.p.messages.some(m => m.indexOf('靠近结构完整的许愿井') >= 0), true, 'availability message shown');
  c.lv.overrides = {};
  c.p.x = c.core.x + 20;
  eq(buy(c, 'summon', K(c, 'FTWW_SUMMONS')[0]), false, 'distance beyond 8 blocks rejected');
  c.p.x = c.core.x + 0.5;
  const keep = c.p.level;
  c.p.level = { dimension: 'minecraft:the_nether' };
  eq(buy(c, 'summon', K(c, 'FTWW_SUMMONS')[0]), false, 'other dimension rejected');
  c.p.level = keep;
  putRaw(c, 'u-shop', JSON.stringify({ emeralds: 0, goldPots: 99 }));
  eq(buy(c, 'summon', K(c, 'FTWW_SUMMONS')[0]), false, 'below 100 pots rejected');
  eq(pot.count, 2, 'no pot spent on rejected availability checks');
  putRaw(c, 'u-shop', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  const empty = new Stack('minecraft:air', 0);
  rcBlock(c, c.p, empty, 'MAIN_HAND');
  eq(c.p.guiOpens, 1, 'empty-hand click at Lv3 opens the shop GUI');
  eq(c.p.gui.buttons.length, 14, 'GUI has 11 summon buttons plus 3 specials');
  eq(c.p.gui.playerSlots, true, 'GUI displays the captured inventory copy in the player slot area');
  eq(String(c.p.gui.title).indexOf('Lv.3') >= 0, true, 'GUI title names Lv.3');
  const summonButton = c.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('1 金币锅') >= 0)[0];
  eq(!!summonButton, true, 'summon button present');
  c.p.gui.close();
  summonButton.click({ setHandled: () => {} });
  eq(pot.count, 2, 'click after GUI close does nothing');
  eq(countMain(c, K(c, 'FTWW_SUMMONS')[0]), 0, 'closed GUI click delivers nothing');
  ticks(c, 10);
  rcBlock(c, c.p, empty, 'MAIN_HAND');
  const reopen = c.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('1 金币锅') >= 0)[0];
  reopen.click({ setHandled: () => {} });
  eq(K(c, 'global.ftwwPurchaseQueue').length, 1, 'open-GUI click queues exactly one purchase instead of paying inside the captured menu');
  eq(c.p.closed, 2, 'the click closes the shop container a second time immediately');
  eq(c.p.openInventory, c.server.inventoryMenu, 'the queued click leaves the vanilla inventory menu open');
  eq(countMain(c, K(c, 'FTWW_SUMMONS')[0]), 0, 'queued click delivers nothing while the native restore has not run yet');
  ticks(c, 10);
  eq(K(c, 'global.ftwwPurchaseQueue').length, 0, 'queued purchase runs on a later server tick and leaves the queue empty');
  eq(countMain(c, K(c, 'FTWW_SUMMONS')[0]), 1, 'queued summon really lands in the restored main backpack');
  eq(countAll(c, 'farmingtales:gold_bucket'), 1, 'only the untouched pot remains in the restored real backpack after one queued summon');
}
section('T24 real capture lifecycle and root-cause regression');
{
  const empty = new Stack('minecraft:air', 0);
  const broken = create({ nativeInventoryLifecycle: true });
  const liveWell = fs.readFileSync(FILE.wellOrig, 'utf8');
  vm.runInContext(liveWell, broken.vmCtx, { filename: 'wishing_well_live_backup.js' });
  const blv = world(broken);
  const bp = player(broken, { uuid: 'u-root', username: 'Root', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(broken, 'u-root', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  broken.p = bp; broken.lv = blv; broken.block = coreBlock(broken, bp);
  bp.inventory.add('minecraft:stone', 64);
  broken.wallet.balances.set('u-root', K(broken, 'FTWW_SPELL_PRICE'));
  eq(broken.wallet.balances.get('u-root'), K(broken, 'FTWW_SPELL_PRICE'), 'live backup test starts with exactly 5m in the SDM wallet');
  eq(bp.inventory.getStackInSlot(0).id, 'minecraft:stone', 'live backup player owns 64 stone before opening the shop');
  openShopGui(broken, empty);
  eq(bp.openInventory instanceof CustomChestMenu, true, 'opening the shop leaves a CustomChestMenu as the open menu');
  eq(countAll(broken, 'minecraft:stone'), 0, 'captureInventory(true) empties the real 36 main slots');
  eq(broken.server.restoreInventories.get('u-root').size, 1, 'native restore map keeps only the non-empty original slot');
  const spellButton = bp.gui.buttons.filter(b => String(b.text.getString()).indexOf('500 万') >= 0)[0];
  eq(!!spellButton, true, 'live backup shop exposes the spell button');
  spellButton.click({ setHandled: () => {} });
  eq(broken.wallet.balances.get('u-root'), 0, 'live backup debits 5m synchronously inside the captured menu');
  eq(bp.messages.some(m => m.indexOf('购买成功') >= 0), true, 'live backup reports 购买成功 while the real inventory is still captured');
  eq(countMain(broken, 'irons_spellbooks:scroll'), 1, 'live backup reward exists only inside the captured copy at that instant');
  bp.gui.close();
  ticks(broken, 1);
  eq(bp.openInventory, broken.server.inventoryMenu, 'closing the shop switches back to the vanilla inventory menu');
  eq(countMain(broken, 'irons_spellbooks:scroll'), 0, 'ROOT CAUSE: native restore overwrites the paid scroll away on the next tick');
  eq(countAll(broken, 'minecraft:stone'), 64, 'native restore brings the survived original stack back after the loss');
  eq(broken.wallet.balances.get('u-root'), 0, 'ROOT CAUSE: the 5m debit is kept while the reward is gone');

  const fresh = create({ nativeInventoryLifecycle: true }); load(fresh);
  const flv = world(fresh);
  const fp = player(fresh, { uuid: 'u-fix', username: 'Fix', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(fresh, 'u-fix', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  fresh.p = fp; fresh.lv = flv; fresh.block = coreBlock(fresh, fp);
  fp.inventory.add('minecraft:stone', 64);
  fresh.wallet.balances.set('u-fix', K(fresh, 'FTWW_SPELL_PRICE'));
  openShopGui(fresh, empty);
  eq(countAll(fresh, 'minecraft:stone'), 0, 'candidate shop open still captures and empties the real slots');
  fp.gui.buttons.filter(b => String(b.text.getString()).indexOf('500 万') >= 0)[0].click({ setHandled: () => {} });
  eq(fp.openInventory, fresh.server.inventoryMenu, 'candidate click closes the container before any money moves');
  eq(fresh.wallet.balances.get('u-fix'), K(fresh, 'FTWW_SPELL_PRICE'), 'candidate does not debit on the click tick');
  eq(countMain(fresh, 'irons_spellbooks:scroll'), 0, 'candidate has no reward while the native restore has not run yet');
  ticks(fresh, 1);
  eq(fresh.server.restoreCounts.get('u-fix'), 1, 'native 36-slot restore ran before the ServerEvents tick handler');
  eq(fresh.wallet.balances.get('u-fix'), 0, 'candidate debits exactly 5m once after the restore');
  eq(fresh.wallet.set, 1, 'candidate SDM debit happens exactly once');
  eq(countMain(fresh, 'irons_spellbooks:scroll'), 1, 'candidate scroll lands in the restored real backpack');
  eq(countAll(fresh, 'minecraft:stone'), 64, 'candidate keeps the pre-existing stone stack after delivery');
  eq(fp.guiOpens, 1, 'candidate never reopens the shop GUI after a purchase');
}
section('T25 candidate three products deliver after close with NBT intact');
{
  const empty = new Stack('minecraft:air', 0);
  const c = create({ nativeInventoryLifecycle: true }); load(c);
  const summon = K(c, 'FTWW_SUMMONS')[2];
  const c2 = create({ nativeInventoryLifecycle: true }); load(c2);
  const c3 = create({ nativeInventoryLifecycle: true }); load(c3);
  const c4 = create({ nativeInventoryLifecycle: true }); load(c4);
  function ready(ctx, uuid) {
    const lv = world(ctx);
    const p = player(ctx, { uuid: uuid, username: uuid, x: 100.5, y: 64.5, z: 100.5 });
    putRaw(ctx, uuid, JSON.stringify({ emeralds: 0, goldPots: 100 }));
    ctx.p = p; ctx.lv = lv; ctx.block = coreBlock(ctx, p);
    return p;
  }
  const p1 = ready(c, 'u-summon');
  p1.inventory.add('farmingtales:gold_bucket', 4);
  openShopGui(c, empty);
  c.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('1 金币锅') >= 0)[2].click({ setHandled: () => {} });
  ticks(c, 1);
  eq(countAll(c, summon), 1, 'queued summon reaches the restored real backpack');
  eq(countAll(c, 'farmingtales:gold_bucket'), 3, 'queued summon consumed exactly one restored gold pot');
  eq(c.wallet.set + c.wallet.get + c.wallet.add, 0, 'queued summon never touches the SDM wallet');

  const p2 = ready(c2, 'u-weapon');
  p2.inventory.add('minecraft:stone', 64);
  c2.wallet.balances.set('u-weapon', K(c2, 'FTWW_WEAPON_PRICE'));
  openShopGui(c2, empty);
  c2.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('5000 万') >= 0)[0].click({ setHandled: () => {} });
  ticks(c2, 1);
  eq(c2.wallet.balances.get('u-weapon'), 0, 'queued weapon debits exactly the 50m price once');
  eq(c2.wallet.set, 1, 'queued weapon writes the SDM balance exactly once');
  eq(countAll(c2, 'minecraft:stone'), 64, 'weapon delivery preserves the restored original stack');
  const weaponSlots = [];
  for (let slot = 0; slot < 36; slot++) { const s = p2.inventory.getStackInSlot(slot); if (s.count > 0 && s.id !== 'minecraft:stone') weaponSlots.push(s); }
  eq(weaponSlots.length, 1, 'exactly one weapon lands in the restored backpack');
  eq(weaponSlots[0].id === 'ftboss:gourmet_blank_blade', false, 'queued weapon is not the excluded blade');
  let catalogNbt = null;
  for (let attempt = 0; attempt < 400; attempt++) {
    const sample = c2.sandbox.ftwwRandomWeapon();
    if (sample.id === weaponSlots[0].id) { catalogNbt = nbtText(sample.nbt); break; }
  }
  eq(catalogNbt !== null, true, 'queued weapon id exists in the manor catalog');
  eq(nbtText(weaponSlots[0].nbt), catalogNbt, 'queued weapon keeps the catalog NBT through the queue');
  ticks(c2, 10);
  eq(K(c2, 'global.ftwwShopCooldowns')['u-weapon'] > 0, true, 'queued purchase armed the per-player shop cooldown');

  const p3 = ready(c3, 'u-spell');
  p3.inventory.add('minecraft:stone', 64);
  c3.wallet.balances.set('u-spell', K(c3, 'FTWW_SPELL_PRICE'));
  openShopGui(c3, empty);
  c3.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('500 万') >= 0)[0].click({ setHandled: () => {} });
  ticks(c3, 1);
  eq(countAll(c3, 'minecraft:stone'), 64, 'spell delivery preserves the restored original stack');
  eq(c3.wallet.balances.get('u-spell'), 0, 'queued spell debits exactly 5m once');
  eq(c3.wallet.set, 1, 'queued spell writes the SDM balance exactly once');
  eq(p3.given.length, 0, 'queued spell never falls back to give()');
  eq(p3.guiOpens, 1, 'queued spell delivery never reopens the GUI');
  let scroll = null;
  for (let slot = 0; slot < 36; slot++) { const s = p3.inventory.getStackInSlot(slot); if (s.id === 'irons_spellbooks:scroll' && s.count > 0) scroll = s; }
  eq(!!scroll, true, 'queued spell scroll lands in the restored backpack');
  eq(!!scroll && !!scroll.scroll, true, 'queued scroll carries a written container');
  eq(nbtText(scroll.nbt).indexOf('spellContainer') >= 0, true, 'queued scroll keeps its spell NBT through the queue');
  const allowed = { 'irons_spellbooks:fireball': [1, 5], 'irons_spellbooks:ice_block': [2, 3] };
  eq(!!allowed[scroll.scroll.spellId], true, 'queued scroll spell is enabled and lootable');
  eq(scroll.scroll.level >= allowed[scroll.scroll.spellId][0] && scroll.scroll.level <= allowed[scroll.scroll.spellId][1], true, 'queued scroll level is legal');

  const p4 = ready(c4, 'u-dup');
  p4.inventory.add('minecraft:stone', 64);
  c4.wallet.balances.set('u-dup', K(c4, 'FTWW_SPELL_PRICE') * 2);
  openShopGui(c4, empty);
  const dup = c4.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('500 万') >= 0)[0];
  dup.click({ setHandled: () => {} });
  dup.click({ setHandled: () => {} });
  eq(K(c4, 'global.ftwwPurchaseQueue').length, 1, 'double click queues only one purchase');
  eq(p4.openInventory, c4.server.inventoryMenu, 'double click closes the container once');
  c4.p.openInventory = c4.server.inventoryMenu;
  dup.click({ setHandled: () => {} });
  eq(K(c4, 'global.ftwwPurchaseQueue').length, 1, 'a further click cannot add a second queue entry');
  ticks(c4, 1);
  eq(countMain(c4, 'irons_spellbooks:scroll'), 1, 'duplicate click delivers exactly one scroll');
  eq(c4.wallet.set, 1, 'duplicate click debits the SDM wallet exactly once');
  eq(c4.wallet.balances.get('u-dup'), K(c4, 'FTWW_SPELL_PRICE'), 'duplicate click leaves exactly one price deducted');
  ticks(c4, 10);
  eq(countMain(c4, 'irons_spellbooks:scroll'), 1, 'no second delivery appears on later ticks');
  eq(c4.wallet.set, 1, 'no later tick debits again');
}
function shopLifecycleSetup(opts) {
  const c = create(Object.assign({ nativeInventoryLifecycle: true }, opts || {}));
  load(c);
  const lv = world(c);
  const p = player(c, { uuid: 'u-life', username: 'Life', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-life', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  c.p = p; c.lv = lv; c.block = coreBlock(c, p);
  return c;
}
function holdPlayerMenu(c) {
  c.p.openInventory = new CustomChestMenu(c.p.guiOpens + 200, c.p.guiCapture);
}
function restoreOrderProbe(c, uuid) {
  const original = c.handlers.tick;
  const probe = { restored: 0, tick: 0 };
  c.handlers.tick = function (event) {
    probe.restored = c.server.restoreCounts.get(uuid) || 0;
    probe.tick++;
    return original.call(this, event);
  };
  return probe;
}
function spellButtonOf(c) {
  return c.p.gui.buttons.filter(b => String(b.text.getString()).indexOf('500 万') >= 0)[0];
}
section('T26 native lifecycle rejections, queue ordering and per-player isolation');
{
  const empty = new Stack('minecraft:air', 0);
  const cA = shopLifecycleSetup();
  fillMain(cA, 'minecraft:stone', 64);
  cA.p.inventory.setStackInSlot(0, new Stack('farmingtales:gold_bucket', 4));
  cA.wallet.balances.set('u-life', K(cA, 'FTWW_SPELL_PRICE'));
  openShopGui(cA, empty);
  eq(cA.server.capturedInventories.get('u-life').size, 36, 'captured copy holds all 36 occupied main slots');
  eq(countAll(cA, 'minecraft:stone') + countAll(cA, 'farmingtales:gold_bucket'), 0, 'the real 36 slots are empty while the shop is open');
  spellButtonOf(cA).click({ setHandled: () => {} });
  ticks(cA, 1);
  eq(cA.server.restoreCounts.get('u-life'), 1, 'native restore refilled all 36 occupied slots before the queue ran');
  eq(cA.wallet.set, 0, 'a full restored backpack is rejected before any SDM debit');
  eq(cA.wallet.balances.get('u-life'), K(cA, 'FTWW_SPELL_PRICE'), 'full restored backpack keeps the whole balance');
  eq(countMain(cA, 'irons_spellbooks:scroll'), 0, 'full restored backpack receives no scroll');
  eq(K(cA, 'global.ftwwPurchaseQueue').length, 0, 'rejected full-backpack order leaves no stale queue entry');
  eq(countAll(cA, 'minecraft:stone'), 35 * 64, 'every occupied stone slot is restored untouched');

  const cB = shopLifecycleSetup();
  cB.p.inventory.add('minecraft:stone', 64);
  cB.wallet.balances.set('u-life', K(cB, 'FTWW_SPELL_PRICE'));
  openShopGui(cB, empty);
  cB.p.x = cB.core.x + 20;
  spellButtonOf(cB).click({ setHandled: () => {} });
  eq(K(cB, 'global.ftwwPurchaseQueue').length, 1, 'walk-away order is queued while the shop is still open');
  ticks(cB, 1);
  eq(cB.server.restoreCounts.get('u-life'), 1, 'walk-away case still restores the real 36 slots first');
  eq(cB.wallet.set, 0, 'walking beyond 8 blocks before settlement never debits');
  eq(cB.wallet.balances.get('u-life'), K(cB, 'FTWW_SPELL_PRICE'), 'walk-away keeps the whole balance');
  eq(K(cB, 'global.ftwwPurchaseQueue').length, 0, 'walk-away order is dropped after settlement fails');
  eq(countMain(cB, 'irons_spellbooks:scroll'), 0, 'walk-away delivers no scroll');

  const cC = shopLifecycleSetup();
  cC.p.inventory.add('minecraft:stone', 64);
  cC.wallet.balances.set('u-life', K(cC, 'FTWW_SPELL_PRICE'));
  openShopGui(cC, empty);
  spellButtonOf(cC).click({ setHandled: () => {} });
  cC.lv.overrides['0,0,0'] = 'minecraft:stone';
  ticks(cC, 2);
  eq(cC.server.restoreCounts.get('u-life'), 1, 'broken structure still restores the real 36 slots first');
  eq(cC.wallet.set, 0, 'a broken core structure before settlement never debits');
  eq(cC.wallet.balances.get('u-life'), K(cC, 'FTWW_SPELL_PRICE'), 'broken structure keeps the whole balance');
  eq(K(cC, 'global.ftwwPurchaseQueue').length, 0, 'broken-structure order is dropped');
  eq(countAll(cC, 'minecraft:stone'), 64, 'broken-structure order leaves the restored stack untouched');

  const cD = shopLifecycleSetup();
  cD.p.inventory.add('minecraft:stone', 64);
  cD.wallet.balances.set('u-life', K(cD, 'FTWW_SPELL_PRICE'));
  openShopGui(cD, empty);
  cD.p.throwOnClose = true;
  spellButtonOf(cD).click({ setHandled: () => {} });
  eq(K(cD, 'global.ftwwPurchaseQueue').length, 1, 'a refused close keeps the order queued while the menu stays open');
  ticks(cD, 1);
  eq(cD.p.openInventory instanceof CustomChestMenu, true, 'the refused close left the same CustomChestMenu as the open menu');
  eq(cD.server.restoreInventories.has('u-life'), true, 'the held CustomChestMenu blocks the native restore');
  eq(cD.wallet.set, 0, 'no debit while the native restore is still blocked');
  ticks(cD, 41);
  eq(K(cD, 'global.ftwwPurchaseQueue').length, 0, 'waiting past the tick budget cancels the order');
  eq(cD.wallet.set, 0, 'a cancelled waiting order never debits');
  eq(countMain(cD, 'irons_spellbooks:scroll'), 0, 'a cancelled waiting order delivers nothing');
  eq(cD.p.messages.some(m => m.indexOf('购买已取消') >= 0), true, 'the cancelled order tells the player it expired');
  cD.p.throwOnClose = false;
  cD.p.closeContainer();
  ticks(cD, 1);
  eq(cD.server.restoreCounts.get('u-life'), 1, 'the blocked restore runs once the held menu really closes');
  eq(countAll(cD, 'minecraft:stone'), 64, 'the original stack survives the blocked-restore wait');
  eq(cD.wallet.balances.get('u-life'), K(cD, 'FTWW_SPELL_PRICE'), 'the blocked-restore wait costs nothing');

  const cD2 = shopLifecycleSetup();
  cD2.p.inventory.add('minecraft:stone', 64);
  cD2.wallet.balances.set('u-life', K(cD2, 'FTWW_SPELL_PRICE'));
  openShopGui(cD2, empty);
  cD2.p.throwOnClose = true;
  spellButtonOf(cD2).click({ setHandled: () => {} });
  eq(K(cD2, 'global.ftwwPurchaseQueue').length, 1, 'the manual-close probe queues its order after the refused close');
  eq(cD2.p.openInventory instanceof CustomChestMenu, true, 'the refused close keeps the custom menu open for the manual close');
  cD2.p.throwOnClose = false;
  cD2.p.closeContainer();
  ticks(cD2, 1);
  eq(cD2.server.restoreCounts.get('u-life'), 1, 'closing the held menu by hand restores the real 36 slots');
  eq(cD2.wallet.set, 1, 'the manual close settles the pending order with exactly one debit');
  eq(cD2.wallet.balances.get('u-life'), 0, 'the manual close costs exactly the spell price');
  eq(countMain(cD2, 'irons_spellbooks:scroll'), 1, 'the manual close delivers exactly one scroll');
  eq(countAll(cD2, 'minecraft:stone'), 64, 'the manual close keeps the restored original stack');

  const cE = shopLifecycleSetup();
  cE.p.inventory.add('minecraft:stone', 64);
  cE.wallet.balances.set('u-life', K(cE, 'FTWW_SPELL_PRICE'));
  openShopGui(cE, empty);
  eq(K(cE, 'ftwwPurchase')({ player: cE.p, level: cE.lv, block: cE.block, server: cE.server }, 'spell'), false, 'a direct purchase while the shop menu is open is refused');
  eq(cE.p.messages.some(m => m.indexOf('请先关闭商店界面') >= 0), true, 'the refused purchase explains the menu must be closed');
  eq(cE.wallet.set, 0, 'the refused purchase never debits');
  eq(cE.wallet.balances.get('u-life'), K(cE, 'FTWW_SPELL_PRICE'), 'the refused purchase keeps the whole balance');
  eq(countMain(cE, 'irons_spellbooks:scroll'), 0, 'the refused purchase delivers nothing');
  holdPlayerMenu(cE);
  eq(K(cE, 'ftwwPurchase')({ player: cE.p, level: cE.lv, block: cE.block, server: cE.server }, 'spell'), false, 'a direct purchase behind a foreign container menu is refused');
  eq(cE.wallet.set, 0, 'the foreign-menu refusal never debits');
  cE.p.openInventory = cE.server.inventoryMenu;
  ticks(cE, 1);
  eq(countAll(cE, 'minecraft:stone'), 64, 'the refused purchase leaves the restored backpack intact');

  const cF = shopLifecycleSetup();
  cF.p.inventory.add('minecraft:stone', 64);
  cF.wallet.balances.set('u-life', K(cF, 'FTWW_SPELL_PRICE'));
  const probe = restoreOrderProbe(cF, 'u-life');
  openShopGui(cF, empty);
  spellButtonOf(cF).click({ setHandled: () => {} });
  ticks(cF, 1);
  eq(probe.tick, 1, 'the ServerEvents tick handler ran exactly once');
  eq(probe.restored, 1, 'the native 36-slot restore is complete inside the ServerEvents tick handler');
  eq(cF.wallet.set, 1, 'delivery is paid only after the native restore inside the same tick');
  eq(cF.server.restoreCounts.get('u-life'), 1, 'the native restore happens exactly once for this purchase');
  ticks(cF, 1);
  eq(cF.server.restoreCounts.get('u-life'), 1, 'no second native restore runs for the same order');
  eq(cF.wallet.set, 1, 'no second debit happens on a later tick');
  eq(countMain(cF, 'irons_spellbooks:scroll'), 1, 'exactly one scroll is delivered');

  const cG = shopLifecycleSetup();
  const g2 = makePlayer(cG.server, { uuid: 'u-life2', username: 'Life2', level: cG.lv, x: 100.5, y: 64.5, z: 100.5 });
  cG.server.players.push(g2);
  putRaw(cG, 'u-life2', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  cG.p.inventory.add('minecraft:stone', 64);
  g2.inventory.add('minecraft:stone', 64);
  cG.wallet.balances.set('u-life', K(cG, 'FTWW_SPELL_PRICE'));
  cG.wallet.balances.set('u-life2', K(cG, 'FTWW_WEAPON_PRICE'));
  openShopGui(cG, empty);
  spellButtonOf(cG).click({ setHandled: () => {} });
  eq(K(cG, 'global.ftwwPurchaseQueue').length, 1, 'first player order enters the shared queue alone');
  eq(g2.openInventory, cG.server.inventoryMenu, 'the second player keeps the vanilla inventory menu');
  const secondGui = openShopGui(cG, empty, g2);
  secondGui.buttons.filter(b => String(b.text.getString()).indexOf('5000 万') >= 0)[0].click({ setHandled: () => {} });
  eq(K(cG, 'global.ftwwPurchaseQueue').length, 2, 'both players can hold one pending order at the same time');
  eq(K(cG, 'global.ftwwPurchaseQueue')[0].key === K(cG, 'global.ftwwPurchaseQueue')[1].key, false, 'the two pending orders are keyed to different players');
  ticks(cG, 1);
  eq(K(cG, 'global.ftwwPurchaseQueue').length, 0, 'both independent orders settle on the next tick');
  eq(cG.wallet.balances.get('u-life'), 0, 'the first player pays exactly the spell price');
  eq(cG.wallet.balances.get('u-life2'), 0, 'the second player pays exactly the weapon price');
  eq(countMain(cG, 'irons_spellbooks:scroll'), 1, 'the first player receives exactly one scroll');
  let weaponCount = 0;
  for (let slot = 0; slot < 36; slot++) { const s = g2.inventory.getStackInSlot(slot); if (s.count > 0 && s.id !== 'minecraft:stone') weaponCount++; }
  eq(weaponCount, 1, 'the second player receives exactly one weapon');
  eq(countAll(cG, 'minecraft:stone'), 64, 'the first player keeps only their own restored stack');
  eq(g2.inventory.getStackInSlot(0).id, 'minecraft:stone', 'the second player keeps their own restored stone');
  eq(g2.inventory.getStackInSlot(0).count, 64, 'the second player keeps exactly 64 of their own stone');

  const cOff = shopLifecycleSetup();
  cOff.p.inventory.add('minecraft:stone', 64);
  cOff.wallet.balances.set('u-life', K(cOff, 'FTWW_SPELL_PRICE'));
  openShopGui(cOff, empty);
  spellButtonOf(cOff).click({ setHandled: () => {} });
  eq(K(cOff, 'global.ftwwPurchaseQueue').length, 1, 'the offline probe queues its order while the player is still online');
  cOff.server.players.length = 0;
  ticks(cOff, 2);
  eq(K(cOff, 'global.ftwwPurchaseQueue').length, 0, 'an offline player never keeps a pending queue entry');
  eq(cOff.wallet.set, 0, 'an offline player is never debited');
  eq(cOff.wallet.balances.get('u-life'), K(cOff, 'FTWW_SPELL_PRICE'), 'an offline player keeps the whole balance');
  eq(countMain(cOff, 'irons_spellbooks:scroll'), 0, 'an offline player receives no reward');

  const cDead = shopLifecycleSetup();
  cDead.p.inventory.add('minecraft:stone', 64);
  cDead.wallet.balances.set('u-life', K(cDead, 'FTWW_SPELL_PRICE'));
  openShopGui(cDead, empty);
  spellButtonOf(cDead).click({ setHandled: () => {} });
  eq(K(cDead, 'global.ftwwPurchaseQueue').length, 1, 'the death probe queues its order before the player dies');
  cDead.p.dead = true;
  ticks(cDead, 2);
  eq(K(cDead, 'global.ftwwPurchaseQueue').length, 0, 'a dead player never keeps a pending queue entry');
  eq(cDead.wallet.set, 0, 'a dead player is never debited');
  eq(cDead.wallet.balances.get('u-life'), K(cDead, 'FTWW_SPELL_PRICE'), 'a dead player keeps the whole balance');
  eq(countMain(cDead, 'irons_spellbooks:scroll'), 0, 'a dead player receives no reward');
  cDead.p.dead = false;

  const cSpec = shopLifecycleSetup();
  cSpec.p.inventory.add('minecraft:stone', 64);
  cSpec.wallet.balances.set('u-life', K(cSpec, 'FTWW_SPELL_PRICE'));
  openShopGui(cSpec, empty);
  spellButtonOf(cSpec).click({ setHandled: () => {} });
  eq(K(cSpec, 'global.ftwwPurchaseQueue').length, 1, 'the spectator probe queues its order before the mode change');
  cSpec.p.spectator = true;
  ticks(cSpec, 2);
  eq(K(cSpec, 'global.ftwwPurchaseQueue').length, 0, 'a spectator never keeps a pending queue entry');
  eq(cSpec.wallet.set, 0, 'a spectator is never debited');
  eq(cSpec.wallet.balances.get('u-life'), K(cSpec, 'FTWW_SPELL_PRICE'), 'a spectator keeps the whole balance');
  eq(countMain(cSpec, 'irons_spellbooks:scroll'), 0, 'a spectator receives no reward');
  cSpec.p.spectator = false;
}
section('T16e no-op insert that returns empty must refund and report failure');
{
  const c = shopSetup(); c.p.inventory.mode = 'noop';
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  const before = countMain(c, 'irons_spellbooks:scroll');
  eq(before, 0, 'no scroll owned before the no-op insert');
  eq(buy(c, 'spell'), false, 'no-op insert that returns empty is rejected by the real count check');
  eq(countMain(c, 'irons_spellbooks:scroll'), 0, 'no scroll was actually delivered');
  eq(c.wallet.set, 2, 'no-op delivery debited then refunded');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_SPELL_PRICE'), 'SDM balance restored after the no-op delivery');
  eq(c.logs.error.some(l => l.indexOf('Reward not present in the target inventory') >= 0), true, 'missing real count delta logged as failure');
  const c2 = shopSetup(); c2.p.inventory.mode = 'noop';
  const pot = c2.p.inventory.add('farmingtales:gold_bucket', 2);
  const summon = K(c2, 'FTWW_SUMMONS')[0];
  eq(buy(c2, 'summon', summon), false, 'no-op summon delivery is rejected');
  eq(c2.p.inventory.getStackInSlot(0).count, 2, 'gold pot restored in the real slot after the no-op summon delivery');
  eq(countAll(c2, 'farmingtales:gold_bucket'), 2, 'gold pot total restored after the no-op summon delivery');
  eq(countMain(c2, summon), 0, 'no summon was actually delivered');
}
section('T16f slot restore failure must not block the verified SDM refund');
{
  const c = shopSetup(); c.p.inventory.mode = 'realfail';
  c.p.inventory.failSlot = 0;
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  eq(buy(c, 'spell'), false, 'purchase fails when insertion is refused');
  eq(c.wallet.set, 2, 'SDM balance written twice (debit then refund)');
  eq(c.wallet.get >= 2, true, 'refund is verified by reading the balance back');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_SPELL_PRICE'), 'SDM refund verified after a slot restore error');
  c.p.inventory.mode = 'normal';
}
section('T16f2 injected slot restore error is reported and never blocks the verified refund');
{
  const c = shopSetup();
  c.p.inventory.mode = 'realfail';
  c.p.inventory.stacks[0] = new Stack('minecraft:stone', 64);
  c.p.inventory.failSlot = 0;
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  const stone = c.p.inventory.getStackInSlot(0);
  eq(stone.count, 64, 'seed stack in slot 0 is 64 stone before the purchase');
  eq(buy(c, 'spell'), false, 'insertion refusal plus injected slot error still reports failure');
  eq(c.wallet.set, 2, 'SDM balance written twice (debit then refund)');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_SPELL_PRICE'), 'SDM refund read back successfully');
  eq(c.logs.error.some(l => l.indexOf('Purchase inventory rollback incomplete slots=0') >= 0), true, 'rollback reports slot 0 exactly once');
  eq(c.logs.error.some(l => l.indexOf('slot write refused (test)') >= 0), true, 'injected slot write refusal is logged');
  for (let slot = 1; slot < 36; slot++) eq(c.p.inventory.getStackInSlot(slot).id, 'minecraft:air', 'slot ' + slot + ' has no shifted stack after the failed restore');
  const c2 = shopSetup();
  c2.p.inventory.mode = 'normal';
  c2.p.inventory.stacks[0] = new Stack('minecraft:stone', 64);
  c2.p.inventory.failSlot = 0;
  c2.wallet.balances.set('u-shop', K(c2, 'FTWW_SPELL_PRICE'));
  eq(buy(c2, 'spell'), true, 'a slot error that is never touched does not block a successful purchase');
  eq(countMain(c2, 'irons_spellbooks:scroll'), 1, 'scroll is delivered when the failing slot is not written');
  eq(c2.wallet.balances.get('u-shop'), 0, 'spell price charged exactly once for the delivered scroll');
}
section('R2 full main backpack of 36 occupied slots rejects scroll and weapon without debit');
{
  const c = shopSetup();
  for (let slot = 0; slot < 36; slot++) c.p.inventory.setStackInSlot(slot, new Stack('minecraft:stone', 64));
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  eq(buy(c, 'spell'), false, 'scroll purchase rejected when all 36 main slots are occupied');
  eq(c.wallet.set, 0, 'no SDM debit for the rejected scroll purchase');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_SPELL_PRICE'), 'scroll price untouched when the main backpack is full');
  eq(countMain(c, 'irons_spellbooks:scroll'), 0, 'no scroll written into the full main backpack');
  eq(c.p.messages.some(m => m.indexOf('背包空间不足') >= 0), true, 'full main backpack message shown');
  ticks(c, 10);
  c.wallet.balances.set('u-shop', K(c, 'FTWW_WEAPON_PRICE'));
  eq(buy(c, 'weapon'), false, 'weapon purchase rejected when all 36 main slots are occupied');
  eq(c.wallet.set, 0, 'no SDM debit for the rejected weapon purchase');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_WEAPON_PRICE'), 'weapon price untouched when the main backpack is full');
  eq(c.p.inventory.getStackInSlot(36).isEmpty(), true, 'armor slot 36 stayed empty');
  eq(c.p.inventory.getStackInSlot(40).isEmpty(), true, 'offhand slot 40 stayed empty');
  const c2 = shopSetup();
  for (let slot = 0; slot < 35; slot++) c2.p.inventory.setStackInSlot(slot, new Stack('minecraft:stone', 64));
  c2.wallet.balances.set('u-shop', K(c2, 'FTWW_SPELL_PRICE'));
  eq(buy(c2, 'spell'), true, 'scroll purchase succeeds with exactly one free main slot');
  eq(countMain(c2, 'irons_spellbooks:scroll'), 1, 'scroll lands in the single free main slot');
  eq(c2.p.inventory.getStackInSlot(35).id, 'irons_spellbooks:scroll', 'scroll used the only free main slot');
  eq(nbtText(c2.p.inventory.getStackInSlot(35).nbt).indexOf('spellContainer') >= 0, true, 'scroll keeps its spell NBT after the insertion');
  eq(c2.wallet.balances.get('u-shop'), 0, 'spell price charged once for the delivered scroll');
}
section('T16g sync failure rolls back the delivered item and refunds the debit');
{
  const c = shopSetup();
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  c.p.sendInventoryUpdate = function () { throw new Error('inventory sync unavailable (test)'); };
  eq(buy(c, 'spell'), false, 'purchase fails when the real inventory sync throws');
  eq(countMain(c, 'irons_spellbooks:scroll'), 0, 'delivered scroll rolled back after a sync failure');
  eq(c.wallet.set, 2, 'sync failure debited then refunded');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_SPELL_PRICE'), 'SDM balance restored after a sync failure');
  eq(c.p.messages.some(m => m.indexOf('购买失败') >= 0), true, 'sync failure is reported as a failure, never a success');
}
section('T16h reporting failures must not roll back a completed transaction');
{
  const c = shopSetup();
  const pot = c.p.inventory.add('farmingtales:gold_bucket', 3);
  const summon = K(c, 'FTWW_SUMMONS')[0];
  c.p.throwOnTell = true;
  eq(buy(c, 'summon', summon), true, 'successful delivery stays committed when tell throws');
  eq(countMain(c, summon), 1, 'summon stays delivered after a reporting failure');
  eq(pot.count, 2, 'gold pot stays consumed after a reporting failure');
  eq(c.p.syncs, 1, 'successful purchase still synced once');
  ticks(c, 10);
  const c2 = shopSetup({ throwOnInfo: true });
  c2.wallet.balances.set('u-shop', K(c2, 'FTWW_SPELL_PRICE'));
  eq(buy(c2, 'spell'), true, 'successful delivery stays committed when console.info throws');
  eq(countMain(c2, 'irons_spellbooks:scroll'), 1, 'scroll stays delivered after a log failure');
  eq(c2.wallet.balances.get('u-shop'), 0, 'spell price stays charged, no rollback of a finished trade');
}
section('T16i wrapper availability is required before any debit');
{
  const c = shopSetup({ wrapperMissing: true });
  c.wallet.balances.set('u-shop', K(c, 'FTWW_WEAPON_PRICE'));
  eq(buy(c, 'weapon'), false, 'purchase fails when the main inventory wrapper is unavailable');
  eq(c.wallet.set, 0, 'no debit without a real main inventory wrapper');
  eq(c.wallet.balances.get('u-shop'), K(c, 'FTWW_WEAPON_PRICE'), 'balance untouched when the wrapper is unavailable');
  const c2 = shopSetup({ wrapperMissing: true });
  const pot = c2.p.inventory.add('farmingtales:gold_bucket', 2);
  eq(buy(c2, 'summon', K(c2, 'FTWW_SUMMONS')[0]), false, 'summon purchase also fails without the wrapper');
  eq(pot.count, 2, 'no gold pot consumed without a real main inventory wrapper');
}
section('R1 main-backpack layout keeps armor and offhand untouched');
{
  const c = shopSetup();
  c.wallet.balances.set('u-shop', K(c, 'FTWW_SPELL_PRICE'));
  const helmet = new Stack('minecraft:leather_helmet', 1);
  const offhand = new Stack('minecraft:shield', 1);
  c.p.inventory.stacks[36] = helmet;
  c.p.inventory.stacks[40] = offhand;
  eq(buy(c, 'spell'), true, 'spell purchase succeeds with armor and offhand occupied');
  eq(c.p.inventory.stacks[36].id, 'minecraft:leather_helmet', 'armor slot untouched by delivery');
  eq(c.p.inventory.stacks[40].id, 'minecraft:shield', 'offhand slot untouched by delivery');
  eq(countMain(c, 'irons_spellbooks:scroll'), 1, 'scroll delivered inside slots 0..35');
  eq(c.p.inventory.getStackInSlot(36).id === 'irons_spellbooks:scroll', false, 'scroll never written into the armor range');
}
section('T17 weapon pool excludes gourmet_blank_blade and keeps native variants/NBT');
{
  const c = create(); load(c);
  const seen = {}; let blank = 0; let lostNbt = 0;
  for (let i = 0; i < 400; i++) {
    const s = c.sandbox.ftwwRandomWeapon();
    seen[s.id] = (seen[s.id] || 0) + 1;
    if (s.id === 'ftboss:gourmet_blank_blade') blank++;
    if (s.id === 'ftboss:dora_nova_launcher' && s.nbt.FTBossManorWeapon !== 'dora') lostNbt++;
    if (s.id === 'ftboss:wenxuanlan_flywheel' && s.nbt.XuanlanWhite !== true) lostNbt++;
  }
  eq(blank, 0, 'gourmet_blank_blade never rolls');
  eq(lostNbt, 0, 'native variant NBT retained on the copied stack');
  eq(Object.keys(seen).length, 3, 'only the three non-excluded catalog entries roll');
  const c2 = create({ catalogEntries: [{ id: 'ftboss:unregistered', nbt: null }, { id: 'ftboss:shadow_sword', nbt: null }] }); load(c2);
  const ids = {};
  for (let i = 0; i < 100; i++) { const s = c2.sandbox.ftwwRandomWeapon(); ids[s.id] = 1; }
  eq(Object.keys(ids).join(','), 'ftboss:shadow_sword', 'unregistered catalog stacks are filtered out');
  const c3 = create({ catalogEntries: [{ id: 'ftboss:unregistered', nbt: null }] }); load(c3);
  let threw = false;
  try { c3.sandbox.ftwwRandomWeapon(); } catch (e) { threw = true; }
  eq(threw, true, 'no usable manor weapons throws');
}
section('T18 enabled lootable spells and legal levels');
{
  const c = create(); load(c);
  const allowed = { 'irons_spellbooks:fireball': [1, 5], 'irons_spellbooks:ice_block': [2, 3] };
  const seen = {}; let illegal = 0;
  for (let i = 0; i < 400; i++) {
    const s = c.sandbox.ftwwRandomSpell();
    const id = s.scroll.spellId; const level = s.scroll.level;
    seen[id] = 1;
    if (!allowed[id]) illegal++;
    else if (level < allowed[id][0] || level > allowed[id][1]) illegal++;
  }
  eq(illegal, 0, 'every scroll uses an enabled lootable spell and a legal level');
  eq(Object.keys(seen).sort().join(','), 'irons_spellbooks:fireball,irons_spellbooks:ice_block', 'disabled and invalid spells excluded');
  const c2 = create({ spells: [{ id: 'irons_spellbooks:blood_slash', lootable: false, min: 1, max: 4 }] }); load(c2);
  let threw = false;
  try { c2.sandbox.ftwwRandomSpell(); } catch (e) { threw = true; }
  eq(threw, true, 'no lootable spell throws instead of granting');
}
section('T19 dropped-entry exception isolation');
{
  const c = create({ rng: () => 0.99 }); load(c); const lv = world(c);
  const p = player(c, { uuid: 'u-iso', username: 'Iso', x: 100.5, y: 64.5, z: 100.5 });
  const bad = makeEntity({ id: 'minecraft:emerald', count: 1, x: 101, y: 64, z: 101, level: lv, owner: p });
  const good = makeEntity({ id: 'minecraft:emerald', count: 1, x: 101, y: 64, z: 101, level: lv, owner: p });
  spawn(c, bad); spawn(c, good);
  Object.defineProperty(bad, 'x', { get() { throw new Error('entity coordinate unavailable (test)'); } });
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 2, 'two drops queued');
  ticks(c, 2);
  eq(prog(c, 'u-iso').emeralds, 1, 'healthy entry still processed in the same scan');
  eq(good.removed, true, 'healthy entry consumed');
  eq(bad.item.count, 1, 'failing entry left untouched');
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 1, 'failing entry kept for retry');
  eq(c.sandbox.global.ftwwPendingEmeraldDrops[0].failures, 1, 'failure counter incremented');
  eq(c.logs.error.some(l => l.indexOf('dropped entry scan failed') >= 0), true, 'per-entry failure logged');
  ticks(c, 6);
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 0, 'entry gives up after three failed attempts');
}
section('T20 structure validation blocks payment');
{
  const c = create(); load(c); const lv = world(c, { overrides: { '-2,0,-2': 'minecraft:air' } });
  const p = player(c, { uuid: 'u-str', username: 'Str', x: 100.5, y: 64.5, z: 100.5 });
  const held = p.inventory.add('minecraft:emerald', 1);
  rcBlock(c, p, held, 'MAIN_HAND');
  eq(held.count, 1, 'incomplete structure blocks the emerald payment');
  eq(prog(c, 'u-str'), null, 'no progress on incomplete structure');
  eq(p.messages.some(m => m.indexOf('结构不完整') >= 0), true, 'structure error message');
  eq(c.server.commands.some(cmd => cmd.indexOf('particle minecraft:angry_villager') >= 0), true, 'missing block highlighted');
  const e = makeEntity({ id: 'minecraft:emerald', count: 1, x: 101, y: 64, z: 101, level: lv, owner: p });
  spawn(c, e); ticks(c, 2);
  eq(e.item.count, 1, 'dropped emerald not consumed on incomplete structure');
  eq(c.sandbox.global.ftwwPendingEmeraldDrops.length, 0, 'structure-rejected drop stops being tracked');
  eq(p.messages.some(m => m.indexOf('投入未生效') >= 0), true, 'drop structure message');
}
section('T21 interaction tick advances exactly +1 per server tick');
{
  const c = create(); load(c);
  eq(Number(c.sandbox.global.ftwwDropScanTick || 0), 0, 'interaction tick starts at 0');
  ticks(c, 1); eq(Number(c.sandbox.global.ftwwDropScanTick), 1, 'after 1 tick');
  ticks(c, 6); eq(Number(c.sandbox.global.ftwwDropScanTick), 7, 'after 7 ticks (steady +1)');
  ticks(c, 13); eq(Number(c.sandbox.global.ftwwDropScanTick), 20, 'after 20 ticks');
}
section('T22 progress display is non-throwing and creative flow preserved');
{
  const c = create(); load(c);
  const p = makePlayer(c.server, { uuid: 'u-show' });
  putRaw(c, 'u-show', '{bad json');
  eq(c.sandbox.ftwwShowProgress(c.server, p), false, 'ftwwShowProgress returns false instead of throwing');
  eq(p.messages.some(m => m.indexOf('数据异常') >= 0), true, 'progress read error reported to player');
  const c2 = create({ rng: () => 0.99 }); load(c2); world(c2);
  const cp = player(c2, { uuid: 'u-creative', username: 'Creative', x: 100.5, y: 64.5, z: 100.5, creative: true });
  const held = cp.inventory.add('minecraft:emerald', 3);
  rcBlock(c2, cp, held, 'MAIN_HAND');
  eq(held.count, 3, 'creative roll consumes no emerald');
  eq(prog(c2, 'u-creative'), null, 'creative roll writes no progress (existing behaviour kept)');
  eq(cp.given.length, 1, 'creative roll still delivers the outcome');
}
section('T23 tooltip script loads and documents the requirements');
{
  const c = create(); loadTooltip(c);
  const lines = [];
  c.handlers.tooltip({ addAdvanced: (id, fn) => fn({}, {}, { add: s => lines.push(String(s)) }) });
  eq(lines.length >= 10, true, 'tooltip adds the requirement lines');
  eq(lines.some(l => l.indexOf('1 / 10 / 100') >= 0), true, 'tooltip documents 1/10/100 thresholds');
  eq(lines.some(l => l.indexOf('31.1% → 45% → 60% → 80%') >= 0), true, 'tooltip documents useful odds');
  eq(lines.some(l => l.indexOf('5000 万') >= 0 && l.indexOf('不含万味胚刃') >= 0), true, 'tooltip documents 50m weapon excluding the blade');
  eq(lines.some(l => l.indexOf('500 万') >= 0), true, 'tooltip documents the 5m spell scroll');
  eq(lines.some(l => l.indexOf('金币锅') >= 0), true, 'tooltip documents gold pot donation');
  eq(lines.some(l => l.indexOf('空手右键打开商店') >= 0), true, 'tooltip documents the Lv3 shop');
}

section('T27 merged live gold-pot pool coexists with the queue rootfix');
{
  const empty = new Stack('minecraft:air', 0);
  const c = create(); load(c); const lv = world(c);
  const p = player(c, { uuid: 'u-pool', username: 'Pool', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(c, 'u-pool', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  p.shift = true;
  const held = p.inventory.add('farmingtales:gold_bucket', 5);
  const ev = rcBlock(c, p, held, 'MAIN_HAND');
  eq(ev.cancelled, true, 'the threshold-crossing pot click is still cancelled');
  eq(prog(c, 'u-pool').goldPots, 105, 'the donation past Lv3 commits the merged total');
  eq(held.count, 0, 'the merged donation consumes the whole offered stack');
  eq(c.goldPool.calls.length, 1, 'the gold pool is rolled exactly once per donation');
  eq(c.goldPool.lastArgs.goldPots, 100, 'the roll sees the pre-donation total');
  eq(c.goldPool.lastArgs.amount, 5, 'the roll sees the whole donated amount');
  eq(c.goldPool.lastArgs.level === lv, true, 'the roll receives the event level');
  eq(p.given.length, 5, 'every pot above 100 delivers one gold-pool reward');
  eq(p.given.every(g => g.count === 1), true, 'every gold-pool reward is a single item');
  eq(p.given[0].id, 'minecraft:golden_apple', 'a non-manor category maps to its rolled stack');
  eq(p.given[0].nbt.GoldPoolCategory, 'iron_spell', 'the delivered stack keeps the rolled category NBT');
  eq(p.given[0].nbt.GoldPoolRoll, 0, 'the delivered stack keeps the rolled index NBT');
  eq(p.given.every(g => !!g.nbt && g.nbt.GoldPoolCategory !== undefined), true, 'Item.of(javaStack) copies the Java NBT into every delivered stack');
  const probe = c.sandbox.Item.of(new JavaStack('minecraft:golden_apple', 2, { GoldPoolRoll: 9 }));
  eq(probe.id, 'minecraft:golden_apple', 'Item.of(javaStack) keeps the item id');
  eq(probe.count, 2, 'Item.of(javaStack) keeps the count');
  eq(probe.nbt.GoldPoolRoll, 9, 'Item.of(javaStack) keeps the Java NBT');
  eq(p.messages.some(m => m.indexOf('金币锅奖池') >= 0), true, 'the Lv3 progress line announces the gold-pot pool');
  eq(p.messages.some(m => m.indexOf('绿宝石有用物品概率') >= 0), true, 'the merged progress line still reports the emerald odds');

  const partial = create(); load(partial); world(partial);
  const pp = player(partial, { uuid: 'u-partial', username: 'Partial', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(partial, 'u-partial', JSON.stringify({ emeralds: 0, goldPots: 98 }));
  pp.shift = true;
  const partialHeld = pp.inventory.add('farmingtales:gold_bucket', 5);
  rcBlock(partial, pp, partialHeld, 'MAIN_HAND');
  eq(prog(partial, 'u-partial').goldPots, 103, 'a partial crossing commits the whole donated amount');
  eq(pp.given.length, 3, 'only the three pots above the threshold roll a reward');
  eq(partialHeld.count, 0, 'a partial crossing still consumes the offered stack');

  const noPool = create({ goldPoolMissing: true }); load(noPool); world(noPool);
  const np = player(noPool, { uuid: 'u-nopool', username: 'NoPool', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(noPool, 'u-nopool', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  const noPoolHeld = np.inventory.add('farmingtales:gold_bucket', 2);
  const refused = noPool.sandbox.ftwwDonatePots({ server: noPool.server, level: noPool.level, block: coreBlock(noPool, np), player: np }, noPoolHeld, 2, null);
  eq(refused, false, 'a missing gold pool refuses the donation');
  eq(noPoolHeld.count, 2, 'a missing gold pool consumes no pot');
  eq(prog(noPool, 'u-nopool').goldPots, 100, 'a missing gold pool leaves the committed total untouched');
  eq(np.messages.some(m => m.indexOf('尚未加载') >= 0), true, 'the player is told the gold pool is not loaded');
  eq(noPool.logs.error.some(l => l.indexOf('Gold pot rewards unavailable') >= 0), true, 'the missing gold pool is logged');

  const shortPool = create({ goldPoolMode: 'short' }); load(shortPool); world(shortPool);
  const sp = player(shortPool, { uuid: 'u-short', username: 'Short', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(shortPool, 'u-short', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  const spHeld = sp.inventory.add('farmingtales:gold_bucket', 2);
  const shortDone = shortPool.sandbox.ftwwDonatePots({ server: shortPool.server, level: shortPool.level, block: coreBlock(shortPool, sp), player: sp }, spHeld, 2, null);
  eq(shortDone, false, 'a short roll list refuses the donation');
  eq(spHeld.count, 2, 'a short roll list consumes no pot');
  eq(prog(shortPool, 'u-short').goldPots, 100, 'a short roll list keeps the committed total');
  eq(sp.messages.some(m => m.indexOf('奖励配置异常') >= 0), true, 'a short roll list reports a configuration problem');
  eq(shortPool.logs.error.some(l => l.indexOf('Gold pot reward count mismatch') >= 0), true, 'the roll-count mismatch is logged');

  const manor = create({ goldPoolCategories: ['manor_reward'] }); load(manor); world(manor);
  const mp = player(manor, { uuid: 'u-manor', username: 'Manor', x: 100.5, y: 64.5, z: 100.5 });
  putRaw(manor, 'u-manor', JSON.stringify({ emeralds: 0, goldPots: 100 }));
  mp.shift = true;
  const manorHeld = mp.inventory.add('farmingtales:gold_bucket', 2);
  rcBlock(manor, mp, manorHeld, 'MAIN_HAND');
  eq(mp.given.length, 2, 'both manor_reward rolls are delivered');
  eq(mp.given[0].id, 'ftboss:dora_nova_launcher', 'the manor_reward category keeps its rolled stack id');
  eq(mp.messages.some(m => m.indexOf('万分之一的奇迹') >= 0), true, 'manor_reward uses the merged live reward line');
  eq(manor.server.commands.some(cmd => cmd.indexOf('minecraft:firework') >= 0), true, 'manor_reward emits the firework particle');
  eq(manor.server.commands.some(cmd => cmd.indexOf('ui.toast.challenge_complete') >= 0), true, 'manor_reward plays the challenge-complete sound');

  const cQ = shopLifecycleSetup();
  cQ.p.inventory.add('minecraft:stone', 64);
  cQ.wallet.balances.set('u-life', K(cQ, 'FTWW_SPELL_PRICE'));
  cQ.p.shift = true;
  const potQ = cQ.p.inventory.add('farmingtales:gold_bucket', 4);
  rcBlock(cQ, cQ.p, potQ, 'MAIN_HAND');
  eq(prog(cQ, 'u-life').goldPots, 104, 'the gold pool commits on top of the queued-purchase lifecycle');
  eq(cQ.p.given.length, 4, 'the merged donation delivers all four pool rewards');
  cQ.p.shift = false;
  openShopGui(cQ, empty);
  spellButtonOf(cQ).click({ setHandled: () => {} });
  eq(K(cQ, 'global.ftwwPurchaseQueue').length, 1, 'the queue rootfix still accepts a purchase after the gold merge');
  ticks(cQ, 1);
  eq(cQ.wallet.set, 1, 'the queue rootfix still debits exactly once after the gold merge');
  eq(cQ.wallet.balances.get('u-life'), 0, 'the queue rootfix still charges exactly the spell price');
  eq(countMain(cQ, 'irons_spellbooks:scroll'), 1, 'the queue rootfix still delivers the scroll after the gold merge');
  eq(countAll(cQ, 'minecraft:stone'), 64, 'the queue rootfix still keeps the restored original stack');
}

console.log('');
console.log('SUMMARY pass=' + pass + ' fail=' + fail);
if (failures.length) { console.log('FAILURES:'); failures.forEach(f => console.log('  - ' + f)); }
process.exit(fail === 0 ? 0 : 1);
