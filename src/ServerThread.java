//internal to Main; Main spawns these threads to listen for incoming client connections on multiple ports
//pass in socket for the thread to listen on

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerThread extends Thread{

    ServerSocket serverSocket;
    Socket clientSocket;
    InputStream inputStream;
    DataInputStream dataInputStream;
    PrintWriter out;
    int numLoopIterations=0;
    int socketNumber;
    ConcurrentHashMap<String, Integer> javaFiles;

    public ServerThread(int socketNumber,  ConcurrentHashMap<String, Integer> javaFiles) throws IOException, InterruptedException
    {
        this.socketNumber = socketNumber;
        this.javaFiles = javaFiles;
    }

    public void run()
    {
        try {
            serverSocket = new ServerSocket(socketNumber);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        clientSocket=null;

        try{clientSocket= serverSocket.accept();}
        catch (IOException e)
        {
            System.out.println("Cant accept client");
        }

        try{inputStream= clientSocket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);}
        catch (IOException e)
        {
            System.out.println("Cant get socket input");
        }

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        //Read in from client how many files will be sent
        int numFilesReceiving = 0;
        try {
            numFilesReceiving = dataInputStream.readInt();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("ServerThread class on socket "+socketNumber+": Num files to be receiving from client: "+numFilesReceiving);
        for (int i = 0; i < numFilesReceiving; i++)
        {
            byte[] array = new byte[8192];
            long fileLength = 0;
            try {
                fileLength = dataInputStream.readLong();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  //read in file lengthfrom client
            System.out.println("ServerThread class on socket "+socketNumber+" length of file "+ (i+1)+": "+fileLength);

            //Generate path with a new filename, read in file from client, and write to the new file on server's HD
            String path = "c://Users//Public//temp//"+"ServerFile_"+(i+1)+"_"+socketNumber+".java";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (int j = 0; j < fileLength/8192; j++) {
                int total =0;
                while(total<8192){
                    int count = 0;
                    try {
                        count = dataInputStream.read(array, total, 8192-total);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    total+=count;
                }
                try {
                    fos.write(array, 0, total);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            int count = 0;
            try {
                count = dataInputStream.read(array, 0, (int)fileLength%8192);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                fos.write(array, 0, count);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //fos.flush();
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //call method to check how many loop-iterations are present in the java file that was sent in
            try {
                numLoopIterations = analyzeProgram(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //store file path - along with number of iterations found - in the 'javaFiles' map
            javaFiles.put(path, numLoopIterations);

        }

        try {
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //method to search through a file for for-loops and tally the number of iterations found
    public static int analyzeProgram(String file) throws IOException{

        FileChannel fileChannel = new FileInputStream(file).getChannel();
        ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)fileChannel.size());
        CharBuffer cBuffer = Charset.forName("8859_1").newDecoder().decode(byteBuffer);

        int numIterations = 0;
        int numloops=0;
        String match= "(for\\s*[(]\\s*int \\w\\s*=\\s*)(\\d+)(\\s*;\\s*\\w<=?)(\\d+);";
        //		String match= "(for\\s*[(]\\s*int \\w\\s*=.*)(\\d+)(\\s*;\\s*\\w<=?)(\\d+);";

        Pattern pattern = Pattern.compile(match);
        Matcher matcher = pattern.matcher(cBuffer);

        //search through file for for-loops and subtract the iteration it's up to from the total iterations
        while (matcher.find()) {
            numloops++;
            numIterations+=(Integer.parseInt(matcher.group(4))-Integer.parseInt(matcher.group(2)));
        }

        fileChannel.close();
        System.out.println("Number of for-loops found in "+file+": "+numloops+" Number of iterations found: "+numIterations);
        return numIterations;
    }
}
