import java.net.*;
import java.io.*;

import javax.sound.sampled.*;

public class SoundReceiver implements Runnable
{
    Socket connection = null;
    DataInputStream soundIn = null;
    SourceDataLine inSpeaker = null;

    //final DataOutputStream dos;

    public SoundReceiver(Socket conn) throws Exception {
        connection = conn;
        soundIn = new DataInputStream(connection.getInputStream());
        AudioFormat af = new AudioFormat(8000.0f,8,1,true,false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        inSpeaker = (SourceDataLine)AudioSystem.getLine(info);
        inSpeaker.open(af);
        //dos = new DataOutputStream(connection.getOutputStream());
    }

    public void run() {
        int bytesRead = 0;
        byte[] inSound = new byte[1];
        inSpeaker.start();
        //sendHandler endVoice = new sendHandler(connection,dos);
        while(bytesRead != -1)
        {
            try{
                bytesRead = soundIn.read(inSound, 0, inSound.length);
                String end = new String(inSound);
                if(end.equals("q")){
                    soundIn.close();
                    //dos.close();
                    bytesRead = -1;
                    break;
                }
            } catch (Exception e){
                bytesRead = -1;
            }
            if(bytesRead >= 0)
            {
                inSpeaker.write(inSound, 0, bytesRead);
            }
        }
    }
}