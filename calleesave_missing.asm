.global main

.text


main:
    li s0, 1

    addi sp, sp, -4
    sw s0, (sp)
	li s0, 1

	call func

    lw s0, (sp)
    addi sp, sp, 4
	li a7, 10
	ecall

func:
    addi sp, sp, -4
    sw s0, (sp)

	li s0, 2
	addi s0, s0, 1


	lw s0, (sp)
	addi sp, sp, 4
	ret