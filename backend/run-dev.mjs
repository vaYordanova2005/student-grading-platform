// Loads backend/.env (git-ignored, holds DB credentials) if present, then runs
// the Maven wrapper. Node (not a shell) so this works the same way on
// Windows/macOS/Linux without depending on bash being resolvable on PATH.
import { spawnSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const backendDir = dirname(fileURLToPath(import.meta.url));
const envPath = join(backendDir, '.env');
const env = { ...process.env };

if (existsSync(envPath)) {
  for (const line of readFileSync(envPath, 'utf8').split('\n')) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eq = trimmed.indexOf('=');
    if (eq === -1) continue;
    const key = trimmed.slice(0, eq).trim();
    let value = trimmed.slice(eq + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    env[key] = value;
  }
}

const mvnw = process.platform === 'win32' ? '.\\mvnw.cmd' : './mvnw';
const result = spawnSync(mvnw, ['spring-boot:run'], { cwd: backendDir, env, stdio: 'inherit', shell: true });
process.exit(result.status ?? 1);
