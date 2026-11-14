'use strict';

const THIRD_PERSON_GRIP_Y = 7.5;
/** 第三人称横向偏移（单位 = 1/16 格 = "像素"）；越小越靠近玩家身体中线。 */
const THIRD_PERSON_SIDE_X = -1.6;
/** 第三人称俯仰（显示系角度；净下倾角 = 90 - 该值）。 */
const THIRD_PERSON_PITCH_X = 30;
/**
 * 在握点补偿之外额外加的偏移（单位 1/16 格 = "像素"）。
 * 显示 translation 经 ItemInHandLayer 的 [-90X,180Y] 基座后落到模型系：
 *   item X → 模型 -X（左右，+ 为角色右手侧）
 *   item Y → 模型 +Z（前后；模型前方是 -Z，所以"向前"要取负）
 *   item Z → 模型 +Y（上下，+ 为上）
 */
// 实机确认（2026-09-17 18:07 用户反馈）：item Y 增大 = 玩家视线/脸部方向（前）；item Z 增大 = 上。
const THIRD_PERSON_FRONT_PX = 4;
const THIRD_PERSON_LIFT_PX = 2;

function thirdPersonDisplay() {
  const rotation = [THIRD_PERSON_PITCH_X, 90, 0];
  const scale = [0.85, 0.85, 0.85];
  const gripOffset = THIRD_PERSON_GRIP_Y * scale[1];
  const radians = rotation[0] * Math.PI / 180;
  return {
    rotation,
    translation: [THIRD_PERSON_SIDE_X,
      Number((-2 - gripOffset * Math.cos(radians) + THIRD_PERSON_FRONT_PX).toFixed(6)),
      Number((2 - gripOffset * Math.sin(radians) + THIRD_PERSON_LIFT_PX).toFixed(6))],
    scale
  };
}

module.exports = { THIRD_PERSON_GRIP_Y, THIRD_PERSON_SIDE_X, THIRD_PERSON_PITCH_X, THIRD_PERSON_FRONT_PX, THIRD_PERSON_LIFT_PX, thirdPersonDisplay };
