package rars.riscv.instructions;

import rars.Globals;
import rars.custom.memory.MemoryChecker;
import rars.riscv.hardware.AddressErrorException;

public class LD extends Load {
    public LD() {
        super("ld t1, -100(t2)", "Set t1 to contents of effective memory double word address", "011",true);
    }

    public long load(int address) throws AddressErrorException {
        Globals.memoryChecker.checkLoad(address, MemoryChecker.Type.DOUBLE);
        return Globals.memory.getDoubleWord(address);
    }
}
