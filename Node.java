import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

/**usage:
 * For running master:
 *  java Node master IP PORT NUMBER_OF_NODES
 * Otherwise:
 *  java Node IP PORT Master_IP Master_PORT inputfile
 */
public class Node extends Thread {
    private ServerSocket serverSocket;
    private ArrayList<ArrayList<String>> sockets = new ArrayList<ArrayList<String>>();

    private Node(String ipAddr, int port) throws Exception {
        this.serverSocket = new ServerSocket(port,1, InetAddress.getByName(ipAddr));
    }

    // for getting array of sockets
    private ArrayList<ArrayList<String>> getNetworkSockets() {
        return this.sockets;
    }

    private String getIpAddr() {
        return this.serverSocket.getInetAddress().getHostAddress();
    }

    private int getPort() {
        return this.serverSocket.getLocalPort();
    }

    private Socket getServerSocket() throws Exception {
        return this.serverSocket.accept();
    }

    private void listen() throws Exception {
        Socket clientSocket = this.getServerSocket();
    }

    private void exchangeSockets(String masterIpAddr, int masterPort) throws Exception {
        Socket masterSocket = new Socket(masterIpAddr, masterPort);
        DataOutputStream outToMaster = new DataOutputStream(masterSocket.getOutputStream());

        //sending ip and port to master
        outToMaster.writeBytes(this.getIpAddr() + "\n");
        outToMaster.writeBytes(Integer.toString(this.getPort()) + "\n");
        System.out.println("Address send to master");
        masterSocket.close();

        //getting object with addresses of all nodes from master
        masterSocket = this.getServerSocket();
        ObjectInputStream inFromMaster = new ObjectInputStream(masterSocket.getInputStream());
        System.out.println("Input stream created");
        Object s = inFromMaster.readObject();
        System.out.println("Received object from master { " + s + " }");
        this.sockets = (ArrayList<ArrayList<String>>) s;
    }

    private static void runMaster() {}

    private static BigInteger getSpanSize(int nodeNumber) {
        return BigInteger.valueOf(2).pow(80).divide(BigInteger.valueOf(nodeNumber));
    }

    private ArrayList<Socket> createSockets(ArrayList<ArrayList<String>> networkSockets) throws IOException {
        ArrayList<Socket> nodeSockets= new ArrayList<>();
        for (ArrayList<String> nodeAddress : networkSockets) {
            if (!nodeAddress.get(0).equals(this.getIpAddr())) {
                Socket nodeSocket = new Socket(nodeAddress.get(0), Integer.parseInt(nodeAddress.get(1)));
                nodeSockets.add(nodeSocket);
            } else {
                nodeSockets.add(null);
            }
        }
        return nodeSockets;
    }

    private static ArrayList<DataOutputStream> createOutputStreams(ArrayList<Socket> sockets) throws IOException {
        ArrayList<DataOutputStream> result = new ArrayList<>();
        for (Socket sock : sockets) {
            if (sock != null) {
                DataOutputStream stream = new DataOutputStream(sock.getOutputStream());
                result.add(stream);
            } else {
                result.add(null);
            }

        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(args[0] + args[1]);
        if (args[0].equals("master")) {
            Master master = new Master(args[1], Integer.parseInt(args[2]));
            master.run(Integer.parseInt(args[3]));
        } else {
            // Create a new processing node
            Node node = new Node(args[0], Integer.parseInt(args[1]));
            System.out.println("Running Node: " + node.getIpAddr() + " " + node.getPort());
            // Communicate with Master node to obtain other node addresses
            node.exchangeSockets(args[2], Integer.parseInt(args[3]));
            // Calculate key range for each node
            BigInteger spanSize = getSpanSize(node.getNetworkSockets().size());
            // Create a new socket connection with each node
            ArrayList<Socket> nodeSockets = node.createSockets(node.getNetworkSockets());
            // Create data output stream for each socket
            ArrayList<DataOutputStream> nodeStreams = createOutputStreams(nodeSockets);
            /** FOR GETTING DATA:
             *  insert here code that will use getNetworkSockets()
             **/
            File file = new File(args[4]);
            try (RandomAccessFile data = new RandomAccessFile(file, "r")) {
                byte[] record = new byte[100];
                int nodeIndex;
                for (long i = 0, len=data.length()/100; i < len; i++) {
                    data.readFully(record);
                    //if key is in x range, send it to x socket
                    nodeIndex = new BigInteger(1, Arrays.copyOfRange(record, 0, 9)).divide(spanSize).intValue();
                    nodeStreams.get(nodeIndex).write(record);
                }
            }
            try {
                while (true) {
                    /** FOR SORTING:
                     *  insert here code for getting data from sockets
                     */
                    node.listen();
                }
            } finally {
                node.serverSocket.close();
            }
        }
    }
}
