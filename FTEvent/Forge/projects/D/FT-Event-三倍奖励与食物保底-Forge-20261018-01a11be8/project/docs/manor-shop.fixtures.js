const createBeforeManorCatalog = create;
create = function (options) {
  options = options || {};
  const candidate = createBeforeManorCatalog(options);
  const originalLoader = candidate.sandbox.Java.loadClass;
  const definitions = options.manorSpells || [
    { id: 'ftboss:effigy_volley', enabled: true, min: 1, max: 5 },
    { id: 'ftboss:candle_reap', enabled: true, min: 2, max: 3 },
    { id: 'ftboss:feather_fan', enabled: false, min: 1, max: 5 }
  ];
  const spells = definitions.map(definition => ({
    getSpellId: () => definition.id,
    isEnabled: () => definition.enabled,
    allowLooting: () => false,
    getMinLevel: () => definition.min,
    getMaxLevel: () => definition.max
  }));
  candidate.manorCatalogCalls = [];
  candidate.sandbox.Java.loadClass = name => {
    if (name === 'net.farmingtales.ftboss.util.ManorSpellRules$Kind') {
      if (options.manorKindsMissing) throw new Error('ManorSpellRules.Kind unavailable (test)');
      return { values: () => spells.map((spell, index) => index) };
    }
    if (name === 'net.farmingtales.ftboss.integ.spells.ManorIronSpells') {
      if (options.manorCatalogMissing) throw new Error('ManorIronSpells unavailable (test)');
      return { spell(kind) {
        candidate.manorCatalogCalls.push(kind);
        return spells[kind];
      } };
    }
    if (name === 'io.redspace.ironsspellbooks.api.registry.SpellRegistry' && options.rejectGeneralRegistry) {
      throw new Error('Shop must not request the general spell registry');
    }
    return originalLoader(name);
  };
  return candidate;
};
