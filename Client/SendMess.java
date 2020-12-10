import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SendMess implements Runnable {
    final DataOutputStream dos;
    final Socket s;

    SendMess(Socket socket, DataOutputStream dos) throws Exception{
        s = socket;
        this.dos = dos;
    }

    @Override
    public void run() {
        sendHandler program = new sendHandler(s, dos);
        while (true) {

            try {
                System.out.println("Enter mess: ");
                Scanner in = new Scanner(System.in);
                String toSend = in.nextLine();
                dos.write(toSend.getBytes(),0,1);
                //System.out.println(toSend.getBytes().length);

                if(toSend.equals("1")){
                    program.setSound(true);
                }

                if (toSend.equals("q")) {
                    //dos.close();
                    //s.close();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
