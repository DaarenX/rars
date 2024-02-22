.text
main:
	# main ist korrekt implementiert - nichts aendern!
	# ruft print_primes(100) zum Testen auf
	li a0, 100
	call print_primes
	# exit()
	li a7, 10
	ecall
	
# void print_primes(int ziel)
# Gibt alle Primzahlen von 1 bis ziel (a0) auf der Kommandozeile aus
print_primes:
	#speichern der Rücksprung adresse
	addi sp, sp, -4
	sw ra, (sp)
	mv s0, a0
	li t0, 2
	b pp_Lcond
pp_Lbody:
	mv a0, t0
	#abspeichern aller temporäreren register
	addi sp, sp, -4
	sw t0, (sp)
	call is_prime
	
	#nach aufruf wieder laden aller temporären register
	lw t0, (sp)
	addi sp, sp, 4
	bnez a0, pp_print
	b pp_inc
pp_print:
	mv a0, t0
	# Gib a0 auf Kommandozeile aus
	li a7, 1
	ecall
	# Gib newline aus
	li a0, 10
	li a7, 11
	ecall
pp_inc:
	addi t0, t0, 1
pp_Lcond:
	blt t0, s0, pp_Lbody
	#laden der rücksprung adresse
	lw ra (sp)
	addi sp, sp, 4
	ret
	

# bool is_prime(int n)
# Gib 1 zurueck (via a0), falls n (a0) eine Primzahl ist. Ansonsten 0.
# Diese Funktion ist korrekt implementiert - nichts aendern!
is_prime:
	li t0, 2
	mv t1, a0
	li a0, 1
	b ip_Lcond
ip_Lbody:
	rem t2, t1, t0
	beqz t2, ip_ret_is_not_prime
	b ip_cont
ip_ret_is_not_prime:
	li a0, 0
	b ip_end
ip_cont:
	addi t0, t0, 1
ip_Lcond:
	blt t0, t1, ip_Lbody
ip_end:
	ret
