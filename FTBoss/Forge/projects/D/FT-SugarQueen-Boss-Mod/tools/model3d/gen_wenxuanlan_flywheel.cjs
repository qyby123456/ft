const { generate } = require('./gen_xuanlan_weapons.cjs');

if (require.main === module) console.log(JSON.stringify(generate(process.argv[2]), null, 2));
module.exports = { generate };
