import ctypes
import json
import unittest
import uuid
import zipfile
from pathlib import Path

import release


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.saved_workspace, self.saved_root = release.WORKSPACE, release.ROOT
        release.WORKSPACE = self.saved_root / "tests" / (self._testMethodName + "-" + uuid.uuid4().hex)
        release.ROOT = release.WORKSPACE / "project"
        jar = release.WORKSPACE / release.JAR
        jar.parent.mkdir(parents=True, exist_ok=True)
        release.ROOT.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(jar, "w") as archive:
            archive.writestr(release.MIXINS, b'{"client":["LevelRendererMixin"]}')
            archive.writestr("weather-logic.class", b"unchanged event logic")
            archive.writestr("model.json", b"latest model")

    def tearDown(self):
        release.WORKSPACE, release.ROOT = self.saved_workspace, self.saved_root

    def test_no_unrelated_jar_entries_changed(self):
        payload = {release.MIXINS: b"new config", release.POLICY: b"policy", release.MIXIN: b"mixin"}
        output = release.ROOT / "staged.jar"
        delta = release.patch(release.target(release.JAR), output, payload)
        self.assertEqual(delta["modified"], [release.MIXINS])
        self.assertEqual(set(delta["added"]), {release.POLICY, release.MIXIN})
        with zipfile.ZipFile(output) as archive:
            self.assertEqual(archive.read("weather-logic.class"), b"unchanged event logic")
            self.assertEqual(archive.read("model.json"), b"latest model")

    def test_staged_archive_is_deterministic(self):
        payload = {release.MIXINS: b"new config", release.POLICY: b"policy", release.MIXIN: b"mixin"}
        first, second = release.ROOT / "first.jar", release.ROOT / "second.jar"
        release.patch(release.target(release.JAR), first, payload)
        release.patch(release.target(release.JAR), second, payload)
        self.assertEqual(first.read_bytes(), second.read_bytes())

    def test_changed_baseline_rejected_without_writes(self):
        before = release.target(release.JAR).read_bytes()
        with self.assertRaisesRegex(RuntimeError, "BASELINE_CHANGED"):
            release.check_baseline({release.JAR: "0" * 64})
        self.assertEqual(before, release.target(release.JAR).read_bytes())

    def test_outside_scope_rejected(self):
        with self.assertRaises(RuntimeError):
            release.target("../../outside.jar")

    def test_missing_target_rejected(self):
        with self.assertRaises(RuntimeError):
            release.target("missing.jar")

    def test_exact_payload_whitelist(self):
        for name in release.ENTRIES:
            path = release.ROOT / "payload" / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(name.encode())
        self.assertEqual(set(release.payload_bytes()), set(release.ENTRIES))
        (release.ROOT / "payload/unrelated.class").write_bytes(b"unexpected")
        with self.assertRaises(RuntimeError):
            release.payload_bytes()

    def test_duplicate_zip_rejected(self):
        output = release.ROOT / "duplicate.jar"
        with zipfile.ZipFile(output, "w") as archive:
            archive.writestr("duplicate", b"first")
            archive.writestr("duplicate", b"second")
        with self.assertRaises(RuntimeError):
            release.jar_map(output)

    def kernel(self):
        kernel = ctypes.WinDLL("kernel32", use_last_error=True)
        kernel.CreateFileW.argtypes = [ctypes.c_wchar_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p,
                                      ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p]
        kernel.CreateFileW.restype = ctypes.c_void_p
        kernel.CloseHandle.argtypes = [ctypes.c_void_p]
        return kernel

    def test_occupied_jar_stops_before_write(self):
        kernel = self.kernel()
        before = release.target(release.JAR).read_bytes()
        handle = kernel.CreateFileW(str(release.target(release.JAR)), 0x80000000, 0, None, 3, 0x80, None)
        self.assertNotEqual(handle, ctypes.c_void_p(-1).value)
        try:
            with self.assertRaisesRegex(RuntimeError, "LOCKED"):
                with release.locked_archive():
                    self.fail("Occupied JAR accepted")
        finally:
            kernel.CloseHandle(handle)
        self.assertEqual(before, release.target(release.JAR).read_bytes())

    def test_shared_installer_lock_stops_before_write(self):
        kernel = self.kernel()
        before = release.target(release.JAR).read_bytes()
        lock = release.target(release.JAR).with_name("." + Path(release.JAR).name + ".shared-install.lock")
        handle = kernel.CreateFileW(str(lock), 0xC0000000, 0, None, 4, 0x80, None)
        self.assertNotEqual(handle, ctypes.c_void_p(-1).value)
        try:
            with self.assertRaisesRegex(RuntimeError, "LOCKED"):
                with release.locked_archive():
                    self.fail("Shared deployment lock bypassed")
        finally:
            kernel.CloseHandle(handle)
        self.assertEqual(before, release.target(release.JAR).read_bytes())


if __name__ == "__main__":
    result = unittest.TextTestRunner(verbosity=2).run(unittest.defaultTestLoader.loadTestsFromTestCase(ReleaseTests))
    release.save(release.ROOT / "release-safety.json", {"passed": result.wasSuccessful(), "tests": result.testsRun,
                                                      "scope": "Isolated fixtures inside current project; no runtime instance files written"})
    raise SystemExit(0 if result.wasSuccessful() else 1)
