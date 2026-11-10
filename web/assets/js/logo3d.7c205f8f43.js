/* TOKE · logo 3D. Negro líquido → se ilumina en verde → las 3 ondas laten (solo brillo, sin inflarse) → levitación leve.
   Three.js va incluido en el sitio (sin CDN) para cumplir la CSP. */
import * as THREE from '/assets/vendor/three.0.160.0.module.76dea8151b.js';

const stage = document.getElementById('stage');
const canvas = document.getElementById('logo3d');
const floor = stage && stage.querySelector('.floor');
const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;

if (stage && canvas) {
  try { start(); } catch (err) { stage.classList.add('nogl'); }
}

function start() {
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 0.92;
  renderer.outputColorSpace = THREE.SRGBColorSpace;

  const scene = new THREE.Scene();
  const pmrem = new THREE.PMREMGenerator(renderer);

  /* Estudio tipo render: domo gris suave + cajas de luz que dan los reflejos largos del cristal */
  const studio = new THREE.Scene();
  studio.add(new THREE.Mesh(new THREE.SphereGeometry(30, 48, 24), new THREE.MeshBasicMaterial({ side: THREE.BackSide, color: new THREE.Color(0.16, 0.18, 0.17) })));
  const softbox = (w, h, x, y, z, power) => {
    const m = new THREE.Mesh(new THREE.PlaneGeometry(w, h), new THREE.MeshBasicMaterial({ side: THREE.DoubleSide, color: new THREE.Color().setScalar(power) }));
    m.position.set(x, y, z); m.lookAt(0, 0, 0); studio.add(m);
  };
  softbox(16, 10, -9, 11, 9, 10);
  softbox(3.2, 18, 13, 2, 4, 8);
  softbox(18, 3, 0, -10, 7, 2.2);
  softbox(12, 6, 2, 13, -10, 5);
  softbox(5, 12, -14, -1, 2, 3);
  scene.environment = pmrem.fromScene(studio, 0.015).texture;

  const camera = new THREE.PerspectiveCamera(28, 1, 0.1, 100);
  camera.position.set(0, 0, 11);
  const key = new THREE.DirectionalLight(0xffffff, 0.7); key.position.set(3, 5, 6); scene.add(key);

  /* Formas del logo (coordenadas del arte oficial, y hacia abajo) */
  const pt = (x, y) => new THREE.Vector2((x - 500) / 100, -(y - 500) / 100);
  const arc = (cx, cy, r, a0, a1, n = 36) => { const o = []; for (let i = 0; i <= n; i++) { const a = a0 + (a1 - a0) * i / n; o.push(pt(cx + r * Math.cos(a), cy + r * Math.sin(a))); } return o; };
  const bodyPts = [pt(382, 350), pt(557, 350), pt(557, 632), ...arc(508.5, 632, 48.5, 0, Math.PI), pt(460, 440), pt(382, 440), ...arc(382, 395, 45, Math.PI / 2, Math.PI * 1.5)];
  const wave0 = [pt(565, 440), pt(565, 382), ...arc(565, 440, 58, -Math.PI / 2, 0)];
  const ring = (ri, ro, x0) => { const cx = 574, cy = 440; const c = (r) => Math.acos((x0 - cx) / r); return [...arc(cx, cy, ro, -c(ro), 0), ...arc(cx, cy, ri, 0, -c(ri))]; };
  const wave1 = ring(64, 87, 578);
  const wave2 = ring(108, 131, 578);

  /* Bordes muy redondeados; bevelOffset = -bevelSize mantiene la silueta original */
  const geo = (pts, bevel, depth) => {
    const g = new THREE.ExtrudeGeometry(new THREE.Shape(pts), { depth, bevelEnabled: true, bevelThickness: bevel, bevelSize: bevel, bevelOffset: -bevel, bevelSegments: 18, curveSegments: 56 });
    g.translate(0, 0, -depth / 2); g.computeVertexNormals(); return g;
  };

  const BLACK = new THREE.Color(0x07080a), GREEN = new THREE.Color(0x00C853);
  const mk = () => new THREE.MeshPhysicalMaterial({ color: BLACK.clone(), metalness: 0.1, roughness: 0.12, clearcoat: 1, clearcoatRoughness: 0.025, envMapIntensity: 1.3, sheen: 0.6, sheenRoughness: 0.35, sheenColor: new THREE.Color(0x35e08c), emissive: GREEN.clone(), emissiveIntensity: 0 });
  const mBody = mk(), mW = [mk(), mk(), mk()];

  const inner = new THREE.Group();
  inner.add(new THREE.Mesh(geo(bodyPts, 0.12, 0.3), mBody));
  inner.add(new THREE.Mesh(geo(wave0, 0.08, 0.3), mW[0]));
  inner.add(new THREE.Mesh(geo(wave1, 0.05, 0.3), mW[1]));
  inner.add(new THREE.Mesh(geo(wave2, 0.05, 0.3), mW[2]));
  const c = new THREE.Box3().setFromObject(inner).getCenter(new THREE.Vector3());
  inner.position.set(-c.x, -c.y, -c.z);
  const pivot = new THREE.Group(); pivot.add(inner); scene.add(pivot);
  pivot.rotation.set(0.1, -0.42, 0);

  function resize() {
    const w = stage.clientWidth, h = stage.clientHeight; if (!w || !h) return;
    renderer.setSize(w, h, false); camera.aspect = w / h; camera.updateProjectionMatrix();
  }
  resize(); new ResizeObserver(resize).observe(stage);

  const IGNITE_AT = 1.5, IGNITE_LEN = 1.6, BEAT = 2.0, STAGGER = 0.17, BEAT_LEN = 0.75;
  const ease = (t) => t < .5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
  const clock = new THREE.Clock();

  function frame() {
    const t = reduce ? 99 : clock.getElapsedTime();
    const k = ease(Math.min(Math.max((t - IGNITE_AT) / IGNITE_LEN, 0), 1));   /* 0 = negro líquido, 1 = verde Toke */
    [mBody, ...mW].forEach((m) => {
      m.color.lerpColors(BLACK, GREEN, k);
      m.roughness = 0.1 + 0.14 * k; m.metalness = 0.15 - 0.12 * k;
      m.envMapIntensity = 1.3 - 0.45 * k;
    });
    mBody.emissiveIntensity = 0.16 * k;
    const begin = IGNITE_AT + IGNITE_LEN + 0.2;                                /* las ondas laten solo cuando ya es verde */
    mW.forEach((m, i) => {
      let beat = 0;
      if (t > begin) { const p = ((t - begin - i * STAGGER) % BEAT) / BEAT_LEN; beat = (p >= 0 && p < 1) ? Math.sin(p * Math.PI) : 0; }
      m.emissiveIntensity = 0.16 * k + beat * 0.95;
    });

    const fall = reduce ? 0 : Math.sin(t * 1.05);                              /* levitación leve */
    pivot.position.set(0.3, fall * 0.05, 0);
    pivot.rotation.y = -0.42 + (reduce ? 0 : Math.sin(t * 0.55) * 0.045);
    pivot.rotation.x = 0.1 + (reduce ? 0 : Math.sin(t * 0.8 + 1) * 0.012);
    if (floor) floor.style.setProperty('--lift', (1 - fall * 0.05).toFixed(3));

    renderer.render(scene, camera);
    if (!reduce) requestAnimationFrame(frame);
  }
  frame();
}
