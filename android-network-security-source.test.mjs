import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import test from "node:test";
import { X509Certificate } from "node:crypto";

const root = process.cwd();
const manifestPath = path.join(root, "app/src/main/AndroidManifest.xml");
const configPath = path.join(root, "app/src/main/res/xml/network_security_config.xml");
const certificatePath = path.join(root, "app/src/main/res/raw/isrg_root_x1.pem");

test("Android trusts the API chain through bundled ISRG Root X1 without disabling TLS verification", () => {
  const manifest = readFileSync(manifestPath, "utf8");
  assert.match(manifest, /android:networkSecurityConfig="@xml\/network_security_config"/);
  assert.ok(existsSync(configPath), "network security config must exist");
  assert.ok(existsSync(certificatePath), "ISRG Root X1 certificate must be bundled");

  const config = readFileSync(configPath, "utf8");
  assert.match(config, /<certificates src="system"\s*\/>/);
  assert.match(config, /<certificates src="@raw\/isrg_root_x1"\s*\/>/);
  assert.doesNotMatch(config, /cleartextTrafficPermitted="true"/);
  assert.doesNotMatch(config, /overridePins="true"/);

  const certificate = new X509Certificate(readFileSync(certificatePath));
  assert.match(certificate.subject, /CN=ISRG Root X1/);
  assert.equal(
    certificate.fingerprint256,
    "96:BC:EC:06:26:49:76:F3:74:60:77:9A:CF:28:C5:A7:CF:E8:A3:C0:AA:E1:1A:8F:FC:EE:05:C0:BD:DF:08:C6",
  );
});
