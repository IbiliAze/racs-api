#!/usr/bin/env node
// Mints a long-lived service JWT for racs-api (TRACS) machine-to-machine auth.
//
// The signing algorithm must match what racs-api itself uses: jjwt's
// Keys.hmacShaKeyFor picks HS512 for secrets >= 64 bytes, HS384 >= 48, HS256 >= 32.
//
// Usage (run wherever the target environment's JWT_SECRET lives):
//   JWT_SECRET='<secret>' node mint-service-token.js
//   JWT_SECRET='<secret>' AUTHORITIES=CREATE_CARD,READ_CARD YEARS=5 node mint-service-token.js
//
// Falls back to reading JWT_SECRET from ./.env (local dev).

const { createHmac, randomUUID } = require('crypto');
const { readFileSync, existsSync } = require('fs');
const { join } = require('path');

let secret = process.env.JWT_SECRET;
if (!secret) {
  const envPath = join(__dirname, '.env');
  if (existsSync(envPath)) {
    const match = readFileSync(envPath, 'utf8').match(/^JWT_SECRET=(.*)$/m);
    if (match) secret = match[1].trim().replace(/^["']|["']$/g, '');
  }
}
if (!secret) throw new Error('Set JWT_SECRET env var (or add it to ./.env)');

const bytes = Buffer.byteLength(secret);
const [alg, sha] = bytes >= 64 ? ['HS512', 'sha512'] : bytes >= 48 ? ['HS384', 'sha384'] : ['HS256', 'sha256'];
if (bytes < 32) throw new Error(`Secret too short (${bytes} bytes); racs-api requires >= 32`);

const authorities = (process.env.AUTHORITIES || 'CREATE_CARD').split(',').map(s => s.trim());
// exp is required (Jwt.isExpired() NPEs without it) — default to 100y as "never expires"
const years = Number(process.env.YEARS || 100);

const now = Math.floor(Date.now() / 1000);
const payload = {
  sub: randomUUID(),
  email: 'cardops-service',
  firstName: 'CardOps',
  lastName: 'Service',
  authorities,
  iat: now,
  exp: now + years * 365 * 24 * 60 * 60,
};

const b64url = obj => Buffer.from(JSON.stringify(obj)).toString('base64url');
const signingInput = `${b64url({ alg, typ: 'JWT' })}.${b64url(payload)}`;
const token = `${signingInput}.${createHmac(sha, secret).update(signingInput).digest('base64url')}`;

console.log(token);
console.error(`\nalg: ${alg} | authorities: ${authorities.join(', ')} | expires: ${new Date(payload.exp * 1000).toISOString()}`);
