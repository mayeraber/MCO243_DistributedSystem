//Thread for slave to run to accept incoming connection from master ('Main') and take in a file,
//save it in its temp dir, compile and run it
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.Line;

public class Slave2
{

    public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException
    {
        //Use next line instead of the following line if you want to pass in a specific port number to connect on
        //new Slave(Integer.parseInt(args[0]));
        new Slave2(4431);
    }

    ServerSocket serverSocket;
    Socket clientSocket;
    InputStream inputStream;
    DataInputStream dataInputStream;
    PrintWriter out;

    public Slave2(int socketNumber) throws IOException, InterruptedException
    {
        serverSocket = new ServerSocket(socketNumber);
        clientSocket=null;
        try{clientSocket= serverSocket.accept();System.out.println("connected!");}
        catch (IOException e) {System.out.println("Cant accept client");}

        try{inputStream= clientSocket.getInputStream();}
        catch (IOException e) {System.out.println("Cant get socket input");	}

        dataInputStream = new DataInputStream(inputStream);
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        //Slave reads in from SlaveThread the number of files it'll be sent
        int numFilesReceiving = dataInputStream.readInt();
        System.out.println("Receiving "+ numFilesReceiving+ " files");

        for (int i = 0; i < numFilesReceiving; i++)
        {
            byte[] array = new byte[8192];
            long fileLength = dataInputStream.readLong();
            String simpleFileName =  "SLAVE"+i;
            String javaFileName = simpleFileName+".java";
            System.out.println("File has been named: "+javaFileName);
            String path = "c://Users//Public//temp//"+javaFileName;

            //read in file and write to new file in Slave's HD
            //File is renamed so that this system can be tested on a single computer using a single folder
            // in the system without overwriting the original file that was sent
            FileOutputStream fos = new FileOutputStream(path);
            for (int j = 0; j < fileLength/8192; j++) {
                int total =0;
                while(total<8192){
                    int count = dataInputStream.read(array, total, 8192-total);
                    total+=count;
                }
                fos.write(array, 0, total);
                fos.flush();
            }
            int count = dataInputStream.read(array, 0, (int)fileLength%8192);
            fos.write(array, 0, count);
            fos.flush();
            fos.close();

            //Change class name in file to match the new name of the file
            ArrayList<String> newLines = new ArrayList<>();
            for(String line: Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)){
                if(line.startsWith("public class"))
                    newLines.add("public class SLAVE"+i);
                else
                    newLines.add(line);
            }
            Files.write(Paths.get(path), newLines, StandardCharsets.UTF_8);


            System.out.println("File has been received "+path);

            //Compile the code and run it

            //example: full param will be arg "javac -cp -g c://Users//Public//temp//hi5.java"
            runProcess("javac -cp -g "+path);
            //example full param "java -cp c://Users//Public//temp hi5"
            out.println("Output from program: "+simpleFileName+ "\n"+runProcess("java -cp c://Users//Public//temp "+simpleFileName));
            out.flush();

        }
        out.close();
    }


    public static String runProcess(String process) throws InterruptedException 	{

        Process process2 = null;
        String iString = null;
        String output = "";
        try {
            process2 = Runtime.getRuntime().exec(process);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            BufferedReader bufferedError = new BufferedReader(new InputStreamReader(process2.getErrorStream()));

            //System.out.println(bufferedReader.readLine());
            while((iString=bufferedReader.readLine())!=null){
                System.out.println(iString);
                output += (iString+"\n");
            }
            while((iString=bufferedError.readLine())!=null){
                System.out.println(iString);
                output += iString;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        process2.waitFor();
        return output;
    }



}
