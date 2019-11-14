import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServerTest implements Runnable {

    Socket socket;
    BufferedReader in = null;
    PrintWriter out= null;
    ArrayList<String> replyList;
    ArrayList<String> messageList;

    ServerTest(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        replyList = new ArrayList<>();
        messageList = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {

        final ServerSocket serverSocket = new ServerSocket(40800);

        while (true) {
            Socket sock = serverSocket.accept();
            System.out.println("Connected");
            Thread test  = new Thread(new ServerTest(sock));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            test.start();
        }
    }

    @Override
    public void run() {
        try {

            while (true) {
                String receivedMsg;
                while ((receivedMsg = in.readLine()) != null) {
                    messageList.add(receivedMsg);
                    System.out.println("receivedMsg - " + receivedMsg);
                    String replyMsg = "message was received successfully - " + new JSONObject(receivedMsg).get("tuid");
                    replyList.add(replyMsg);
                    Thread.sleep(200);
                    handleReplyList();
                }
            }

        } catch(IOException | InterruptedException | JSONException e) {
            e.printStackTrace();

        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleReplyList() throws JSONException {
        if(!messageList.isEmpty()) {
            try {
                new MySqlHelper().ConnectToMySql(new JSONObject( messageList.get(messageList.size()-1)));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(!replyList.isEmpty()) {
            out.write(replyList.get(replyList.size()-1) +"\n");
            out.flush();
        }

    }
}
