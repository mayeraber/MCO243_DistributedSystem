//Client sends file to Main

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

public class ProjClient2 {
    public static void main(String[] args) throws UnknownHostException, IOException
    {
        //Socket socket = new Socket(args[0], 4447); //to pass in ip address to connect to
        Socket socket = new Socket("localhost", 4447);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        ArrayList<File> filesToSend = new ArrayList<>();
        filesToSend.add(new File("c://Users//Public//temp//fib1.txt"));

        dataOutputStream.writeInt(filesToSend.size());
        while(!filesToSend.isEmpty())
        {
            File fileSending = filesToSend.remove(0);
            System.out.println(fileSending.getPath()+" "+fileSending.length());

            dataOutputStream.writeLong(fileSending.length()); //send file length
            //dataOutputStream.writeUTF(fileSending.getPath());
            InputStream inputStream = new FileInputStream(fileSending);

            byte[] array = new byte[8192];
            int count;
            while((count = inputStream.read(array))>0){
                outputStream.write(array, 0, count);
            }
        }

        System.out.println("closed");
        outputStream.close();
        socket.close();

    }
}