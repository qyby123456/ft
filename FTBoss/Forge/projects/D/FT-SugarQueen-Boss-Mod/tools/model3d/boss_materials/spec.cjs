const specs = [
  {
    id: 'frost', title: '霜糖晶冠', concept: '霜糖晶锭 · 糖霜女皇',
    palette: { K: '#304354', d: '#537c91', b: '#77bcd0', m: '#a2e0e7', l: '#d1f4ee', h: '#faffef', a: '#d88ba9', A: '#ffd5d9', g: '#c39b53' },
    pixels: [
      '................', '........KK......', '.......KAhK.....', '....KK.KAlK.KK..',
      '...KhhKKAAKKAhK.', '..KlhllKAAKAlK..', '..KmlhhlllAhK...', '.KmlhllhhllmK...',
      '.KblmlhhllmmKK..', '..KbbmllmmmbbdK.', '..KdmbmmbbbddK..', '...KdbmbbddKK...',
      '....KdbddKK.....', '.....KdKK.......', '......K.........', '................'
    ]
  },
  {
    id: 'butcher', title: '屠夫骨牙', concept: '血骨与獠牙 · 屠夫',
    palette: { K: '#492c34', d: '#8b6b5c', b: '#bca37f', m: '#e4d1a4', l: '#fff0c5', h: '#fffce1', a: '#963948', A: '#dd6271', g: '#61323e' },
    pixels: [
      '................', '...KKK..........', '..KhlmK.........', '..KhlmbK........',
      '...KhlmbKK......', '...KhlmAAaK.KK..', '....KlmAAaKKhlK.', '....KKaaAalmhlK.',
      '...KAaAaalmhlK..', '..KAAaaalmhlK...', '..KaaKblmhlK....', '...KKblmhlK.....',
      '....KlmhlK......', '.....KmlK.......', '......KK........', '................'
    ]
  },
  {
    id: 'tea', title: '茶心纤维', concept: '茶脉纤维束 · 沙耶',
    palette: { K: '#293f30', d: '#3d673b', b: '#5b914a', m: '#82bb5e', l: '#b8dc86', h: '#e0efaf', a: '#986943', A: '#d8b36c', g: '#59794a' },
    pixels: [
      '................', '.........KKK....', '......KKKhlK....', '.....KlmhlK.....',
      '....KhlmKlmKK...', '...KhlmKKlmhlK..', '...KlmKAlmhlK...', '..KlmKAAAlmK....',
      '..KlmAAAlmK.....', '.KlmKAlmhlK.....', '.KlmKKlmhlK.....', '..KKKlmhlK......',
      '...KlmhlK.......', '...KhlKK........', '....KK..........', '................'
    ]
  },
  {
    id: 'wine', title: '陈酿酒封', concept: '紫酿封蜡锭 · 酒桶王',
    palette: { K: '#3f2c49', d: '#604071', b: '#85519a', m: '#b179bf', l: '#d9a5d6', h: '#f4d9eb', a: '#a77842', A: '#e4c07c', g: '#642743' },
    pixels: [
      '................', '................', '.......KKKK.....', '.....KKhlAAK....',
      '...KKhlmAAAlK...', '..KhlmAAAlmmK...', '.KAlmAAAlmmbbK..', '.KAAmAlmmggmbK..',
      '.KaAAlmbgAgbdK..', '..KaAlmbgggbdK..', '...KaAlmbbddK...', '....KabbddKK....',
      '.....KddKK......', '......KK........', '................', '................'
    ]
  },
  {
    id: 'cheese', title: '奶酪月牙', concept: '熟成月牙酪 · 鼠群',
    palette: { K: '#694527', d: '#a26b29', b: '#d59c36', m: '#efc451', l: '#ffe282', h: '#fff3b4', a: '#ba812d', A: '#f5b945', g: '#966126' },
    pixels: [
      '................', '.....KKK........', '....KhlmK.......', '...KhlmK........',
      '..KhlmK.........', '..KlmK..........', '.KhlmK....KK....', '.KlmalKKKKhlK...',
      '.KlmmlhhlmmlK...', '.KlmamlmmamlK...', '..KblmmlmmbK....', '..KdbbambbdK....',
      '...KdbbbddK.....', '....KKddKK......', '......KK........', '................'
    ]
  },
  {
    id: 'capacitor', title: '蓄能电芯', concept: '铜极蓄能芯 · 故障机器人',
    palette: { K: '#273b48', d: '#39596b', b: '#4d8194', m: '#44bfd0', l: '#97e8e3', h: '#e5fff0', a: '#b77c48', A: '#f2c783', g: '#657882' },
    pixels: [
      '................', '.....KK..KK.....', '....KAaKKAaK....', '...KKAAKKAAKK...',
      '..KgglllllggK...', '..KgblmmmlbgK...', '..KgblmhhmbgK...', '..KgblhhmmbgK...',
      '..KgblmhmmbgK...', '..KgblhhmmbgK...', '..KgbmmmlbbgK...', '..KggbbbbbggK...',
      '...KKdddddKK....', '.....KddKK......', '......KK........', '................'
    ]
  },
  {
    id: 'spore', title: '虹彩孢子', concept: '虹彩菌簇 · 炫彩答辩超人',
    palette: { K: '#42314b', d: '#66517b', b: '#956bad', m: '#be8ed1', l: '#e6b4e5', h: '#fff2dc', a: '#5fae8b', A: '#a4e5ae', g: '#edbc65' },
    pixels: [
      '................', '.......KKK......', '......KhlmK.....', '.....KhlhmmK....',
      '...KKlmhmmbKK...', '..KhlmmbbbbhlK..', '.KhlhmKKKKhlmmK.', '.KmmmbKllKbbbdK.',
      '..KKKKKlbKKKKK..', '....KAlmblAK....', '...KAhmblmAAK...', '..KAhlmbllmAaK..',
      '..KAAlmmlmAaaK..', '...KAAAAAaaaK...', '....KKKKKKKK....', '................'
    ]
  },
  {
    id: 'apple', title: '金苹果核', concept: '鎏金果核锭 · 苹果爵士',
    palette: { K: '#63412d', d: '#a17130', b: '#d19a37', m: '#edbf50', l: '#ffe483', h: '#fff4b9', a: '#4a7741', A: '#91bb63', g: '#965331' },
    pixels: [
      '................', '........KKKK....', '.......KAAlK....', '.......KaAK.....',
      '.....KKKgKK.....', '...KKhlmglhKK...', '..KhlmmmmlmmlK..', '.KhlmmllmmlmmbK.',
      '.KlmmlgmlgmlmbK.', '.KblmmlgmlmmbdK.', '..KblmmmmmbbdK..', '...KdbmbmbddK...',
      '....KdbbddKK....', '.....KKdKK......', '.......K........', '................'
    ]
  },
  {
    id: 'shepherd', title: '牧野哨骨', concept: '牧野束骨 · 边牧犬王',
    palette: { K: '#3a423a', d: '#827e62', b: '#b2af89', m: '#d6d6ac', l: '#ebedce', h: '#fffbe7', a: '#547749', A: '#91b775', g: '#bc9750' },
    pixels: [
      '................', '..KKK...........', '.KhlmK..........', '.KhlmbKK........',
      '..KhlmblKK......', '..KKhlmAAaK.....', '....KlmAAaaK....', '.....KAAaAlmKK..',
      '.....KAaAlmhlmK.', '......KAlmhlhlK.', '.......KlmhlmK..', '........KlhmK...',
      '........KlmhlK..', '.........KhlmK..', '..........KKK...', '................'
    ]
  },
  {
    id: 'tide', title: '潮汐晶材', concept: '深海潮晶 · 潮汐鱼',
    palette: { K: '#244c63', d: '#326f87', b: '#459fb1', m: '#67cbd0', l: '#b4ece1', h: '#f0ffec', a: '#5276b7', A: '#96b9ed', g: '#4067a0' },
    pixels: [
      '................', '.......KK.......', '......KhlK......', '...KK.KhlmK.....',
      '..KhlKKhlmK.KK..', '..KhlmKhlmKKAlK.', '..KhlmKhlmKAlAK.', '...KlmKhlmKAAK..',
      '...KlmmlhhmlAK..', '..KAlmhlhhlmAK..', '..KAlmhhlmlmbK..', '...KAbmlmmbbdK..',
      '....KdbbbddKK...', '.....KKddKK.....', '.......KK.......', '................'
    ]
  }
];
module.exports = { specs };
