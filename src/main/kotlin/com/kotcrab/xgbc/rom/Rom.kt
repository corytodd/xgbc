package com.kotcrab.xgbc.rom

import com.badlogic.gdx.files.FileHandle
import com.kotcrab.xgbc.EmulatorException
import com.kotcrab.xgbc.rom.mbc.MBC
import com.kotcrab.xgbc.rom.mbc.MBC1
import com.kotcrab.xgbc.rom.mbc.RomOnly
import com.kotcrab.xgbc.toUnsignedInt

/** @author Kotcrab */
class Rom(romFile: FileHandle) {
    val rom: ByteArray = romFile.readBytes()
    lateinit var mbc: MBC

    val title: String by lazy {
        val builder = StringBuilder()
        for (addr in 0x0134..0x0142) {
            val byte = read(addr)
            if (byte.equals(0)) break
            builder.append(byte.toChar())
        }
        builder.toString()
    }

    val gameBoyColor: Boolean by lazy {
        read(0x143).equals(0x80)
    }

    val superGameBoy: Boolean by lazy {
        read(0x146).equals(0x03)
    }

    val cartridgeType: CartridgeType by lazy {
        cartridgeTypeFromByte(read(0x147))
    }

    val romSize: Int by lazy {
        val size: Int
        when (readInt(0x148)) {
            0x0 -> size = 32 * 1024
            0x1 -> size = 64 * 1024
            0x2 -> size = 128 * 1024
            0x3 -> size = 256 * 1024
            0x4 -> size = 512 * 1024
            0x5 -> size = 1024 * 1024
            0x6 -> size = 2048 * 1024
            0x52 -> size = 72 * 16 * 1024 //72 banks
            0x53 -> size = 80 * 16 * 1024
            0x54 -> size = 96 * 16 * 1024
            else -> throw EmulatorException("Unknown ROM size")
        }
        size
    }

    val ramSize: Int by lazy {
        val size: Int
        when (readInt(0x149)) {
            0x0 -> size = 0
            0x1 -> size = 2 * 1024
            0x2 -> size = 8 * 1024
            0x3 -> size = 32 * 1024
            0x4 -> size = 128 * 1024
            else -> throw EmulatorException("Unknown RAM size")
        }
        size
    }

    val destCode: Int by lazy {
        readInt(0x014A)
    }

    init {
        when (cartridgeType) {
            CartridgeType.ROM -> mbc = RomOnly(this)
            CartridgeType.ROM_MBC1 -> mbc = MBC1(this)
            CartridgeType.ROM_MBC1_RAM -> mbc = MBC1(this)
            CartridgeType.ROM_MBC1_RAM_BATT -> mbc = MBC1(this)
            else -> throw EmulatorException("Unsupported memory bank controller: $cartridgeType")
        }
    }

    fun read(addr: Int): Byte {
        return rom[addr]
    }

    private fun readInt(addr: Int): Int {
        return read(addr).toUnsignedInt()
    }
}
