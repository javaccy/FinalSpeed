import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by javaccy on 2017/3/1.
 */
public class MainTest {

    public static void main(String[] args) throws IOException {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setBackground(Color.red);
                frame.setSize(500,500);
                frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowEvent.WINDOW_CLOSED);
            }
        });


        ServerSocket socket = new ServerSocket(7000);
        Socket accept = socket.accept();
        InputStream in = accept.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String s = reader.readLine();
        System.out.println("content :" + s);


    }

}
