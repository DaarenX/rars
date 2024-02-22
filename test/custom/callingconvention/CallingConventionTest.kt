package custom.callingconvention

import rars.Globals
import rars.Launch
import rars.custom.callingconvention.CallingConventionErrorType

fun main() {
    Launch.main(arrayOf("test/custom/callingconvention/callersave/unnecessary_save_before_call.asm"))
    assert(Globals.callingConventionChecker.exceptionList.size == 1)
    assert(Globals.callingConventionChecker.exceptionList.getOrNull(0) == CallingConventionErrorType.CALLER_SAVE_SAVE_WITHOUT_WRITE)
    Launch.main(arrayOf("test/custom/callingconvention/callersave/unnecessary_save_after_call.asm"))
    assert(Globals.callingConventionChecker.exceptionList.size == 1)
    assert(Globals.callingConventionChecker.exceptionList.getOrNull(0) == CallingConventionErrorType.CALLER_SAVE_SAVED_AND_RESTORED_WITHOUT_CALLING_FUNCTION)
    println(Globals.callingConventionChecker.exceptionList)

}

fun assert(value: Boolean) {
    assert(value) { "Assertion failed" }
}
fun assert(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message)
    }
}
