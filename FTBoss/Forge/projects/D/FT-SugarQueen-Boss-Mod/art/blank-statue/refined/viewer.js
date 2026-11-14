'use strict';

(async () => {
  const canvas = document.querySelector('#view');
  const status = document.querySelector('#status');
  const gl = canvas.getContext('webgl', { antialias: true, alpha: false });
  if (!gl) throw new Error('WebGL 不可用');
  function shader(type, source) {
    const result = gl.createShader(type);
    gl.shaderSource(result, source);
    gl.compileShader(result);
    if (!gl.getShaderParameter(result, gl.COMPILE_STATUS)) throw new Error(gl.getShaderInfoLog(result));
    return result;
  }
  const program = gl.createProgram();
  gl.attachShader(program, shader(gl.VERTEX_SHADER, `
    attribute vec3 position; attribute vec2 uv; attribute vec3 normal; attribute float emission;
    uniform vec2 camera; uniform float zoom; uniform float aspect;
    varying vec2 textureUv; varying float brightness;
    void main() {
      float yaw=camera.x; float pitch=camera.y;
      vec3 right=vec3(-cos(yaw),0.0,-sin(yaw));
      vec3 up=vec3(-sin(pitch)*sin(yaw),cos(pitch),sin(pitch)*cos(yaw));
      vec3 forward=vec3(sin(yaw)*cos(pitch),sin(pitch),-cos(yaw)*cos(pitch));
      vec3 local=position-vec3(0.5);
      gl_Position=vec4(dot(local,right)*zoom/aspect,dot(local,up)*zoom,-dot(local,forward),1.0);
      textureUv=uv; brightness=mix(min(1.08,0.62+max(0.0,dot(normal,vec3(-0.35,0.8,-0.48)))*0.48),1.0,emission);
    }
  `));
  gl.attachShader(program, shader(gl.FRAGMENT_SHADER, `
    precision mediump float; varying vec2 textureUv; varying float brightness; uniform sampler2D atlas;
    void main() { vec4 pixel=texture2D(atlas,textureUv); gl_FragColor=vec4(pixel.rgb*brightness,pixel.a); }
  `));
  gl.linkProgram(program);
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) throw new Error(gl.getProgramInfoLog(program));
  gl.useProgram(program);
  const positions = [];
  const coordinates = [];
  const normals = [];
  const vertices = [];
  let emission = 0;
  const response = await fetch('statue_blank.obj');
  if (!response.ok) throw new Error('OBJ 请求失败');
  for (const line of (await response.text()).split('\n')) {
    const [kind, ...values] = line.trim().split(/\s+/);
    if (kind === 'v') positions.push(values.map(Number));
    if (kind === 'vt') coordinates.push(values.map(Number));
    if (kind === 'vn') normals.push(values.map(Number));
    if (kind === 'usemtl') emission = values[0] === 'glow' ? 1 : 0;
    if (kind === 'f') for (const reference of values) {
      const indices = reference.split('/').map(value => Number(value) - 1);
      vertices.push(...positions[indices[0]], ...coordinates[indices[1]], ...normals[indices[2]], emission);
    }
  }
  const buffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
  for (const [name, size, offset] of [['position', 3, 0], ['uv', 2, 3], ['normal', 3, 5], ['emission', 1, 8]]) {
    const location = gl.getAttribLocation(program, name);
    gl.enableVertexAttribArray(location);
    gl.vertexAttribPointer(location, size, gl.FLOAT, false, 36, offset * 4);
  }
  const image = await new Promise((resolve, reject) => {
    const loaded = new Image(); loaded.onload = () => resolve(loaded); loaded.onerror = reject; loaded.src = 'statue_blank.png';
  });
  const texture = gl.createTexture();
  gl.bindTexture(gl.TEXTURE_2D, texture);
  gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
  gl.enable(gl.DEPTH_TEST);
  gl.enable(gl.CULL_FACE);
  gl.frontFace(gl.CCW);
  const cameraLocation = gl.getUniformLocation(program, 'camera');
  const zoomLocation = gl.getUniformLocation(program, 'zoom');
  const aspectLocation = gl.getUniformLocation(program, 'aspect');
  let yaw = -0.56;
  let pitch = 0.35;
  let zoom = 1.58;
  let spinning = false;
  let drag = null;
  canvas.addEventListener('pointerdown', event => { drag = [event.clientX, event.clientY]; canvas.setPointerCapture(event.pointerId); });
  canvas.addEventListener('pointermove', event => {
    if (!drag) return;
    yaw -= (event.clientX - drag[0]) * 0.008;
    pitch = Math.max(-1.57, Math.min(1.57, pitch + (event.clientY - drag[1]) * 0.008));
    drag = [event.clientX, event.clientY];
  });
  canvas.addEventListener('pointerup', () => { drag = null; });
  canvas.addEventListener('pointercancel', () => { drag = null; });
  canvas.addEventListener('wheel', event => { event.preventDefault(); zoom = Math.max(0.7, Math.min(3.3, zoom * Math.exp(-event.deltaY * 0.001))); }, { passive: false });
  document.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', () => {
    [yaw, pitch] = button.dataset.view.split(',').map(Number); spinning = false; document.querySelector('#spin').textContent = '自动旋转';
  }));
  document.querySelector('#spin').addEventListener('click', event => { spinning = !spinning; event.target.textContent = spinning ? '暂停旋转' : '自动旋转'; });
  document.querySelector('#reset').addEventListener('click', () => { yaw = -0.56; pitch = 0.35; zoom = 1.58; spinning = false; document.querySelector('#spin').textContent = '自动旋转'; });
  let previous = performance.now();
  function frame(now) {
    const width = Math.round(canvas.clientWidth * Math.min(devicePixelRatio, 2));
    const height = Math.round(canvas.clientHeight * Math.min(devicePixelRatio, 2));
    if (canvas.width !== width || canvas.height !== height) { canvas.width = width; canvas.height = height; gl.viewport(0, 0, width, height); }
    if (spinning && !drag) yaw += Math.min(now - previous, 100) * 0.0003;
    previous = now;
    gl.clearColor(0.069, 0.114, 0.165, 1); gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.uniform2f(cameraLocation, yaw, pitch); gl.uniform1f(zoomLocation, zoom); gl.uniform1f(aspectLocation, width / height);
    gl.drawArrays(gl.TRIANGLES, 0, vertices.length / 9);
    requestAnimationFrame(frame);
  }
  status.textContent = `最终运行时 OBJ 已加载 · ${vertices.length / 27} 三角面 · ${image.width}×${image.height} 图集 · 六向 / 旋转 / 缩放`;
  document.body.dataset.ready = 'true';
  requestAnimationFrame(frame);
})().catch(error => { document.querySelector('#status').textContent = '模型加载失败：' + error.message; document.body.dataset.ready = 'error'; });
