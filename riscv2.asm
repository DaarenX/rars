.global main
.data
satz:	.asciz "QCoC, pjo rhuu Hzzltisly!"
laenge:	.word	25

.text


main:
    la	t1,	laenge
    lw	t1,	(t1)			# t1 = laenge

	addi sp, sp, -4
	sw t1, (sp)
	li a0, 5
	call func
	call func
	call func
	call secondfunc
	lw t1, (sp)
	addi sp, sp, 4

	li a7, 10
	ecall

func:
	addi a0, a0, 5
	addi sp, sp, -4
	addi sp, sp, 4
	ret

secondfunc:
    ret