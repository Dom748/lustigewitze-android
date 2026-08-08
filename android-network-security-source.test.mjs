import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import test from "node:test";
import { X509Certificate } from "node:crypto";

const root = process.cwd();
const manifestPath = path.join(root, "app/src/main/AndroidManifest.xml");
const configPath = path.join(root, "app/src/main/res/xml/network_security_config.xml");
const certificatePath = path.join(root, "app/src/main/res/raw/isrg_root_x1.pem");
const certificateX2Path = path.join(root, "app/src/main/res/raw/isrg_root_x2.pem");

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

test("Android 5 trusts the current Let's Encrypt YE chain through bundled ISRG Root X2", () => {
  assert.ok(existsSync(certificateX2Path), "ISRG Root X2 certificate must be bundled for the current API chain");

  const config = readFileSync(configPath, "utf8");
  assert.match(config, /<certificates src="@raw\/isrg_root_x2"\s*\/>/);

  const certificate = new X509Certificate(readFileSync(certificateX2Path));
  assert.match(certificate.subject, /CN=ISRG Root X2/);
  assert.equal(
    certificate.fingerprint256,
    "69:72:9B:8E:15:A8:6E:FC:17:7A:57:AF:B7:17:1D:FC:64:AD:D2:8C:2F:CA:8C:F1:50:7E:34:45:3C:CB:14:70",
  );
});
