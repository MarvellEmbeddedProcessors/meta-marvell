LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0adaf0d6160d61a8467973ac69edd367"
DEPENDS = "u-boot-mkimage-native"

SRC_URI = "file://bootscript \
           file://LICENSE"

S = "${WORKDIR}/"

inherit deploy

BOOTSCRIPT = "${S}/bootscript"
FILES_${PN} += "/boot.scr"

do_mkimage () {
    uboot-mkimage -A arm -O linux -T script -C none -a 0 -e 0 \
                  -n "boot script" -d ${BOOTSCRIPT} ${S}/boot.scr
}

addtask mkimage after do_compile before do_install

do_deploy () {
    install -d ${DEPLOYDIR}
    install ${S}/boot.scr \
            ${DEPLOYDIR}/boot.scr

    cd ${DEPLOYDIR}
    mv boot.scr boot.scr-${MACHINE}-${PV}-${PR}
    ln -sf boot.scr-${MACHINE}-${PV}-${PR} boot.scr-${MACHINE}
    ln -sf boot.scr-${MACHINE}-${PV}-${PR} boot.scr

}

do_install () {
    install ${S}/boot.scr ${D}/boot.scr
}

addtask deploy after do_install before do_build

do_compile[noexec] = "1"

RPROVIDES_${PN} += "u-boot-script"

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(armada38x)"
