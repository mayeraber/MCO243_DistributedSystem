//thread to send file to slave
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SlaveThread
{
    Socket socket;
    public SlaveThread(int socketToConnectToSlaveOn, ArrayList<String> filesToSend) throws UnknownHostException, IOException
    {
        socket = new Socket("localhost", socketToConnectToSlaveOn);

        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(filesToSend.size());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        int numFiles = filesToSend.size();
        //the SlaveThread sends the files from the arraylist it was passed to the client
        while(!filesToSend.isEmpty())
        {
            File fileSending = new File(filesToSend.remove(0));
            dataOutputStream.writeLong(fileSending.length()); //send file length
            System.out.println("SlaveThread on socket: "+socketToConnectToSlaveOn+" sending file: "+fileSending.getPath());
            InputStream inputStream = new FileInputStream(fileSending);

            byte[] array = new byte[8192];
            int count;
            while((count = inputStream.read(array))>0){
                outputStream.write(array, 0, count);
            }
            inputStream.close();
        }

        String input = in.readLine();
        while(input!=null)
        {
            System.out.println(input);
            input = in.readLine();
        }

        socket.close();
    }
}
