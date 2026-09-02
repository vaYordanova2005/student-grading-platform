import { useEffect, useRef, type RefObject } from 'react';

/**
 * Ambient network background: points that drift, link up when close, and
 * gather toward the cursor. Ported from the gamification-module project's
 * Achievements/NetworkField.jsx, retinted to Markly's green palette.
 */
type RGB = [number, number, number];

const COLORS = {
  green: [46, 122, 94] as RGB,
  greenDeep: [22, 82, 60] as RGB,
  mint: [58, 168, 130] as RGB,
  coral: [224, 122, 95] as RGB,
  gold: [214, 168, 74] as RGB,
  azure: [90, 140, 190] as RGB,
};

const DEFAULT_LINK_DIST = 58;
const POINTER_R = 68;
const POINTER_R_SQ = POINTER_R * POINTER_R;

const DEFAULT_MIN_NODES = 40;
const DEFAULT_MAX_NODES = 70;
const DEFAULT_AREA_PER_NODE = 1700;

const DEFAULT_MAX_PULSES = 20;
const SPAWN_EVERY = 1.1;
const SPAWN_BATCH = 2;

const rgba = (c: RGB, a: number) => `rgba(${c[0]},${c[1]},${c[2]},${a})`;

const pickNodeColor = (): RGB => {
  const roll = Math.random();
  if (roll > 0.88) return COLORS.coral;
  if (roll > 0.76) return COLORS.gold;
  if (roll > 0.64) return COLORS.azure;
  if (roll > 0.5) return COLORS.mint;
  if (roll > 0.28) return COLORS.greenDeep;
  return COLORS.green;
};

const pickPulseColor = (): RGB => {
  const roll = Math.random();
  if (roll > 0.75) return COLORS.coral;
  if (roll > 0.5) return COLORS.gold;
  if (roll > 0.25) return COLORS.azure;
  return COLORS.mint;
};

const prefersReducedMotion = () =>
  typeof window !== 'undefined' && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

interface Node {
  x: number;
  y: number;
  vx: number;
  vy: number;
  r: number;
  depth: number;
  phase: number;
  ix: number;
  iy: number;
  color: RGB;
}

interface Pulse {
  a: number;
  b: number;
  p: number;
  speed: number;
  color: RGB;
}

interface NetworkFieldProps {
  className?: string;
  pointerTarget?: RefObject<HTMLElement>;
  intensity?: number;
  minNodes?: number;
  maxNodes?: number;
  areaPerNode?: number;
  linkDist?: number;
  maxPulses?: number;
  centerFadeRadius?: number;
}

