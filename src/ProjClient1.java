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

public class ProjClient1 {
    public static void main(String[] args) throws UnknownHostException, IOException
    {
        //Uncomment next line and comment out the following line to allow user to specify ip address of server to connect to
        //Socket socket = new Socket(args[0], 4446);
        Socket socket = new Socket("localhost", 4446);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        ArrayList<File> filesToSend = new ArrayList<>();

        //if user passed in cmd-line arguments of file path(s) for client to send
        //then add them to the arraylist here
        for(int i =1; i< args.length; i++)
        {
            filesToSend.add(new File(args[i]));
        }

        //for demo purposes, hardcode in files for client to send
        filesToSend.add(new File("c://Users//Public//temp//fib2.txt"));
        filesToSend.add(new File("c://Users//Public//temp//fibExtraLoops.txt"));

        //tell ServerThread how many files to expect
        dataOutputStream.writeInt(filesToSend.size());
        //send all the files from 'filesToSend'
        while(!filesToSend.isEmpty())
        {
            File fileSending = filesToSend.remove(0);
            System.out.println("Sending file: "+fileSending.getPath()+" ; File Size: "+fileSending.length());

            dataOutputStream.writeLong(fileSending.length()); //send file length
            //dataOutputStream.writeUTF(fileSending.getPath());
            InputStream inputStream = new FileInputStream(fileSending);

            byte[] array = new byte[8192];
            int count;
            while((count = inputStream.read(array))>0){
                outputStream.write(array, 0, count);
            }
            inputStream.close();
        }

        outputStream.close();
        dataOutputStream.close();
        socket.close();
    }
}