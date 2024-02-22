.data
array: .word 1, 2, 3, 4, 5

.text
.globl main
main:
  # Array-Start in Register a0 laden
  la t0, array

  # Länge des Arrays in Register a1 laden
  li a1, 5

  # Schleife über alle Elemente des Arrays
  loop:
    # Element aus dem Array laden
    lw a0, (t0)

    # Element ausgeben
    li a7, 1
    ecall

    # Zeiger auf nächstes Element erhöhen
    addi t0, t0, 3

    # Schleifenbedingung prüfen
    addi a1, a1, -1
    bgtz a1, loop

  # Programm beenden
  li a7, 10
  ecall