package custom.callingconvention

import rars.Globals
import rars.Launch

fun main() {
    Launch.main(arrayOf("test/custom/callingconvention/callersave/unnecessary_save_before_call.asm"))
    assert(Globals.callingConventionChecker.exceptionList.size == 1)
    assert(Globals.callingConventionChecker.exceptionList.getOrNull(0)?.message == "t0(0): saved without being written")
    Launch.main(arrayOf("test/custom/callingconvention/callersave/unnecessary_save_after_call.asm"))
    assert(Globals.callingConventionChecker.exceptionList.size == 1)
    assert(Globals.callingConventionChecker.exceptionList.getOrNull(0)?.message == "t0(1): saved and restored without calling a function in between")
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
