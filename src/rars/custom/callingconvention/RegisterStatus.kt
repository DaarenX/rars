package rars.custom.callingconvention

import rars.Globals
import rars.custom.ScopeAware


abstract class RegisterStatus(protected val register: String, protected val depth: Int): ScopeAware {
    protected var hasBeenWritten: Boolean = false
    protected var hasBeenRead: Boolean = false
    protected var saveAmount: Int = 0
    protected var loadAmount: Int = 0
    protected var skipNextReadAccess: Boolean = false // store operation
    protected var skipNextWriteAccess: Boolean = false // load operation
    protected var enteredNewScope = false
    open fun onWriteAccess() {
        if (skipNextWriteAccess) {
            skipNextWriteAccess = false
            return
        }
        hasBeenWritten = true
    }
    open fun onReadAccess() {
        if (skipNextReadAccess) {
            skipNextReadAccess = false
            return
        }
        if (!hasBeenWritten) throwToList("read without being written", CallingConventionErrorType.GENERAL_REGISTER_READ_BEFORE_WRITE)
        hasBeenRead = true
    }
    open fun onSave() {
        if (skipNextReadAccess) throw CallingConventionCheckerException("should not happen: skipNextReadAccess true onSave $this")
        skipNextReadAccess = true
        saveAmount++
    }

    open fun onLoad() {
        if (skipNextWriteAccess) throw CallingConventionCheckerException("should not happen: skipNextWriteAccess true onLoad $this")
        skipNextWriteAccess = true
        loadAmount++
    }

    override fun onLeaveScope() {
        if (saveAmount != loadAmount) throwToList("saveAmount($saveAmount) != loadAmount ($loadAmount)", CallingConventionErrorType.GENERAL_REGISTER_SAVE_AND_LOAD_AMOUNT_NOT_EQUAL)
        if (hasBeenWritten && !hasBeenRead) throwToList("written but never read", CallingConventionErrorType.GENERAL_REGISTER_WRITE_WITHOUT_READ)
    }
    override fun toString(): String {
        return "$register($depth) {hasBeenWritten:$hasBeenWritten; hasBeenRead:$hasBeenRead; saveAmount:$saveAmount; loadAmount:$loadAmount; skipNextReadAccess:$skipNextReadAccess; skipNextWriteAccess:$skipNextWriteAccess}"
    }

    override fun onExitProgram() {
        onLeaveScope()
    }
    fun throwToList(message: String, type: CallingConventionErrorType) {
        val exception = CallingConventionCheckerException("$register($depth): $message")
        exception.printStackTrace()
        Globals.callingConventionChecker.exceptionList.add(type)
    }

    fun auipc_workaround() {
        if (register != "t1") throw Exception("auipc_workaround called on different register than t1")
        skipNextWriteAccess = true
    }

    fun jalr_workaround() {
        if (register != "t1") throw Exception("jalr_workaround called on different register than t1")
        skipNextReadAccess = true
    }
}

class CalleeSaveRegisterStatus(register: String, depth: Int): RegisterStatus(register, depth) {
    override fun onWriteAccess() {
        if (skipNextWriteAccess) {
            super.onWriteAccess()
            return
        }
        super.onWriteAccess()
        if (saveAmount == 0) throwToList("callee save register written before saving", CallingConventionErrorType.CALLEE_SAVE_REGISTER_WRITTEN_BEFORE_SAVING)
        if (saveAmount == loadAmount && saveAmount > 0) throwToList("callee save register written after restoring", CallingConventionErrorType.CALLEE_SAVE_REGISTER_WRITTEN_AFTER_RESTORING)
    }

    override fun onReadAccess() {
        if (skipNextReadAccess) {
            super.onReadAccess()
            return
        }
        super.onReadAccess()
        if (loadAmount != 0) throwToList("callee save register read after restoring", CallingConventionErrorType.CALLEE_SAVE_REGISTER_READ_AFTER_RESTORING)
    }

    override fun onEnterNewScope() {
        enteredNewScope = true
    }

    override fun onLeaveScope() {
        super.onLeaveScope()
        if (hasBeenWritten && !enteredNewScope) throwToList("callee save register used without calling a function, should use a caller-save register instead", CallingConventionErrorType.SHOULD_USE_CALLER_SAVE_INSTEAD_OF_CALLEE_SAVE)
    }

      override fun onLoad() {
        if (saveAmount == 0) throwToList("restored without saving", CallingConventionErrorType.CALLEE_SAVE_RESTORED_WITHOUT_SAVING)
        if (!hasBeenWritten) throwToList("saved and restored without writing in between", CallingConventionErrorType.CALLEE_SAVE_SAVED_AND_RESTORED_WITHOUT_WRITING)
        return super.onLoad()
    }
}

class CallerSaveRegisterStatus(register: String, depth: Int): RegisterStatus(register, depth) {

    override fun onWriteAccess() {
        if (skipNextWriteAccess) {
            super.onWriteAccess()
            return
        }
        super.onWriteAccess()
        if (saveAmount > loadAmount) throwToList("written after saving without restoring", CallingConventionErrorType.CALLER_SAVE_WRITE_AFTER_SAVE)
    }

    override fun onEnterNewScope() {
        if (hasBeenWritten && saveAmount == 0)  throwToList("save missing before call", CallingConventionErrorType.CALLER_SAVE_MISSING_SAVE)
        enteredNewScope = true
    }

    override fun onLeaveScope() {
        super.onLeaveScope()
        if (saveAmount > 1 ) throwToList("caller save register was saved more than once, should use callee save register instead", CallingConventionErrorType.SHOULD_USE_CALLEE_SAVE_INSTEAD_OF_CALLER_SAVE)

    }

    override fun onSave() {
        if (!hasBeenWritten) throwToList("saved without being written", CallingConventionErrorType.CALLER_SAVE_SAVE_WITHOUT_WRITE)
        super.onSave()
    }


    override fun onLoad() {
        if (saveAmount == 0) throwToList("restored without saving", CallingConventionErrorType.CALLER_SAVE_RESTORED_WITHOUT_SAVING)
        if (!enteredNewScope) throwToList("saved and restored without calling a function in between", CallingConventionErrorType.CALLER_SAVE_SAVED_AND_RESTORED_WITHOUT_CALLING_FUNCTION)
        super.onLoad()
    }
}

class ArgumentRegisterStatus(register: String, depth: Int): RegisterStatus(register, depth) {

    override fun onReadAccess() {
//        println("read $this")
        super.onReadAccess()
    }

    override fun onWriteAccess() {
//        println("write $this")
        super.onWriteAccess()
    }

    override fun onExitProgram() {
        if (hasBeenWritten && !hasBeenRead) throwToList("written but never read", CallingConventionErrorType.GENERAL_REGISTER_WRITE_WITHOUT_READ)
    }
    override fun onEnterNewScope() = Unit
    override fun onLeaveScope() = Unit
}

class ReturnAddressRegisterStatus(depth: Int): RegisterStatus("ra", depth) {
    override fun onWriteAccess() {
        if (skipNextWriteAccess) skipNextWriteAccess = false
    }
    override fun onReadAccess() {
        if (skipNextReadAccess) skipNextReadAccess = false
    }
    override fun onEnterNewScope() {
        if (depth >= 1 && saveAmount == loadAmount)  {
            throwToList("missing ra save", CallingConventionErrorType.RETURN_ADDRESS_REGISTER_MISSING_SAVE)
            throw CallingConventionCheckerException("missing ra save")
        }
    }
    override fun onLeaveScope() = Unit

    override fun onExitProgram() = Unit

}
