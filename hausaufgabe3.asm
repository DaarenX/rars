.data
satz:	.asciz "QCoC, pjo rhuu Hzzltisly!"
laenge:	.word	25

.text
dekodieren:
# Implementierung beginnt hier

init:
	li	t0,	0			# i = 0
	la	t1,	laenge
	lw	t1,	(t1)			# t1 = laenge
	
	# Addresse von Satz
	la	t2,	satz
	
	
	# Vergleichswerte laden
	li	t4,	65
	li	t5,	122

	b				loop_end# goto loop_end
loop:
	lb	t3,	(t2)			# zeichen = *satz
	
cond1:
	blt	t3,	t4,		endif1	# if (zeichen < 65) goto endif1
	bgt	t3,	t5,		endif1	# if (zeichen > 122) goto endif1
	
then1:
	addi	t3,	t3,	-7		# zeichen = zeichen - 7

cond2:
	blt	t3,	t4		then2	# if (zeichen < 65) goto then2
	b				endif2	# goto endif2

then2:
	addi	t3,	t3,	57		# zeichen = zeichen + 57

endif2:
	sb	t3,	(t2)			# satz[i] = zeichen


endif1:
	
	# Inkrement
	addi	t2,	t2,	1		# satz++
	addi	t0,	t0,	1		# i++
	
loop_end:
	blt	t0,	t1,		loop

# Implementierung endet hier
	
# Gibt satz (aus Datensegment) aus Konsole aus und beendet das Programm
output:
	la a0, satz
	li a7, 4
	ecall
	li a7, 10
	ecall
