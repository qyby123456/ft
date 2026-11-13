export const size=[61,13,61]
export const regions=[
  {id:'water-shrine',name:'西北水景小室',bounds:[14,3,29,15],anchor:[21,2,12],roof:true},
  {id:'dining',name:'西侧餐厅',bounds:[3,16,25,33],anchor:[20,2,29],roof:true},
  {id:'great-hall',name:'东北藏书大厅',bounds:[31,3,57,27],anchor:[47,2,20],roof:true},
  {id:'treasure',name:'西南宝藏小室',bounds:[3,39,20,53],anchor:[8,2,46],roof:true},
  {id:'courtyard',name:'中央古树水庭',bounds:[27,31,49,49],anchor:[31,2,46],roof:false},
  {id:'entrance',name:'东南入口门厅',bounds:[43,49,57,60],anchor:[50,2,57],roof:true},
  {id:'east-west-corridor',name:'东西连接廊',bounds:[24,17,34,23],anchor:[28,2,20],roof:true},
  {id:'elbow-north',name:'转角廊北段',bounds:[19,30,25,43],anchor:[21,2,36],roof:true},
  {id:'elbow-east',name:'转角廊东段',bounds:[23,35,33,41],anchor:[26,2,37],roof:true},
  {id:'hall-court-link',name:'大厅通庭院短廊',bounds:[35,25,41,33],anchor:[38,2,29],roof:true}
]
export const openings=[
  [20,15,22,16],[24,19,32,21],[20,32,22,36],[23,36,28,38],
  [19,41,24,43],[20,38,22,43],[26,36,33,38],[36,25,40,33],[44,48,48,51],[49,59,51,60]
]
export const inRegion=(across,forward,region)=>across>=region.bounds[0]&&across<=region.bounds[2]&&forward>=region.bounds[1]&&forward<=region.bounds[3]
