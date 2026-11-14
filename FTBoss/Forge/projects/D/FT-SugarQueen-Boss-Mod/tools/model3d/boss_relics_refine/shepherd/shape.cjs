module.exports = function build(cube) {
  cube('chamber_bottom',[6.65,3.15,6.45],[9.35,3.91,9.4],'bone');
  cube('chamber_left_wall',[5.95,4.03,6.72],[6.59,8.9,9.04],'bone');
  cube('chamber_right_wall',[9.41,4.03,6.72],[10.05,8.9,9.04],'bone');
  cube('chamber_rear',[6.49,3.83,8.94],[9.51,9.04,9.54],'bone');
  cube('chamber_front_lower',[6.49,3.83,6.24],[9.51,7.8,6.88],'boneLight');
  cube('chamber_top_bridge',[6.49,8.87,6.24],[9.51,9.66,9.44],'boneLight');
  for(const side of [-1,1]) {
    const centerX=8+side*1.77;
    cube('bone_lower_chamfer_'+side,[centerX-.46,3.44,6.58],[centerX+.46,4.49,9.15],'bone',['z',-side*45,[centerX,3.96,8]]);
    cube('bone_upper_chamfer_'+side,[centerX-.46,8.49,6.58],[centerX+.46,9.54,9.15],'boneLight',['z',side*45,[centerX,9.01,8]]);
    cube('fipple_side_'+side,[8+side*1.25-.3,7.65,6.19],[8+side*1.25+.3,9.07,7.14],'boneLight');
  }
  cube('front_upper_rail',[6.55,8.85,6.02],[9.45,9.45,6.42],'metalLight');
  cube('front_lower_rail',[6.55,3.38,6.02],[9.45,3.9,6.47],'metal');
  cube('front_left_rail',[5.99,4.05,6.09],[6.49,8.88,6.62],'metalLight');
  cube('front_right_rail',[9.51,4.05,6.09],[10.01,8.88,6.62],'metal');
  for(const side of [-1,1]) {
    for(const [height,sign] of [[3.96,-side],[9.01,side]]) cube('front_chamfer_'+side+'_'+height,[8+side*1.77-.2,height-.64,6.01],[8+side*1.77+.2,height+.64,6.51],'metal',['z',sign*45,[8+side*1.77,height,6.24]]);
  }
  cube('rear_lower_binding',[6.55,3.43,9.36],[9.45,3.9,9.74],'metal');
  cube('rear_upper_binding',[6.55,8.87,9.36],[9.45,9.39,9.74],'metalLight');
  cube('rear_left_binding',[6.02,4.03,9.22],[6.47,8.94,9.64],'metal');
  cube('rear_right_binding',[9.53,4.03,9.22],[9.98,8.94,9.64],'metal');
  cube('rear_bone_inlay',[6.95,4.35,9.41],[9.05,8.6,9.75],'boneLight');
  cube('rear_engraved_spine',[7.76,4.7,9.68],[8.24,8.22,9.91],'metal');
  for(const level of [5.08,6.25,7.42]) cube('rear_binding_bar_'+level,[7.12,level,9.69],[8.88,level+.2,9.91],'metalLight');
  cube('fipple_hollow_shadow',[6.91,7.78,7.04],[9.09,8.93,7.32],'leather');
  cube('fipple_airway_left',[6.87,7.77,6.72],[7.12,8.89,7.41],'metal');
  cube('fipple_airway_right',[8.88,7.77,6.72],[9.13,8.89,7.41],'metal');
  cube('fipple_sharp_labium',[7.13,7.68,6.18],[8.87,8.11,6.94],'metalLight',['x',22.5,[8,7.83,6.62]]);
  cube('fipple_ceiling_lip',[7.04,8.81,6.25],[8.96,9.04,7.24],'boneLight');
  cube('mouth_neck',[7.08,9.31,7.05],[8.92,10.71,8.86],'boneLight');
  cube('mouthpiece_rear',[7.12,10.52,8.44],[8.88,11.59,8.91],'bone');
  cube('mouthpiece_front',[7.12,10.52,7.0],[8.88,11.59,7.49],'boneLight');
  cube('mouthpiece_left',[6.93,10.52,7.43],[7.4,11.59,8.52],'metalLight');
  cube('mouthpiece_right',[8.6,10.52,7.43],[9.07,11.59,8.52],'metal');
  cube('mouthpiece_windway',[7.36,10.57,7.44],[8.64,10.73,8.52],'leather');
  cube('upper_mouth_binding_front',[7.06,11.31,6.92],[8.94,11.68,7.36],'metalLight');
  cube('upper_mouth_binding_back',[7.06,11.31,8.56],[8.94,11.68,9.0],'metal');
  cube('neck_grass_band_front',[6.77,9.5,6.8],[9.23,10.03,7.19],'strap');
  cube('neck_grass_band_back',[6.77,9.5,8.7],[9.23,10.03,9.09],'strap');
  cube('neck_grass_band_left',[6.76,9.5,7.09],[7.14,10.03,8.81],'strap');
  cube('neck_grass_band_right',[8.86,9.5,7.09],[9.24,10.03,8.81],'strap');
  for(const level of [9.48,9.96]) {
    cube('band_gold_edge_front_'+level,[6.74,level,6.74],[9.26,level+.15,7.0],'metalLight');
    cube('band_gold_edge_back_'+level,[6.74,level,8.9],[9.26,level+.15,9.16],'metal');
  }
  cube('enamel_crest',[7.05,4.92,5.94],[8.95,7.24,6.29],'leather');
  cube('crest_upper_bevel',[7.28,7.16,5.88],[8.72,7.46,6.26],'metalLight');
  cube('crest_lower_bevel',[7.28,4.72,5.88],[8.72,5.02,6.26],'metal');
  cube('crest_left_border',[6.91,5.04,5.87],[7.17,7.11,6.26],'metalLight');
  cube('crest_right_border',[8.83,5.04,5.87],[9.09,7.11,6.26],'metal');
  cube('paw_central_pad',[7.51,5.29,5.65],[8.49,5.98,6.03],'boneLight');
  for(const [centerX,centerY] of [[7.36,6.32],[7.79,6.62],[8.22,6.62],[8.65,6.32]]) cube('paw_toe_'+centerX,[centerX-.16,centerY-.24,5.64],[centerX+.16,centerY+.24,6.02],'boneLight',['z',centerX<8?22.5:-22.5,[centerX,centerY,5.84]]);
  for(const [rivetX,rivetY] of [[6.24,4.78],[9.76,4.78],[6.24,8.11],[9.76,8.11]]) {
    cube('front_fastener_'+rivetX+'_'+rivetY,[rivetX-.15,rivetY-.15,5.88],[rivetX+.15,rivetY+.15,6.23],'metalLight');
    cube('rear_fastener_'+rivetX+'_'+rivetY,[rivetX-.15,rivetY-.15,9.51],[rivetX+.15,rivetY+.15,9.82],'metalLight');
  }
  cube('bail_seat',[7.59,9.98,8.66],[8.41,10.9,9.31],'metal');
  cube('bail_left',[7.06,10.54,8.85],[7.45,12.06,9.27],'metal');
  cube('bail_right',[8.55,10.54,8.85],[8.94,12.06,9.27],'metalLight');
  cube('bail_lower',[7.4,10.43,8.85],[8.6,10.84,9.27],'metal');
  cube('bail_upper',[7.4,11.83,8.85],[8.6,12.24,9.27],'metalLight');
  cube('leather_cord_top',[6.94,14.04,8.68],[9.06,14.49,9.13],'leather');
  cube('leather_cord_bottom',[6.94,10.72,8.68],[9.06,11.17,9.13],'leather');
  cube('leather_cord_left',[5.73,11.76,8.68],[6.18,13.48,9.13],'leather');
  cube('leather_cord_right',[9.82,11.76,8.68],[10.27,13.48,9.13],'leather');
  for(const [centerX,centerY,angle] of [[6.42,13.72,-45],[9.58,13.72,45],[6.42,11.49,45],[9.58,11.49,-45]]) cube('cord_corner_'+centerX+'_'+centerY,[centerX-.23,centerY-.74,8.68],[centerX+.23,centerY+.74,9.13],'leather',['z',angle,[centerX,centerY,8.9]]);
  for(const [centerX,centerY,angle] of [[6.42,13.72,-45],[9.58,13.72,45]]) {
    cube('cord_grass_wrap_'+centerX,[centerX-.29,centerY-.32,8.6],[centerX+.29,centerY+.32,9.21],'strap',['z',angle,[centerX,centerY,8.9]]);
    cube('cord_gold_ferrule_'+centerX,[centerX-.31,centerY-.45,8.55],[centerX+.31,centerY-.23,9.25],'metalLight',['z',angle,[centerX,centerY,8.9]]);
  }
  cube('back_lower_tassel_anchor',[8.54,4.07,9.37],[9.21,4.72,9.88],'strap');
  cube('grass_ribbon_tail',[8.43,2.79,9.36],[9.18,4.37,9.81],'strap',['z',-22.5,[8.8,3.9,9.58]]);
  cube('grass_ribbon_bound_tip',[8.41,2.7,9.32],[9.2,2.95,9.85],'strapEdge',['z',-22.5,[8.8,3.9,9.58]]);
};