export function NetworkField({
  className,
  pointerTarget,
  intensity = 1,
  minNodes = DEFAULT_MIN_NODES,
  maxNodes = DEFAULT_MAX_NODES,
  areaPerNode = DEFAULT_AREA_PER_NODE,
  linkDist = DEFAULT_LINK_DIST,
  maxPulses = DEFAULT_MAX_PULSES,
  centerFadeRadius = 0,
}: NetworkFieldProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const hostRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const linkDistSq = linkDist * linkDist;
    const canvas = canvasRef.current;
    const host = hostRef.current;
    if (!canvas || !host) return undefined;

    const ctx = canvas.getContext('2d', { alpha: true });
    if (!ctx) return undefined;

    const reduce = prefersReducedMotion();
    const rgbaI = (c: RGB, a: number) => rgba(c, Math.min(1, a * intensity));

    let width = 0;
    let height = 0;
    let nodes: Node[] = [];
    let pulses: Pulse[] = [];
    let spawnAccumulator = 0;
    let frameId: number | null = null;
    let running = false;
    let lastFrame = 0;
    let pointerWasActive = false;

    const pointer = { x: -9999, y: -9999, tx: -9999, ty: -9999, active: false };

    const centerFadeFor = (x: number, y: number) => {
      if (centerFadeRadius <= 0) return 1;
      const dx = x - width / 2;
      const dy = y - height / 2;
      return Math.min(1, Math.sqrt(dx * dx + dy * dy) / centerFadeRadius);
    };

    // Nodes only ever link within `linkDist`, so bucketing them into a grid
    // sized to that distance turns the naive O(n^2) pair scan into ~O(n):
    // for each node we only visit its own cell and the 8 neighbors.
    const cellSize = Math.max(1, linkDist);
    const cellKey = (cx: number, cy: number) => `${cx},${cy}`;

    const buildGrid = () => {
      const grid = new Map<string, number[]>();
      for (let i = 0; i < nodes.length; i += 1) {
        const n = nodes[i];
        const key = cellKey(Math.floor(n.x / cellSize), Math.floor(n.y / cellSize));
        const bucket = grid.get(key);
        if (bucket) bucket.push(i);
        else grid.set(key, [i]);
      }
      return grid;
    };

    const forEachLinkPair = (grid: Map<string, number[]>, visit: (a: Node, b: Node, d2: number) => void) => {
      for (let i = 0; i < nodes.length; i += 1) {
        const a = nodes[i];
        const cx = Math.floor(a.x / cellSize);
        const cy = Math.floor(a.y / cellSize);
        for (let ox = -1; ox <= 1; ox += 1) {
          for (let oy = -1; oy <= 1; oy += 1) {
            const bucket = grid.get(cellKey(cx + ox, cy + oy));
            if (!bucket) continue;
            for (const j of bucket) {
              if (j <= i) continue;
              const b = nodes[j];
              const dx = a.x - b.x;
              const dy = a.y - b.y;
              const d2 = dx * dx + dy * dy;
              if (d2 < linkDistSq) visit(a, b, d2);
            }
          }
        }
      }
    };

    const buildNodes = () => {
      const target = Math.min(maxNodes, Math.max(minNodes, Math.round((width * height) / areaPerNode)));
      nodes = [];
      for (let i = 0; i < target; i += 1) {
        const depth = 0.4 + Math.random() * 0.6;
        nodes.push({
          x: Math.random() * width,
          y: Math.random() * height,
          vx: (Math.random() - 0.5) * 0.22,
          vy: (Math.random() - 0.5) * 0.22,
          r: (0.9 + Math.random() * 1.7) * depth,
          depth,
          phase: Math.random() * Math.PI * 2,
          ix: 0,
          iy: 0,
          color: pickNodeColor(),
        });
      }
      pulses = [];
      spawnAccumulator = 0;
    };

    const resize = () => {
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      const rect = host.getBoundingClientRect();
      width = rect.width;
      height = rect.height;
      if (!width || !height) return;
      canvas.width = Math.round(width * dpr);
      canvas.height = Math.round(height * dpr);
      canvas.style.width = `${width}px`;
      canvas.style.height = `${height}px`;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      buildNodes();
    };

    const trySpawnPulse = () => {
      if (pulses.length >= maxPulses || nodes.length < 2) return;
      const from = (Math.random() * nodes.length) | 0;
      const a = nodes[from];
      let best = -1;
      let bestDistance = linkDistSq;
      for (let k = 0; k < 6; k += 1) {
        const to = (Math.random() * nodes.length) | 0;
        if (to === from) continue;
        const b = nodes[to];
        const dx = a.x - b.x;
        const dy = a.y - b.y;
        const d2 = dx * dx + dy * dy;
        if (d2 < bestDistance) {
          bestDistance = d2;
          best = to;
        }
      }
      if (best >= 0) {
        pulses.push({
          a: from,
          b: best,
          p: 0,
          speed: 0.006 + Math.random() * 0.009,
          color: pickPulseColor(),
        });
      }
    };

    const drawStatic = () => {
      ctx.clearRect(0, 0, width, height);
      ctx.lineWidth = 1 + (intensity - 1) * 0.3;
      forEachLinkPair(buildGrid(), (a, b, d2) => {
        const d = Math.sqrt(d2);
        const fade = centerFadeFor((a.x + b.x) / 2, (a.y + b.y) / 2);
        ctx.strokeStyle = rgbaI(COLORS.greenDeep, (1 - d / linkDist) * 0.14 * fade);
        ctx.beginPath();
        ctx.moveTo(a.x, a.y);
        ctx.lineTo(b.x, b.y);
        ctx.stroke();
      });
      for (let i = 0; i < nodes.length; i += 1) {
        const n = nodes[i];
        ctx.fillStyle = rgbaI(n.color, (0.3 * n.depth + 0.12) * centerFadeFor(n.x, n.y));
        ctx.beginPath();
        ctx.arc(n.x, n.y, n.r, 0, Math.PI * 2);
        ctx.fill();
      }
    };

    const frame = (time: number) => {
      if (!running) return;
      const delta = Math.min(48, time - lastFrame);
      lastFrame = time;
      const speed = delta / 16.6667;

      if (pointer.active) {
        if (pointer.x < -9000) {
          pointer.x = pointer.tx;
          pointer.y = pointer.ty;
        }
        pointer.x += (pointer.tx - pointer.x) * 0.08;
        pointer.y += (pointer.ty - pointer.y) * 0.08;
      } else {
        pointer.x += (-9999 - pointer.x) * 0.02;
      }

      ctx.clearRect(0, 0, width, height);

      if (pointerWasActive && !pointer.active) {
        const rx = pointer.x;
        const ry = pointer.y;
        for (let i = 0; i < nodes.length; i += 1) {
          const n = nodes[i];
          const dx = n.x - rx;
          const dy = n.y - ry;
          const d2 = dx * dx + dy * dy;
          if (d2 < POINTER_R_SQ) {
            const d = Math.sqrt(d2) || 1;
            const force = (1 - d / POINTER_R) * 12;
            n.ix += (dx / d) * force;
            n.iy += (dy / d) * force;
          }
        }
      }
      pointerWasActive = pointer.active;

      for (let i = 0; i < nodes.length; i += 1) {
        const n = nodes[i];
        n.x += (n.vx * n.depth + n.ix) * speed;
        n.y += (n.vy * n.depth + n.iy) * speed;
        n.ix *= Math.pow(0.91, speed);
        n.iy *= Math.pow(0.91, speed);

        if (pointer.active) {
          const dx = pointer.x - n.x;
          const dy = pointer.y - n.y;
          const d2 = dx * dx + dy * dy;
          if (d2 < POINTER_R_SQ && d2 > 1) {
            const f = (1 - d2 / POINTER_R_SQ) * 0.016 * n.depth;
            n.x += dx * f;
            n.y += dy * f;
          }
        }

        if (n.x < -20) n.x = width + 20;
        else if (n.x > width + 20) n.x = -20;
        if (n.y < -20) n.y = height + 20;
        else if (n.y > height + 20) n.y = -20;
        n.phase += 0.015 * speed;
      }

      ctx.lineWidth = 1 + (intensity - 1) * 0.3;
      forEachLinkPair(buildGrid(), (a, b, d2) => {
        const d = Math.sqrt(d2);
        let alpha = (1 - d / linkDist) * 0.16;
        if (pointer.active) {
          const mx = (a.x + b.x) * 0.5 - pointer.x;
          const my = (a.y + b.y) * 0.5 - pointer.y;
          if (mx * mx + my * my < POINTER_R_SQ) {
            alpha += 0.14 * (1 - d / linkDist);
          }
        }
        alpha *= centerFadeFor((a.x + b.x) / 2, (a.y + b.y) / 2);
        ctx.strokeStyle = rgbaI(COLORS.greenDeep, alpha);
        ctx.beginPath();
        ctx.moveTo(a.x, a.y);
        ctx.lineTo(b.x, b.y);
        ctx.stroke();
      });

      for (let i = 0; i < nodes.length; i += 1) {
        const n = nodes[i];
        const breathe = 0.55 + 0.45 * (0.5 + 0.5 * Math.sin(n.phase));
        let alpha = 0.2 + 0.28 * breathe * n.depth;
        if (pointer.active) {
          const dx = n.x - pointer.x;
          const dy = n.y - pointer.y;
          if (dx * dx + dy * dy < POINTER_R_SQ) alpha = Math.min(0.9, alpha + 0.35);
        }
        alpha *= centerFadeFor(n.x, n.y);
        ctx.fillStyle = rgbaI(n.color, alpha);
        ctx.beginPath();
        ctx.arc(n.x, n.y, n.r, 0, Math.PI * 2);
        ctx.fill();
      }

      spawnAccumulator += speed;
      if (spawnAccumulator >= SPAWN_EVERY) {
        spawnAccumulator = 0;
        for (let s = 0; s < SPAWN_BATCH; s += 1) trySpawnPulse();
      }

      for (let i = pulses.length - 1; i >= 0; i -= 1) {
        const p = pulses[i];
        p.p += p.speed * speed;
        const a = nodes[p.a];
        const b = nodes[p.b];
        if (!a || !b) {
          pulses.splice(i, 1);
          continue;
        }
        const dx = b.x - a.x;
        const dy = b.y - a.y;
        if (p.p >= 1 || dx * dx + dy * dy > linkDistSq * 3) {
          pulses.splice(i, 1);
          continue;
        }

        const x = a.x + dx * p.p;
        const y = a.y + dy * p.p;
        const fade = Math.sin(Math.PI * p.p);
        const tail = 0.1;
        const bx = a.x + dx * Math.max(0, p.p - tail);
        const by = a.y + dy * Math.max(0, p.p - tail);

        ctx.strokeStyle = rgbaI(p.color, 0.5 * fade);
        ctx.lineWidth = 1.4;
        ctx.beginPath();
        ctx.moveTo(bx, by);
        ctx.lineTo(x, y);
        ctx.stroke();
        ctx.lineWidth = 1;

        const glow = ctx.createRadialGradient(x, y, 0, x, y, 7);
        glow.addColorStop(0, rgbaI(p.color, 0.85 * fade));
        glow.addColorStop(1, rgba(p.color, 0));
        ctx.fillStyle = glow;
        ctx.beginPath();
        ctx.arc(x, y, 7, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = rgbaI(p.color, Math.min(1, 0.9 * fade + 0.1));
        ctx.beginPath();
        ctx.arc(x, y, 1.9, 0, Math.PI * 2);
        ctx.fill();
      }

      frameId = requestAnimationFrame(frame);
    };

    const start = () => {
      if (running) return;
      running = true;
      lastFrame = performance.now();
      frameId = requestAnimationFrame(frame);
    };

    const stop = () => {
      running = false;
      if (frameId) cancelAnimationFrame(frameId);
      frameId = null;
    };

    const onPointerMove = (event: PointerEvent) => {
      const rect = host.getBoundingClientRect();
      pointer.tx = event.clientX - rect.left;
      pointer.ty = event.clientY - rect.top;
      pointer.active = pointer.tx >= 0 && pointer.tx <= width && pointer.ty >= 0 && pointer.ty <= height;
    };

    const onPointerLeave = () => {
      pointer.active = false;
    };

    resize();

    // Falls back to `window` rather than `host`: the host is typically
    // `pointer-events: none` (so it doesn't block clicks on content drawn
    // above it), which means it never receives pointer events itself. The
    // original card usage instead passes the visible card as pointerTarget;
    // for a full-page background there's no equivalent visible container,
    // so window-level tracking is the closest match.
    const pointerHost: EventTarget = pointerTarget?.current ?? window;

    if (reduce) {
      drawStatic();
    } else {
      pointerHost.addEventListener('pointermove', onPointerMove as EventListener, { passive: true });
      pointerHost.addEventListener('pointerleave', onPointerLeave as EventListener, { passive: true });
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (reduce) return;
          if (entry.isIntersecting) start();
          else stop();
        });
      },
      { threshold: 0.02 }
    );
    observer.observe(host);

    let resizeFrame: number | null = null;
    const onResize = () => {
      if (resizeFrame) cancelAnimationFrame(resizeFrame);
      resizeFrame = requestAnimationFrame(() => {
        resize();
        if (reduce) drawStatic();
      });
    };
    window.addEventListener('resize', onResize);

    const resizeObserver = new ResizeObserver(onResize);
    resizeObserver.observe(host);

    return () => {
      stop();
      observer.disconnect();
      resizeObserver.disconnect();
      window.removeEventListener('resize', onResize);
      pointerHost.removeEventListener('pointermove', onPointerMove as EventListener);
      pointerHost.removeEventListener('pointerleave', onPointerLeave as EventListener);
      if (resizeFrame) cancelAnimationFrame(resizeFrame);
    };
  }, [intensity, minNodes, maxNodes, areaPerNode, linkDist, maxPulses, centerFadeRadius, pointerTarget]);

  return (
    <div ref={hostRef} className={className} aria-hidden="true">
      <canvas ref={canvasRef} />
    </div>
  );
}
