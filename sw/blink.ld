ENTRY(RESET)

MEMORY
{
    FLASH (rx): ORIGIN = 0x0, LENGTH = 0x1000 /* 4kB */
    RAM (rx)  : ORIGIN = 0x0, LENGTH = 0x1000 /* 4kB */
}

SECTIONS
{
    text :
    {
        . = ALIGN(4);
        *(.text)           /* .text sections (code) */
        *(.text*)          /* .text* sections (code) */
        *(.rodata)         /* .rodata sections (constants, strings, etc.) */
        *(.rodata*)        /* .rodata* sections (constants, strings, etc.) */
        *(.glue_7)         /* glue arm to thumb code */
        *(.glue_7t)        /* glue thumb to arm code */
        . = ALIGN(4);
        _etext = .;        /* define a global symbols at end of code */
    } >FLASH
}
