main:
	call func

	li a7, 10
	ecall
	
func:
	addi sp, sp, -4
	sw s0, 0(sp) # not needed, because s0 is not used
	# do something without s0
	lw s0, 0(sp)
	addi sp, sp, 4
	ret