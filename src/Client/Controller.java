package Client;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Server.ClientHandler;
import animatefx.animation.FadeIn;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller extends Thread implements Initializable {
    @FXML
    public Label clientName;
    @FXML
    public Pane chat;
    @FXML
    public TextField msgField;
    @FXML
    public TextArea msgRoom;
    @FXML
    public Label processId;
    @FXML
    public Label startTime;
    @FXML
    public Label totalMemory;
    @FXML
    public Pane profile;
    @FXML
    public Button profileBtn;
    @FXML
    public ImageView proImage;
    @FXML
    public Circle showProPic;
    public boolean toggleChat = false, toggleProfile = false;
    public Label memoryUsage;
    public Label cpuUsage;
    public Label activeThread;

    BufferedReader reader;
    PrintWriter writer;
    Socket socket;
    String clientId;
    ArrayList<String> msgArray = new ArrayList<>();
    ArrayList<Integer> lastMessageIndexes = new ArrayList<>();

    public void connectSocket() {
        try {
            socket = new Socket("localhost", 8889);
            System.out.println("Socket is connected with server!");
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            clientId = reader.readLine();
            clientName.setText("Process " + clientId);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = reader.readLine();
                String[] tokens = msg.split(" ");

                if(tokens[1].equalsIgnoreCase("undo")){
                    undo(Integer.parseInt(tokens[2]), tokens[0]);
                    continue;
                }

                String senderId = tokens[1];
                System.out.println(senderId);
                StringBuilder fullMsg = new StringBuilder();
                for(int i = 1; i < tokens.length; i++) {
                    fullMsg.append(tokens[i]);
                }
                System.out.println(fullMsg);
                if (senderId.equalsIgnoreCase( clientId + ":")) {
                    lastMessageIndexes.add(msgArray.isEmpty() ? 1 : msgArray.size());
                    continue;
                }
                else if(fullMsg.toString().equalsIgnoreCase("bye")) {
                    break;
                }
                msgRoom.appendText(msg + "\n");
                msgArray.add(msg + "\n");
            }
            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleProfileBtn(ActionEvent event) {
        if (event.getSource().equals(profileBtn) && !toggleProfile) {
            new FadeIn(profile).play();
            profile.toFront();
            chat.toBack();
            toggleProfile = true;
            toggleChat = false;
            profileBtn.setText("Back");
            setProfile();
        } else if (event.getSource().equals(profileBtn) && toggleProfile) {
            new FadeIn(chat).play();
            chat.toFront();
            toggleProfile = false;
            toggleChat = false;
            profileBtn.setText("Inform");
        }
    }

    public void setProfile() {
        processId.setText(getProcessID());
        processId.setOpacity(1);
        startTime.setText(getStartTime());
        startTime.setOpacity(1);
        totalMemory.setText(getTotalMemory());
        totalMemory.setOpacity(1);
        memoryUsage.setText(getMemoryUsage());
        memoryUsage.setOpacity(1);
        cpuUsage.setText(getCpuUsage());
        cpuUsage.setOpacity(1);
        activeThread.setText(getActiveThread());
        activeThread.setOpacity(1);
    }

    private String getProcessID(){
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        long processId = Long.parseLong(processName.split("@")[0]);
        return processId + "";
    }

    private String getStartTime(){
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long startTime = runtimeMXBean.getStartTime();
        Date startDate = new Date(startTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        return dateFormat.format(startDate);
    }

    private String getTotalMemory(){
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        return totalMemory + " MB";
    }

    private String getMemoryUsage(){
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return ((totalMemory - freeMemory) / (1024 * 1024)) + " MB";
    }

    private String getCpuUsage(){
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
        double cpuUsage = sunOsBean.getProcessCpuLoad();
        String formattedCpuUsage = String.format("%.2f", cpuUsage * 100);
        return formattedCpuUsage + "%";
    }

    private String getActiveThread(){
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }
        int activeThreadCount = rootGroup.activeCount();
        return activeThreadCount + "";
    }

    public void handleSendEvent(MouseEvent event) {
        send();
    }


    public void send() {
        if(!lastMessageIndexes.isEmpty() && msgField.getText().equalsIgnoreCase("undo") ){
            writer.println(clientId + " undo " + (lastMessageIndexes.get(lastMessageIndexes.size() - 1)));
            msgField.setText("");
            return;
        }

        String msg = msgField.getText();

        if(!msg.isEmpty()){
            System.out.println("Message: " + msg);

            writer.println("Process " + clientId + ": " + msg);
            msgRoom.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            msgRoom.appendText("Me: " + msg + "\n");
            msgArray.add("Me: " + msg + "\n");

            msgField.setText("");
            if(msg.equalsIgnoreCase("BYE") || (msg.equalsIgnoreCase("logout"))) {
                System.exit(0);
            }
        }
    }

    public void undo(int lastMessageIndex, String senderId){
        System.out.println(lastMessageIndex);
        msgArray.set(lastMessageIndex - 1, "");
        msgRoom.setText("");
        if(senderId.equalsIgnoreCase(clientId)) {
            lastMessageIndexes.remove(lastMessageIndexes.size() - 1);
        }
        for (String i : msgArray) {
            msgRoom.appendText(i);
        }
    }

    public void sendMessageByKey(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            send();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showProPic.setStroke(Color.valueOf("#90a4ae"));
        Image image;
        image = new Image("icons/processImg.png", false);
        showProPic.setFill(new ImagePattern(image));
        clientName.setText("Process " );
        connectSocket();
    }
}