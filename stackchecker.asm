.global main

.text


main:
	li t0, 1
    
	addi sp, sp, -3
	sw t0, (sp)
	call func
	addi sp, sp, 4

	call secondfunc
	
	li a7, 10
	ecall

func:
	li t1, 1
	ret

secondfunc:
    li t1, 2
    ret