import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server1 {
    private ServerSocket serverSocket;
    private SystemManager systemManager; 

    public Server1(ServerSocket serverSocket, SystemManager systemManager){
        this.serverSocket = serverSocket;
        this.systemManager = systemManager;
    }

    public void startServer(){
        try {
            while(!serverSocket.isClosed()){
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, systemManager);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket(){
        try {
            if(serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(5001);
        SystemManager systemManager = new SystemManager();
        systemManager.loadUsers();

        Server1 server = new Server1(serverSocket, systemManager);
        server.startServer();
    }
}