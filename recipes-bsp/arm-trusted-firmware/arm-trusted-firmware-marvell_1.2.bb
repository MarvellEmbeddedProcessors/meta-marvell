DESCRIPTION = "ARM Trusted Firmware"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://license.md;md5=829bdeb34c1d9044f393d5a16c068371"

DEPENDS = " \
    ${@bb.utils.contains("MACHINE_FEATURES", "edk-efi", "edk-marvell", "virtual/bootloader",d)} \
"

DEPENDS_append_armada37xx = "lib32-gcc-cross-arm unzip-native"

PROVIDES += "arm-trusted-firmware"

inherit deploy

S = "${WORKDIR}/atf"

SRC_URI = " \
    git://git@github.com/MarvellEmbeddedProcessors/atf-marvell.git;branch=${SRCBRANCH_atf};protocol=https;name=atf;destsuffix=atf \
    git://git@github.com/MarvellEmbeddedProcessors/mv-ddr-marvell.git;branch=${SRCBRANCH_mv-ddr};protocol=https;name=mv-ddr;destsuffix=mv-ddr \
    git://git@github.com/MarvellEmbeddedProcessors/binaries-marvell.git;branch=${SRCBRANCH_bin-marvell};protocol=ssh;name=bin-marvell;destsuffix=bin-marvell \
"

SRCREV_atf = "19e848bca9e9338c92a9fd32e780a604ba019de0"
SRCBRANCH_atf = "atf-v1.2-armada-17.02"

SRCREV_mv-ddr = "42da6dacf1b62e51d0807feee1f5e186a16f084b"
SRCBRANCH_mv-ddr = "mv_ddr-armada-17.02"

SRCREV_bin-marvell = "c00864f28553edd4cc7bc7ebb166b9a60a04a1af"
SRCBRANCH_bin-marvell = "binaries-marvell"

SRCREV_FORMAT = "atf"

PV .= "+git${SRCPV}"

# requires CROSS_COMPILE set by hand as there is no configure script
export CROSS_COMPILE="${TARGET_PREFIX}"

# Avoid messing up with build flags
CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"

do_configure[noexec] = "1"

python() {
    atfplatform = d.getVar("ARM_TRUSTED_FIRMWARE_PLATFORM", False)
    if not atfplatform:
        raise bb.parse.SkipPackage("ARM_TRUSTED_FIRMWARE_PLATFORM must be " \
                                   "set in the %s machine configuration." \
                                   % d.getVar("MACHINE", True))
}

python() {
    machine_features = d.getVar("MACHINE_FEATURES", True).split()
    if "edk-efi" in machine_features:
        d.setVar("BL33_IMAGE", "edk-efi.fd")
    else:
        d.setVar("BL33_IMAGE", "u-boot.bin")
}

EXTRA_OEMAKE += " \
    BL33=${DEPLOY_DIR_IMAGE}/${BL33_IMAGE} \
    PLAT=${ARM_TRUSTED_FIRMWARE_PLATFORM} \
    MV_DDR_PATH=${WORKDIR}/mv-ddr \
    USE_COHERENT_MEM=0 \
    LOG_LEVEL=20 \
"

# ARM 32-bit cross compiler, which is required by building WTMI image for CM3.
export CROSS_CM3 = "${STAGING_BINDIR_NATIVE}/arm-marvellmllib32-linux-gnueabi/arm-marvellmllib32-linux-gnueabi-"

# Export path to binary file
export SCP_BL2="${WORKDIR}/bin-marvell/RTOSDemo-cm3.bin"

do_compile() {
	oe_runmake all fip
}

do_install[noexec] = "1"

do_deploy() {
	install -d ${DEPLOYDIR}
	install -m 0644 ${S}/build/${ARM_TRUSTED_FIRMWARE_PLATFORM}/release/flash-image.bin ${DEPLOYDIR}/flash-image.bin-${MACHINE}-${PV}-${PR}
	ln -sf flash-image.bin-${MACHINE}-${PV}-${PR} ${DEPLOYDIR}/flash-image.bin-${MACHINE}
	ln -sf flash-image.bin-${MACHINE}-${PV}-${PR} ${DEPLOYDIR}/flash-image.bin
}
addtask deploy before do_build after do_compile

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(armada70xx|armada80xx|armada37xx)"
