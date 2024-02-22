main:
    addi sp, sp, -4
    sw s0, 0(sp)

	li s0, 1
	addi sp, sp, -4
	sw s0, 0(sp) # not needed
	call func
	lw s0, 0(sp)
	addi sp, sp, 4

	lw s0, 0(sp)
	addi sp, sp, 4
	li a7, 10
	ecall

func:
	addi sp, sp, -4
	sw s0, 0(sp) # needed
	# do something with s0
	lw s0, 0(sp)
	addi sp, sp, 4
	ret