import java.net.*;
import java.io.*;

import javax.sound.sampled.*;

public class SoundReceiver implements Runnable
{
    Socket connection = null;
    DataInputStream soundIn = null;
    SourceDataLine inSpeaker = null;
    Global_Variable gVar;

    public SoundReceiver(Socket conn, Global_Variable gVar) throws Exception {
        connection = conn;
        soundIn = new DataInputStream(connection.getInputStream());
        AudioFormat af = new AudioFormat(8000.0f,8,1,true,false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        inSpeaker = (SourceDataLine)AudioSystem.getLine(info);
        inSpeaker.open(af);

        this.gVar = gVar;
    }

    public void run() {
        int bytesRead = 0;
        byte[] inSound = new byte[1];
        inSpeaker.start();
        while(gVar.getMicStatus()) {
            try{
                bytesRead = soundIn.read(inSound, 0, inSound.length);
            } catch (Exception e){}
            if(bytesRead >= 0) {
                inSpeaker.write(inSound, 0, bytesRead);
            }
        }
        byte[] offsetByte = new byte[10000];
        try{
            bytesRead = soundIn.read(offsetByte);
        } catch (Exception e){}
        System.out.println("END voice"); //!
        //inSpeaker.close();
    }
}