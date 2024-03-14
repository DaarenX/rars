package rars.custom.memory


import rars.custom.ScopeAware
import rars.riscv.hardware.Memory
import rars.riscv.hardware.Register
import rars.riscv.hardware.RegisterAccessNotice
import java.util.*
import kotlin.collections.ArrayDeque

// Nicht kompatibel mit backsteps?
class MemoryChecker(spBaseAddress: Long): Observer, ScopeAware {
    val exceptionList: MutableList<MemoryErrorType> = mutableListOf()
    private val memoryMap: MutableMap<Long, Type> = mutableMapOf()
    private var currentSpValue: Long = spBaseAddress
    private var currentUpperLimit = spBaseAddress
    private val upperLimitStack = ArrayDeque<Long>()

    override fun update(sp: Observable?, access: Any?) {
        if (sp is Register && access is RegisterAccessNotice && access.accessType == RegisterAccessNotice.WRITE) {
                val spValue = sp.valueNoNotify

                if (spValue > currentUpperLimit) throwToList("stack should only grow downwards: $spValue > $currentUpperLimit", MemoryErrorType.STACK_SHOULD_ONLY_GROW_DOWNWARDS)

                if ((currentUpperLimit - spValue) % 4 != 0L) throwToList("stack should be incremented by a multiple of 4: $spValue", MemoryErrorType.STACK_ONLY_INCREMENTS_OF_4)

                if (spValue > currentSpValue) {
                    // clear all objects between currentSpValue and spValue and all objects that overlap with spValue
                    val addressRange = currentSpValue.rangeUntil(spValue)
                    if (addressRange.any { memoryMap[it] == null }) {
                        if ((spValue - currentSpValue) % 16 == 0L) {
                            (currentSpValue..<spValue step 16)
                                .map { it..<(it + 16) } // 16-byte-segments
                                .dropWhile { it.all { memoryMap[it] != null } } // drop full segments
                                .drop(1) // one 16-byte-segment is allowed to not be full.
                                .forEach { throwToList("unused stack space in range $it", MemoryErrorType.STACK_RESERVED_TOO_MUCH_SPACE) }
                        } else {
                            throwToList("unused stack space in range $addressRange", MemoryErrorType.STACK_RESERVED_TOO_MUCH_SPACE)
                        }

                    }
                    addressRange.forEach {
                        val stackType = memoryMap[it] ?: return@forEach
                        if (stackType != Type.REMAINDER) {
                            deleteWholeObjectFromMemory(it)
                        }

                    }

                }
                currentSpValue = spValue
        }
    }

    fun assembleDataStore(address: Long, size: Int) {
        val type = Type.entries.first { it.size == size }
        val addressRange = address.rangeUntil(address + type.size)
        memoryMap[address] = type
        addressRange.drop(1).forEach { memoryMap[it] = Type.REMAINDER }
        return
    }

    fun checkStore(address: Long, type: Type) {
        if (address >= Memory.stackLimitAddress && address < Memory.stackBaseAddress) return checkStackStore(address, type)
        if (address >= Memory.dataSegmentBaseAddress && address < Memory.dataSegmentLimitAddress) return checkDataStore(address, type)
    }

    private fun checkStackStore(address: Long, type: Type) {
        if (type != Type.WORD) throwToList("Only word stores allowed in stack", MemoryErrorType.STACK_ONLY_WORD_AS_STORE)
        if (address < currentSpValue || address + type.size > currentUpperLimit) throwToList("store instruction address not in range ($currentSpValue - $currentUpperLimit): $address", MemoryErrorType.STACK_STORE_NOT_IN_RESERVED_RANGE)

        val addressRange = address.rangeUntil(address + type.size)
        memoryMap[address] = type
        addressRange.drop(1).forEach { memoryMap[it] = Type.REMAINDER }
    }

    private fun checkDataStore(address: Long, type: Type) {
        val addressRange = address.rangeUntil(address + type.size)
        val currentDataObject = memoryMap[address]

        if (currentDataObject == Type.REMAINDER) {
            var startAddressOfObject = address
            while (memoryMap[startAddressOfObject] == Type.REMAINDER) {
                startAddressOfObject -= 1
            }
            if (memoryMap[startAddressOfObject] == null) throw MemoryCheckerException("checkDataStore error")
            deleteWholeObjectFromMemory(startAddressOfObject)
        }

        addressRange.forEach {
            val objectType = memoryMap[it] ?: return@forEach
            if (objectType != Type.REMAINDER) {
                deleteWholeObjectFromMemory(it)
            }

        }

        memoryMap[address] = type
        addressRange.drop(1).forEach { memoryMap[it] = Type.REMAINDER }
    }

    fun checkLoad(address: Long, type: Type) {
        if (address >= Memory.stackLimitAddress && address < Memory.stackBaseAddress) return checkStackLoad(address, type)
        if (address >= Memory.dataSegmentBaseAddress && address < Memory.dataSegmentLimitAddress) return checkDataLoad(address, type)

    }

    private fun checkStackLoad(address: Long, type: Type) {
        if (type != Type.WORD) throwToList("only word loads allowed in stack", MemoryErrorType.STACK_ONLY_WORD_AS_LOAD)

        if (memoryMap[address] == null) throwToList("load invalid: no object at address $address", MemoryErrorType.STACK_LOAD_INVALID_ADDRESS)


    }

    private fun checkDataLoad(address: Long, type: Type) {
        val dataType = memoryMap[address]
        if (dataType == null) throwToList("load invalid: no object at address $address", MemoryErrorType.DATA_LOAD_INVALID_ADDRESS)
        else if (dataType != type) throwToList("load type mismatch at address $address: load $type called on $dataType", MemoryErrorType.DATA_TYPE_MISMATCH)
    }

    override fun onEnterNewScope() {
        upperLimitStack.addFirst(currentUpperLimit)
        currentUpperLimit = currentSpValue
    }

    override fun onLeaveScope() {
        if (currentSpValue != currentUpperLimit) throwToList("invalid sp position at end of subroutine: is $currentSpValue, should be $currentUpperLimit", MemoryErrorType.STACK_POINTER_DIFFERENT_FROM_START)
        val lastLimit = upperLimitStack.removeFirstOrNull()
        if (lastLimit == null) {
            throwToList("ret called without subroutine", MemoryErrorType.RET_CALLED_WITHOUT_SUBROUTINE)
            return
        }
        currentUpperLimit = lastLimit
    }

    override fun onExitProgram() {
        if (currentSpValue != currentUpperLimit) throwToList("invalid sp position at end of subroutine: is $currentSpValue, should be $currentUpperLimit", MemoryErrorType.STACK_POINTER_DIFFERENT_FROM_START)
    }


    enum class Type(val size: Int) {
        BYTE(1),
        HALF(2),
        WORD(4),
        DOUBLE(8),
        REMAINDER(-1)
    }
    
    private fun throwToList(message: String, type: MemoryErrorType) {
        MemoryCheckerException(message).printStackTrace()
        exceptionList.add(type)
    }

    private fun deleteWholeObjectFromMemory(address: Long) {
        val type = memoryMap[address] ?: throw MemoryCheckerException("trying to delete whole object when it is null")
        address.rangeUntil(address + type.size).forEach { x -> memoryMap.remove(x) }
    }
}

class MemoryCheckerException(message: String): Exception(message)

