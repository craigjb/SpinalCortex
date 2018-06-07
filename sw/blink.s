.code 16

# initial stack pointer
.word 0x0
# reset vector
.word RESET
# dummy handler for all other vectors for now
.rept 46
.word EXCEPTION
.endr

.global RESET
.thumb_func
RESET:
    ldr r0, GPIO

    ldr r2, DELAY
    str r2, [r0]
loop1:
    sub r2, r2, #1
    lsr r3, r2, #16
    str r3, [r0]
    cmp r2, #0
    bne loop1
    b RESET


.thumb_func
EXCEPTION:
    b .

.align 4
GPIO:
    .word 0x4000
DELAY:
    .word 0x4C4B40
