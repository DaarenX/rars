.data
satz:	.asciz "QCoC, pjo rhuu Hzzltisly!"
laenge:	.word	25

.text
dekodieren:
# Implementierung beginnt hier
li t0, 0 # int i = 0
j Lcond_for

Lbody_for:
## char zeichen = satz [i]
la t1, satz 
add t1, t1, t0
lb t1, (t1) # t1 = zeichen
##
##
li t2, 65
blt t1, t2, Lend_if_one # if zeichen >= 65
li t2, 122
bgt t1, t2, Lend_if_one # if zeichen <= 122
##
Lthen_if_one:
## zeichen = zeichen - 7
addi t1, t1, -7
##

##
li t2, 65
bge t1, t2, Lend_if_two
##
##
Lthen_if_two:
addi t1, t1, 57
##
Lend_if_two:
la t2, satz
add t2, t2, t0
sb t1, (t2)

Lend_if_one:

addi t0, t0, 1 # i++

Lcond_for:
lb t1, laenge
blt t0, t1, Lbody_for


# Implementierung endet hier
	
# Gibt satz (aus Datensegment) aus Konsole aus und beendet das Programm
output:
	la a0, satz
	li a7, 4
	ecall
	li a7, 10
	ecall
