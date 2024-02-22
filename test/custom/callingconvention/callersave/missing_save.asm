main:
	li t0, 1
	# save on stack is missing
	call func
	li a7, 10
	ecall

func:
	# do something with or without t0
	ret
