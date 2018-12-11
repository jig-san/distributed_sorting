import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Master extends Thread {
    private ServerSocket serverSocket;
    ArrayList<ArrayList<String>> sockets = new ArrayList<>();

    public Master(String masterIpAddr, int masterPort) throws Exception {
        this.serverSocket = new ServerSocket(masterPort, 1, InetAddress.getByName(masterIpAddr));
    }

    private String getIpAddr() {
        return this.serverSocket.getInetAddress().getHostAddress();
    }

    private int getPort() {
        return this.serverSocket.getLocalPort();
    }

    private void listen(Integer n_nodes) throws Exception {
        Socket client = this.serverSocket.accept();
        System.out.println("=======================");

        System.out.println("Connection accepted from node IP " + client.getInetAddress().getHostAddress());
        BufferedReader inFromNode = new BufferedReader(new InputStreamReader(client.getInputStream()));
        System.out.println("Buffer reader created");
        String nodeIp = inFromNode.readLine();
        Integer nodePort = Integer.parseInt(inFromNode.readLine());
        System.out.println("Saved node " + nodeIp + " " + nodePort);

        ArrayList<String> addr = new ArrayList<>();
        addr.add(nodeIp);
        addr.add(Integer.toString(nodePort));
        this.sockets.add(addr);

        System.out.println("Registered nodes count: " + this.sockets.size());
        if (this.sockets.size() == n_nodes)
            this.sendAddressesToNodes();

    }

    private void sendAddressesToNodes() throws Exception {
        for (int i = 0; i < this.sockets.size(); i++) {
            Socket nodeSocket =
                    new Socket(this.sockets.get(i).get(0), Integer.parseInt(this.sockets.get(i).get(1)));
            ObjectOutputStream outToNode = new ObjectOutputStream(nodeSocket.getOutputStream());
            System.out.println("Output stream created for " +
                    this.sockets.get(i).get(0) + " " + this.sockets.get(i).get(1));
            outToNode.writeObject(this.sockets);
            System.out.println("Object sent");
        }
    }

    public void run(Integer n_nodes) throws Exception {
        System.out.println("Running Master Node: IP:" + this.getIpAddr() + " PORT:" + this.getPort());
        try {
            while (true)
                this.listen(n_nodes);
        }
        finally {
            this.serverSocket.close();
        }
    }
}
