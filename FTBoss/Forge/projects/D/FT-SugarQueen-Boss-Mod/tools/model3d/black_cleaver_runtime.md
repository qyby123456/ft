# Black cleaver runtime coordinate contract

The reference generator authors positions in the reference-image basis. GeckoLib
4.8.4 mirrors position and pivot X while loading geometry; its native cube and
bone angles are `[-X, -Y, +Z]`. In the unmirrored authoring coordinates this is
equivalent to `[-X, +Y, -Z]`, in XYZ point-rotation order. Passing authoring angles
directly into the geometry JSON rotates 45-degree strips the wrong way.

`cube_space.cjs` exposes `geckoRotation` (an involution) and an explicit
`loadGeo(filename, { geckolib: true })` mode. Cleaver tools opt in. Unrelated
legacy previews retain their previous behavior. Do not add another approximate
transform implementation. The native test uses the actual installed GeckoLib
baker, RenderUtils, PoseStack and Minecraft ItemTransform, not a Java copy of the
JavaScript math. It covers compound bone rotations, nonzero cube pivots, blood
animation poses, per-vertex UVs and mirrored display transforms.

The editable Blockbench project uses native reflected-X positions/pivots and
`[-X,-Y,+Z]` rotations, matching the GeckoLib plugin's animation export signs.
The standalone preview unreflects X for comparison with the drawn reference;
this is not a different geometry. The project consistency check normalizes the
coordinate conventions before comparing cubes, group pivots and keyframes.

In the unmirrored coordinates, GeckoLib's east/west UV names are exchanged and
the U direction on north/south is reversed relative to the previous preview.
The renderer's exported FACE_CORNERS table is checked against all 8,568 native
UV vertices. The generator compensates the front chart UVs to preserve the
reference motif, rather than changing only the preview to hide the mismatch.

The grip center and weapon animation pivot are both `[0,0,0]`. A 5.5-unit rebase
is applied to the geometry and physical joint pivots, not the root/weapon pivot.
ItemRenderer's -0.5 translation and GeoItemRenderer's [0.5,0.51,0.5] translation
leave a 0.01-block offset; the custom renderer cancels it once, outside re-renders.
Revision 06 checked only ItemInHandLayer's [-90 X,180 Y] basis, omitting the
native PlayerModel arm and palm. The user's 12:38/12:39 screenshots rejected
that result. Native HumanoidModel ITEM pose adds -18 degrees on the arm, and
the layer's [handSign/16,0.125,-0.625] offset is not the center of the palm.
Revision 06 misses the palm by sqrt(8) model units and points the cutting edge
sideways. Do not assert that a zero translation means a correct grip.

Revision 07 uses third-person rotation [72,90,0], translation [1.4,-2,2], scale
0.85. The 72-degree rotation compensates the actual held-item arm plus hand
basis; the 90-degree yaw puts the large cutting edge forward, not across the
body. The physical grip and animation pivot remain [0,0,0]. The display shift
places that point in the last four units of the native arm cube, 1.4 model units
outward from palm center. This stays inside even the slim palm and leaves room
for the thick head's fittings in the neutral pose. Character-specific poses,
walking and attacks still require in-game checks.
The test bakes actual classic/slim PlayerModels, invokes the native ITEM arm
pose and idle bob, then calls translateToHand, ItemTransform and PoseStack.
Both hands, both arm meshes and nine idle samples total 36 checks. Neutral
shaft direction is [0,-1,0] and cutting direction [0,0,-1] in player-model space;
idle bob is retained rather than artificially counteranimated.

Minecraft mirrors display Y/Z angles and translation X for the left hand. Since
this axe is asymmetric, the opposite-hand JSON also turns it to the other face:
[rotationX, rotationY-180, -rotationZ]. This keeps the large blade on the same
screen-relative side for each hand, rather than mirroring just the shaft axis.
The first-person right-hand pose uses [0,125,0], translation [-0.7,-0.5,-4.5],
scale 0.55. The cutting edge faces predominantly forward, rather than presenting
the entire broad face to the camera. The shaft remains vertical. Geometry,
textures, animation and non-hand display contexts remain unchanged.

The viewport check composes native ItemTransform with ItemInHandRenderer's idle
translation [handSign*0.56,-0.52,-0.72], 70-degree hand FOV and 16:9/16:10 aspect
ratios. It checks the closed, entry-complete and blood-idle shapes in both hands,
keeping the full head/upper shaft in frame and outside the central aiming area.
The lower grip may extend below the viewport. Character-specific arm poses,
view bobbing and transient attacks still require in-game acceptance. The native
test checks palm alignment and cutting direction as well as shaft direction;
revision 06 fails this expanded test. `hand-validation.json` contains native
matrices and closed/blood-open vertex scenes. `render_cleaver_hands.cjs` renders
them directly without snapping the weapon onto the reference body. Its four
panels are right-hand front, right-hand side, blood-open oblique and left-hand
oblique. These are native-matrix offline previews, not in-game screenshots.

Run the reference, runtime, native-gecko and surface checks before publishing.
Native checks require the locally installed 1.20.1 libraries; the nested math
dependency is extracted only into the build directory. Use deploy_jar.ps1 for
deployment and never overwrite the running client's mod jar.
