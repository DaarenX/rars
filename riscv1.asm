.global main
.text
main:
	# Simple test to confirm the success code works
	li t0, 1
    li t1, 2
    
	addi sp, sp, -4
	sw t0, -4(sp)
	#sh t1, 2(sp)
    #lh t1, (sp)
	addi sp, sp, 4
