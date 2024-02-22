package rars.custom.callingconvention

import rars.custom.ScopeAware
import rars.riscv.hardware.Memory
import rars.riscv.hardware.Register
import rars.riscv.hardware.RegisterAccessNotice
import rars.riscv.hardware.RegisterFile
import java.util.Observer
import java.util.Observable

import kotlin.collections.ArrayDeque


class CallingConventionChecker: ScopeAware, Observer {
    val exceptionList: MutableList<CallingConventionErrorType> = mutableListOf()

    private val a0Status = ArgumentRegisterStatus("a0", 0)
    private val a1Status = ArgumentRegisterStatus("a1", 0)
    private val a2Status = ArgumentRegisterStatus("a2", 0)
    private val a3Status = ArgumentRegisterStatus("a3", 0)
    private val a4Status = ArgumentRegisterStatus("a4", 0)
    private val a5Status = ArgumentRegisterStatus("a5", 0)
    private val a6Status = ArgumentRegisterStatus("a6", 0)
    private val a7Status = ArgumentRegisterStatus("a7", 0)

    private var currentDepth = 0;

    private var registerStatusMap: Map<String, RegisterStatus> = initRegisterStatusMap().also {
        it.keys.forEach {regName -> RegisterFile.getRegister(regName).addObserver(this) }
    }

    private fun initRegisterStatusMap(): Map<String, RegisterStatus> =  mapOf(
            "s0"  to CalleeSaveRegisterStatus("s0", currentDepth),
            "s1"  to CalleeSaveRegisterStatus("s1", currentDepth),
            "s2"  to CalleeSaveRegisterStatus("s2", currentDepth),
            "s3"  to CalleeSaveRegisterStatus("s3", currentDepth),
            "s4"  to CalleeSaveRegisterStatus("s4", currentDepth),
            "s5"  to CalleeSaveRegisterStatus("s5", currentDepth),
            "s6"  to CalleeSaveRegisterStatus("s6", currentDepth),
            "s7"  to CalleeSaveRegisterStatus("s7", currentDepth),
            "s8"  to CalleeSaveRegisterStatus("s8", currentDepth),
            "s9"  to CalleeSaveRegisterStatus("s9", currentDepth),
            "s10" to CalleeSaveRegisterStatus("s10", currentDepth),
            "s11" to CalleeSaveRegisterStatus("s11", currentDepth),
            "t0" to CallerSaveRegisterStatus("t0", currentDepth),
            "t1" to CallerSaveRegisterStatus("t1", currentDepth),
            "t2" to CallerSaveRegisterStatus("t2", currentDepth),
            "t3" to CallerSaveRegisterStatus("t3", currentDepth),
            "t4" to CallerSaveRegisterStatus("t4", currentDepth),
            "t5" to CallerSaveRegisterStatus("t5", currentDepth),
            "t6" to CallerSaveRegisterStatus("t6", currentDepth),
            "a0" to a0Status,
            "a1" to a1Status,
            "a2" to a2Status,
            "a3" to a3Status,
            "a4" to a4Status,
            "a5" to a5Status,
            "a6" to a6Status,
            "a7" to a7Status,
            "ra" to ReturnAddressRegisterStatus(currentDepth)
        )

    private val mapStack = ArrayDeque<Map<String, RegisterStatus>>()
    override fun onEnterNewScope() {
        registerStatusMap.forEach { (_, regStatus) ->
            regStatus.onEnterNewScope()
        }
        mapStack.addFirst(registerStatusMap)
        currentDepth++
        registerStatusMap = initRegisterStatusMap()
    }

    override fun onLeaveScope() {
        registerStatusMap.forEach { (_, regStatus) ->
            regStatus.onLeaveScope()
        }
        val lastMap = mapStack.removeFirstOrNull()
        if (lastMap == null) {
            CallingConventionCheckerException("ret called without subroutine").printStackTrace()
            exceptionList.add(CallingConventionErrorType.RET_CALLED_WITHOUT_SUBROUTINE)
            return
        }
        registerStatusMap = lastMap
        currentDepth--
    }

    override fun onExitProgram() {
//        val lastMap = mapStack.removeFirstOrNull()
//        if (lastMap != null) {
//            throw CallingConventionCheckerException("should not happen exited program in subroutine")
//        }
        registerStatusMap.forEach { (_, regStatus) ->
            regStatus.onExitProgram()
        }
    }

    override fun update(reg: Observable?, access: Any?) {
            if (reg is Register && access is RegisterAccessNotice ) {
                val registerStatus = registerStatusMap[reg.name] ?: throw CallingConventionCheckerException("invalid register observed (must be a bug): ${reg.name}")
                if (access.accessType == RegisterAccessNotice.WRITE) registerStatus.onWriteAccess() else registerStatus.onReadAccess()
            }
    }

    fun onRegisterSave(regName: String, address: Long) {
        if (!(address >= Memory.stackLimitAddress && address < Memory.stackBaseAddress)) return
        val regStatus = registerStatusMap[regName] ?: return
        regStatus.onSave()
    }

    fun onRegisterLoad(regName: String, address: Long) {
        if (!(address >= Memory.stackLimitAddress && address < Memory.stackBaseAddress)) return
        val regStatus = registerStatusMap[regName] ?: return
        regStatus.onLoad()
    }

    fun removeObservers() {
        for (regName in registerStatusMap.keys) {
            RegisterFile.getRegister(regName).deleteObserver(this)
        }
    }

    fun auipc_workaround() {
        registerStatusMap["t1"]?.auipc_workaround()
    }

    fun jalr_workaround() {
        registerStatusMap["t1"]?.jalr_workaround()
    }
}

class CallingConventionCheckerException(message: String): Exception(message)