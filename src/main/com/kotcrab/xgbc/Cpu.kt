package com.kotcrab.xgbc

/** @author Kotcrab */
class Cpu(private val emulator: Emulator) {
    companion object {
        const val REG_A = 0
        const val REG_F = 1
        const val REG_B = 2
        const val REG_C = 3
        const val REG_D = 4
        const val REG_E = 5
        const val REG_H = 6
        const val REG_L = 7

        const val REG_AF = 0
        const val REG_BC = 2
        const val REG_DE = 4
        const val REG_HL = 6
    }

    var sp: Int = 0 //stack pointer
    var pc: Int = 0 //program counter
    private val regs: ByteArray = ByteArray(8)

    val op = arrayOfNulls<Instr>(256)
    val extOp = arrayOfNulls<Instr>(256)

    private val opProc = OpCodesProcessor(emulator)

    init {
        generateOpCodes(emulator, opProc, op)
        generateExtOpCodes(opProc, extOp)
    }

    fun readReg(reg: Int): Byte {
        return regs[reg]
    }

    fun writeReg(reg: Int, value: Byte) {
        regs[reg] = value
    }

    fun readReg16(reg: Int): Int {
        val r1 = readReg(reg).toInt() and 0xFF
        val r2 = readReg(reg + 1).toInt() and 0xFF

        return ((r1 shl 8) + r2)
    }

    fun writeReg16(reg: Int, value: Int) {
        writeReg(reg, (value ushr 8).toByte())
        writeReg(reg + 1, (value).toByte())
    }
}

open class Instr(val len: Int,
                 val cycles: Int,
                 val name: String,
                 val op: () -> Any?)

class CondInstr(len: Int,
                cycles: Int,
                val cyclesIfActionNotTaken: Int,
                name: String,
                op: () -> Boolean) : Instr(len, cycles, name, op)
