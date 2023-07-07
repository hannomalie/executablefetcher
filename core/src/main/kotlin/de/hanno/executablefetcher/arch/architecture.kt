package de.hanno.executablefetcher.arch

import de.hanno.executablefetcher.arch.Architecture.*


private val archIdentifier = System.getProperty("os.arch")
val currentArchitecture = archIdentifier.toArchitecture()

// This code is translated from
// https://github.com/trustin/os-maven-plugin/blob/master/src/main/java/kr/motd/maven/os/Detector.java
fun String.toArchitecture(): Architecture = normalize().let { value ->
    when {
        value.matches("^(x8664|amd64|ia32e|em64t|x64)$".toRegex()) -> x86_64
        value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$".toRegex()) -> x86_32
        value.matches("^(ia64w?|itanium64)$".toRegex()) -> itanium_64
        "ia64n" == value -> itanium_32
        value.matches("^(sparc|sparc32)$".toRegex()) -> sparc_32
        value.matches("^(sparcv9|sparc64)$".toRegex()) -> sparc_64
        value.matches("^(arm32)$".toRegex()) -> arm_32
        value.matches("^(arm64)$".toRegex()) -> arm_64
        "aarch64" == value -> aarch_64
        value.matches("^(mips|mips32)$".toRegex()) -> mips_32
        value.matches("^(mipsel|mips32el)$".toRegex()) -> mipsel_32
        "mips64" == value -> mips_64
        "mips64el" == value -> mipsel_64
        value.matches("^(ppc|ppc32)$".toRegex()) -> ppc_32
        value.matches("^(ppcle|ppc32le)$".toRegex()) -> ppcle_32
        "ppc64" == value -> ppc_64
        "ppc64le" == value -> ppcle_64
        "s390" == value -> s390_32
        "s390x" == value -> s390_64
        value.matches("^(riscv|riscv32)$".toRegex()) -> riscv
        "riscv64" == value -> riscv64
        "e2k" == value -> e2k
        "loongarch64" == value -> loongarch_64
        else -> Unknown(value)
    }
}

private fun String?.normalize(): String = this?.lowercase()?.replace("[^a-z0-9]+".toRegex(), "") ?: ""

/**
 *
 */
sealed interface Architecture {
    object x86_64: Architecture
    object x86_32: Architecture
    object itanium_64: Architecture
    object itanium_32: Architecture
    object sparc_32: Architecture
    object sparc_64: Architecture
    object arm_32: Architecture
    object arm_64: Architecture
    object aarch_64: Architecture
    object mips_32: Architecture
    object mipsel_32: Architecture
    object mips_64: Architecture
    object mipsel_64: Architecture
    object ppc_32: Architecture
    object ppcle_32: Architecture
    object ppc_64: Architecture
    object ppcle_64: Architecture
    object s390_32: Architecture
    object s390_64: Architecture
    object riscv: Architecture
    object riscv64: Architecture
    object e2k: Architecture
    object loongarch_64: Architecture
    data class Unknown(val rawIdentifier: String): Architecture
}

/**
 * These identifiers don't follow a strict standard, because there is none.
 * Instead, the initial values are taken from the os-maven-plugin as linked above.
 * Some changes were made because it's more common to represent for example
 * x86_64 with amd64 or arm32 as arm and arm_64 as arm64.
 * Whenever that doesn't fit your needs, think about using a manually instantiated
 * instance of the Unknown Architecture class with your preferred identifier.
 * These identifiers can be used wherever a string representation of the architecture
 * is needed, for example in urls to download executables for certain architectures.
 */
val Architecture.identifier: String
    get() = when(this) {
        aarch_64 -> "aarch_64"
        arm_32 -> "arm"
        arm_64 -> "arm64"
        e2k -> "e2k"
        itanium_32 -> "itanium_32"
        itanium_64 -> "itanium_64"
        loongarch_64 -> "loongarch_64"
        mips_32 -> "mips_32"
        mips_64 -> "mips_64"
        mipsel_32 -> "mipsel_32"
        mipsel_64 -> "mipsel_64"
        ppc_32 -> "ppc_32"
        ppc_64 -> "ppc_64"
        ppcle_32 -> "ppcle_32"
        ppcle_64 -> "ppcle_64"
        riscv -> "riscv"
        riscv64 -> "riscv64"
        s390_32 -> "s390_32"
        s390_64 -> "s390_64"
        sparc_32 -> "sparc_32"
        sparc_64 -> "sparc_64"
        x86_32 -> "x86_32"
        x86_64 -> "amd64"
        is Unknown -> rawIdentifier
    }