package Client;

import animatefx.animation.FadeIn;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class Controller {
    @FXML
    public Pane pnSignIn;
    @FXML
    public Pane pnSignUp;
    @FXML
    public Button btnSignUp;
    @FXML
    public Button getStarted;
    @FXML
    public ImageView btnBack;
    @FXML
    public TextField regFirstName;
    @FXML
    public Label controlRegLabel;
    @FXML
    public Label success;
    @FXML
    public Label goBack;
    @FXML
    public TextField privateKey;
    @FXML
    public Label loginNotifier;
    public static String n, d, e, fullName;
    public static ArrayList<User> loggedInUser = new ArrayList<>();
    public static ArrayList<User> users = new ArrayList<User>();
    public Label generatedPublicKey;
    public Label generatedPrivateKey;

    public void registration() {
        if (!regFirstName.getText().equalsIgnoreCase("")) {
            int [] keys = generateKey();
            User newUser = new User();
            newUser.fullName = regFirstName.getText();
            newUser.n = keys[0];
            newUser.publicKey = keys[1];
            newUser.privateKey = keys[2];
            users.add(newUser);

            goBack.setOpacity(1);
            success.setOpacity(1);
            generatedPublicKey.setText("Public Key: {" + newUser.publicKey + ", " + newUser.n + "}");
            generatedPrivateKey.setText("Private Key: {" + newUser.privateKey + ", " + newUser.n + "}");
            generatedPublicKey.setOpacity(1);
            generatedPrivateKey.setOpacity(1);

            makeDefault();
            if (controlRegLabel.getOpacity() == 1) {
                controlRegLabel.setOpacity(0);
            }
        } else {
            controlRegLabel.setOpacity(1);
            setOpacity(success, goBack, generatedPublicKey, generatedPrivateKey);
        }
    }

    private int[] generateKey(){
        int p = generateRandomPrimeNumber();
        int q = generateRandomPrimeNumber();
        int n = p * q;

        int phiN = (p - 1) * (q - 1);
        int e = getE(phiN);
        int d = getD(phiN, e);

        while (!checkKey(n) || e == d || p == q){
            p = generateRandomPrimeNumber();
            q = generateRandomPrimeNumber();
            n = p * q;

            phiN = (p - 1) * (q - 1);
            e = getE(phiN);
            d = getD(phiN, e);
        }

        return new int[]{n, e, d};
    }

    private int generateRandomPrimeNumber(){
        Random random = new Random();
        int prime = random.nextInt(50) + 1;

        while (!isPrime(prime)){
            prime = random.nextInt(50) + 1;
        }
        return prime;
    }

    private boolean isPrime(int num){
        if(num <= 1){
            return false;
        }
        for (int i = 2; i < num; i++) {
            if(num % i == 0){
                return false;
            }
        }
        return true;
    }

    private boolean checkKey(int n){
        for (User user : users){
            if (user.n == n){
                return false;
            }
        }
        return true;
    }

    private int getE(int phiN){
        int e = 0;
        for (int i = 2; i < phiN; i++) {
            if(gcd(i, phiN) == 1){
                e = i;
                break;
            }
        }
        return e;
    }

    private int gcd(int num1, int num2) {
        if (num2 == 0) {
            return num1;
        }
        return gcd(num2, num1 % num2);
    }

    private int getD(int phiN, int e){
        int d = 0;
        for (int i = 0; i <= phiN; i++) {
            if(i * e % phiN == 1){
                d = i;
                break;
            }
        }
        return d;
    }

    private void setOpacity(Label a, Label b, Label c, Label d) {
        if(a.getOpacity() == 1 || b.getOpacity() == 1 || c.getOpacity() == 1 || d.getOpacity() == 1) {
            a.setOpacity(0);
            b.setOpacity(0);
            c.setOpacity(0);
            d.setOpacity(0);
        }
    }


    private void setOpacity(Label controlRegLabel) {
        controlRegLabel.setOpacity(0);
    }

    private void makeDefault() {
        regFirstName.setText("");
        setOpacity(controlRegLabel);
    }


    public void login() {
        d = privateKey.getText().replaceAll(" ", "").split(",")[0];
        n = privateKey.getText().replaceAll(" ", "").split(",")[1];
        boolean login = false;
        for (User x : users) {
            if (x.n == Integer.parseInt(n) && x.privateKey == Integer.parseInt(d)) {
                fullName = x.fullName;
                e = "" + x.publicKey;
                login = true;
                loggedInUser.add(x);
                System.out.println(x.fullName);
                break;
            }
        }
        if (login) {
            changeWindow();
        } else {
            loginNotifier.setOpacity(1);
        }
    }

    public void changeWindow() {
        try {
            Stage stage = (Stage) regFirstName.getScene().getWindow();
            Parent root = FXMLLoader.load(this.getClass().getResource("Room.fxml"));
            stage.setScene(new Scene(root, 330, 560));
            stage.setTitle(fullName.split(" ")[0]);
            stage.setOnCloseRequest(event -> {
                System.exit(0);
            });
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (event.getSource().equals(btnSignUp)) {
            new FadeIn(pnSignUp).play();
            pnSignUp.toFront();
        }
        if (event.getSource().equals(getStarted)) {
            new FadeIn(pnSignIn).play();
            pnSignIn.toFront();
        }
        loginNotifier.setOpacity(0);
        privateKey.setText("");
    }

    @FXML
    private void handleMouseEvent(MouseEvent event) {
        if (event.getSource() == btnBack) {
            new FadeIn(pnSignIn).play();
            pnSignIn.toFront();
        }
        regFirstName.setText("");
    }
}