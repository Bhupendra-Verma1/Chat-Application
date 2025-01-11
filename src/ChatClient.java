import java.io.*;
import java.net.Socket;

public class ChatClient {
    private static final String host = "localhost";
    private static final int port = 7777;
    private String userName;
    private Socket socket;
    private ReadThread readThread;

    public void execute(){
        try {
            socket = new Socket(host, port);
            readThread = new ReadThread(socket, this);
            readThread.start();
            new WriteThread(socket, this).start();
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    void setUserName(String name) {
        this.userName = name;
    }

    String getUserName() {
        return this.userName;
    }

    void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (readThread != null && !readThread.isInterrupted()) {
                readThread.interrupt();
            }
        } catch (IOException ex) {
            System.out.println("Error closing socket: " + ex.getMessage());
        }
    }

    public static void main(String[] args){
        ChatClient chatClient = new ChatClient();
        chatClient.execute();
    }
}

class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient chatClient){
        this.socket = socket;
        this.client = chatClient;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e) {
            System.out.println("Error getting input stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String message = reader.readLine();
                if (message == null) {
                    System.out.println("Server has closed the connection.");
                    break;
                }
                if(client.getUserName() != null){
                    System.out.println(message);
                }
            } catch(IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Error reading from server: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
        }
        client.closeSocket();
    }
}

class WriteThread extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private ChatClient client;
    BufferedReader br;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;
        this.br = new BufferedReader(new InputStreamReader(System.in));

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error getting output stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            System.out.print("Enter your name: ");
            String userName = br.readLine();
            client.setUserName(userName);
            writer.println(userName);

            String text;

            do {
                text = br.readLine();
                writer.println(text);
            } while (!text.equalsIgnoreCase("bye"));

            client.closeSocket();

        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}

