main:
	li t0, 1

	addi sp, sp, -4
	sw t0, 0(sp) # needed
	call func
	lw t0, 0(sp)
	addi sp, sp, 4

	addi t0, t0, 1
	li a7, 10
	ecall

func:


	li t0, 12
    addi t0, t0, 1

	addi sp, sp, -4
	sw t0, 0(sp) # not needed
	# no clall
	lw t0, 0(sp)
	addi sp, sp, 4

	ret


