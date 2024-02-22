main:
    addi sp, sp, -4
    sw s0, 0(sp)

	call func


    lw s0, 0(sp)
    addi sp, sp, 4
	li a7, 10
	ecall

func:
	# do something with s0, save on stack is missing
	addi s0, s0, 1
	ret