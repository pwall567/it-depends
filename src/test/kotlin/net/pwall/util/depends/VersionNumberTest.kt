@file:Suppress("NO_CONSTRUCTOR")
package net.pwall.util.depends

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.expect

class VersionNumberTest {

    @Test fun `should create VersionNumber`() {
        val a = VersionNumber.of("0.1")
        assertFalse(a.rawFormat)
        expect("") { a.rawString }
        expect(0) { a.major }
        expect(1) { a.minor }
        assertNull(a.patch)
        assertFalse(a.snapshot)
    }

    @Test fun `should create VersionNumber with patch`() {
        val a = VersionNumber.of("0.10.5")
        assertFalse(a.rawFormat)
        expect("") { a.rawString }
        expect(0) { a.major }
        expect(10) { a.minor }
        expect(5) { a.patch }
        assertFalse(a.snapshot)
    }

    @Test fun `should create VersionNumber with SNAPSHOT`() {
        val a = VersionNumber.of("2.12-SNAPSHOT")
        assertFalse(a.rawFormat)
        expect("") { a.rawString }
        expect(2) { a.major }
        expect(12) { a.minor }
        assertNull(a.patch)
        assertTrue(a.snapshot)
    }

    @Test fun `should create VersionNumber with patch and SNAPSHOT`() {
        val a = VersionNumber.of("9.99.3-SNAPSHOT")
        assertFalse(a.rawFormat)
        expect("") { a.rawString }
        expect(9) { a.major }
        expect(99) { a.minor }
        expect(3) { a.patch }
        assertTrue(a.snapshot)
    }

    @Test fun `should create VersionNumber in raw format`() {
        val a = VersionNumber.of("unstructured")
        assertTrue(a.rawFormat)
        expect("unstructured") { a.rawString }
    }

    @Test fun `should compare version numbers`() {
        assertTrue { VersionNumber.of("0.1") < VersionNumber.of("0.2") }
        assertTrue { VersionNumber.of("0.2") < VersionNumber.of("0.10") }
        assertTrue { VersionNumber.of("0.2") < VersionNumber.of("0.2.0") }
        assertTrue { VersionNumber.of("0.2-SNAPSHOT") < VersionNumber.of("0.2") }
    }

}
