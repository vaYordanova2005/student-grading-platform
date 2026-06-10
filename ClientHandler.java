import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable{
    Socket socket;
    SystemManager systemManager;
    BufferedReader br;
    PrintStream ps;

    public ClientHandler(Socket socket, SystemManager systemManager){
        try {
            this.socket = socket;
            this.systemManager = systemManager;
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.ps = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            closeEverything(socket, br, ps);
        }
       
    }

    @Override
    public void run(){
        while(socket.isConnected()){
            try {
                 ps.println("Enter username: ");
                String username = br.readLine();
                ps.println("Enter password: ");
                String password = br.readLine();
                User1 user = systemManager.login(username, password);
                if(user == null){
                    ps.println("Invalid password / username");
                    continue;
                }

                UserType type = user.getUserType();
                if(type == UserType.ADMIN){
                    systemManager.addUser(ps);
                }else if(type == UserType.STUDENT){
                    systemManager.showGrades((Student1) user, ps);
                }else if(type == UserType.TEACHER){
                    systemManager.addGrade((Teacher1) user, ps);
                }

            } catch (IOException e) {
                closeEverything(socket, br, ps);
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader br, PrintStream ps){
        try {
           if(socket != null) socket.close();
           if(br != null) br.close();
           if(ps != null) ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
