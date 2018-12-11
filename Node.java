import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**usage:
 * For running master:
 *  java Node master IP PORT NUMBER_OF_NODES
 * Or:
 *  java Node IP PORT Master_IP Master_PORT
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

    public static void main(String[] args) throws Exception {
        System.out.println(args[0] + args[1]);
        if (args[0].equals("master")) {
            Master master = new Master(args[1], Integer.parseInt(args[2]));
            master.run(Integer.parseInt(args[3]));
        } else {
            Node node = new Node(args[0], Integer.parseInt(args[1]));
            System.out.println("Running Node: " + node.getIpAddr() + " " + node.getPort());
            node.exchangeSockets(args[2], Integer.parseInt(args[3]));
            /** FOR GETTING DATA:
             *  insert here code that will use getNetworkSockets()
             */
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
