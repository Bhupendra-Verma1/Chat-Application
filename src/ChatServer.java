
/* A chat application based on server-client architecture */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {
    private static final Set<ClientHandler> clients = new HashSet<>();
    private static final int port = 7777;

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server is running and waiting for connections...");

            while (true) {
                Socket socket = server.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadCast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }
}

class ClientHandler extends Thread{
    private final Socket socket;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket){
        this.socket  = socket;
    }

    @Override
    public void run(){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            clientName = br.readLine();
            System.out.println(clientName + " has joined tha chat!");
            ChatServer.broadCast(clientName + " has joined the chat!",this);

            String message;
            while((message = br.readLine()) != null){
                ChatServer.broadCast("[" + clientName + "]: " + message, this);
            }
        }catch(IOException e){
            System.out.println("Error in ClientHandler: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try{
                socket.close();
            }
            catch(IOException e){
                System.out.println("Error closing socket: " + e.getMessage());
            }
            ChatServer.removeClient(this);
            System.out.println(clientName + " has left the chat.");
            ChatServer.broadCast(clientName + " has left the chat", this);
        }

    }

    void sendMessage(String msg){
        out.println(msg);
    }

}
