//server to spawn 'ServerThreads' to receive file from ProjClient and tally up the loops
//TODO maybe pass in the ports to connect to slaves on
//TODO allow clients to send multiple files

import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MasterServer
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        ConcurrentHashMap<String, Integer> javaFiles = new ConcurrentHashMap<>();

        ArrayList<ServerThread> serverThreads = new ArrayList<>();
        serverThreads.add(new ServerThread(4447, javaFiles));
        serverThreads.add(new ServerThread(4446, javaFiles));

        for (ServerThread serverThread : serverThreads)
        {
            serverThread.start();
        }

        for (ServerThread serverThread : serverThreads)
        {
            serverThread.join();
        }

        for (Map.Entry<String, Integer> sor : javaFiles.entrySet())
            System.out.println(sor.getKey()+" "+sor.getValue());

        System.out.println("\nSorted:");
        Map<String, Integer> sortedBySize = javaFiles.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for (Map.Entry<String, Integer> sor : sortedBySize.entrySet())
            System.out.println(sor.getKey()+" "+sor.getValue());



        int[] numJobs = new int[2];
        ArrayList<String>[] allJobs = new ArrayList[2];
        ArrayList<String> jobs = new ArrayList<String>();
        ArrayList<String> jobs2 = new ArrayList<String>();
        allJobs[0] = jobs;
        allJobs[1] = jobs2;
        int index = 0;
        for (Map.Entry<String, Integer> sor : sortedBySize.entrySet())
        {
            numJobs[index] += sor.getValue();
            allJobs[index].add(sor.getKey());
            index = (index==0) ? 1 : 0;

        }
        SlaveThread slave1 = new SlaveThread(4441, jobs);
        SlaveThread slave2 = new SlaveThread(4431, jobs2);
    }
}