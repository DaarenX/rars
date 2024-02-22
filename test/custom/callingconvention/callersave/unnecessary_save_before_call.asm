main:
	# no t0 usage
	addi sp, sp, -4
	sw t0, 0(sp) # not needed because t0 is not used
	call func
	lw t0, 0(sp)
	addi sp, sp, 4
	li a7, 10
	ecall

func:
	# do something with or without t0
	ret