.global main
.text
main:
	li t0, 1
    li t1, 1

	addi sp, sp, -64
	sw t0, (sp)
    sw t1, 4(sp)
    sw t1, 8(sp)
    sw t1, 12(sp)
    sw t1, 16(sp)
    sw t1, 20(sp)
	call func

	lw t0, (sp)
	lw t1, 4(sp)
    lw t1, 8(sp)
    lw t1, 12(sp)
    lw t1, 16(sp)
    lw t1, 20(sp)
	addi sp, sp, 64

    addi t0, t0, 1
    addi t1, t1, 1

	li a7, 10
    ecall

func:
    ret