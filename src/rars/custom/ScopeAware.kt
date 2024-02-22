package rars.custom

interface ScopeAware {
    fun onEnterNewScope()
    fun onLeaveScope()
    fun onExitProgram()
}
