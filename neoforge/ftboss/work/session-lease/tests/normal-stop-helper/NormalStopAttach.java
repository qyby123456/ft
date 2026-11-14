import com.sun.tools.attach.VirtualMachine;

public final class NormalStopAttach {
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 2) throw new IllegalArgumentException("PID and isolated helper jar required");
        VirtualMachine machine = VirtualMachine.attach(arguments[0]);
        try {
            machine.loadAgent(arguments[1], "observation-complete-normal-stop");
        } finally {
            machine.detach();
        }
        System.out.println("Normal stop request submitted to existing observed process " + arguments[0]);
    }
}
