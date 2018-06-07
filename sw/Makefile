all:
	arm-none-eabi-as -o blink.o blink.s
	arm-none-eabi-ld -T blink.ld -o blink.elf blink.o
	arm-none-eabi-objcopy -O binary blink.elf blink.bin

clean:
	rm -f blink.o
	rm -f blink.elf
	rm -f blink.bin
