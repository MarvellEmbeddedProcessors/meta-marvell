require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot for Marvell Armada 38x"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://README;md5=90fb5e9af37e63370e37e9a9178cc427"

PROVIDES += "u-boot"

SRC_URI = " \
    git://git@github.com/MarvellEmbeddedProcessors/u-boot-marvell.git;branch=${SRCBRANCH_uboot};protocol=https;name=uboot;destsuffix=uboot \
    file://0001-armv7-config.mk-Enable-hard-float-support.patch;name=uboot \
    file://0002-Revert-bin_header_debug-add-implementation-of-C-libr.patch;name=uboot \
    git://git@github.com/MarvellEmbeddedProcessors/mv-ddr-marvell.git;branch=${SRCBRANCH_mv-ddr};protocol=https;name=mv-ddr;destsuffix=mv-ddr \
"

SRCREV_uboot = "ac75ba58edb9fd8e71a38d5d09f8a6bb4f56d71d"
SRCBRANCH_uboot = "u-boot-2013.01-armada-17.06"

SRCREV_mv-ddr = "a55312d36ebf97710bcdcba6f12fd0a71b37ce76"
SRCBRANCH_mv-ddr = "mv_ddr-armada-17.06"

SRCREV_FORMAT = "uboot"

PV .= "+git${SRCPV}"

S = "${WORKDIR}/uboot"
B = "${S}"

do_compile () {
    unset LDFLAGS
    unset CFLAGS
    unset CPPFLAGS

    export CROSS_COMPILE=${TARGET_PREFIX}
    export CROSS_COMPILE_BH=${TARGET_PREFIX}
    export USE_PRIVATE_LIBGCC=${STAGING_LIBDIR}/${TARGET_SYS}/`$CC -dumpversion`

    if [ ! -e ${B}/.scmversion -a ! -e ${S}/.scmversion ]
    then
        echo ${UBOOT_LOCALVERSION} > ${B}/.scmversion
        echo ${UBOOT_LOCALVERSION} > ${S}/.scmversion
    fi

    export CROSS_COMPILE=${TARGET_PREFIX}

    if [ "${UBOOT_MARVELL_MACHINE}" = "armada_38x_clearfog" ]
    then
        export CROSS_COMPILE_BH=${TARGET_PREFIX}
        perl build.pl -f mmc -b ${UBOOT_MARVELL_MACHINE} -v "yocto" -c -d 3 -m 3 -D ${WORKDIR}/mv-ddr
        cp -f u-boot-a38x-yocto-mmc.bin u-boot.mmc
    else
        export CROSS_COMPILE_BH=${TARGET_PREFIX}
        perl build.pl -f spi -b ${UBOOT_MARVELL_MACHINE} -v "yocto" -i spi -c -d 3 -m 3 -D ${WORKDIR}/mv-ddr
        cp -f u-boot-a38x-yocto-spi.bin u-boot.bin
    fi
}

COMPATIBLE_MACHINE = "(armada38x)"

PACKAGE_ARCH = "${MACHINE_ARCH}"
