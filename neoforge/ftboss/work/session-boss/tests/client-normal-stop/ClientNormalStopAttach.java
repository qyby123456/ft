import com.sun.tools.attach.VirtualMachine;

public final class ClientNormalStopAttach {
    public static void main(String[] arguments) throws Exception {
        VirtualMachine target=VirtualMachine.attach(arguments[0]);
        try {target.loadAgent(arguments[1],arguments[2]);}
        finally {target.detach();}
    }
}
