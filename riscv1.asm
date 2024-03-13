.data
array: .asciz "Hello World!"
length: .word 12
.text
    la t0, array # Adresse laden
    la t1, length
    lw t1, (t1)
    li t2, 0

    b cond
body:
    add, t3, t0, t2
    lb t3, (t3)
    # do something with t3
    mv a0, t3
    li a7, 1
    ecall
    addi t2, t2, 1
cond:
    ble t2, t1, body # Fehler