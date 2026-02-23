package java_client;



import javax.swing.*;

import javax.swing.border.TitledBorder;



import java_client.MessageBuilder;

import org.json.JSONObject;



import java.awt.*;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.security.PublicKey;

import java.util.Base64;



public class ChatAppGUI extends JFrame {



    private JTextArea chatArea;

    private JTextField messageField;

    private JButton sendButton;

    private DefaultListModel<String> listModel;

    private JList<String> userList;

    private String currentNickname = null;

    private String mode = "Client"; // "Client" veya "Gateway" modu



    public ChatAppGUI() {

        setTitle("Anonymous Chat");

        setSize(600, 450);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        selectMode();       // Kullanıcıdan mod seçimi

        initializeUI();     // Arayüzü oluştur

        new PythonListener(listModel).start(); // Python listener'ı başlat

    }



    // Kullanıcıdan "Client" veya "Gateway" modunu seçmesini ister

    private void selectMode() {

        String[] options = {"Client", "Gateway"};

        mode = (String) JOptionPane.showInputDialog(

            null, "Select mode:", "Mode Selection",

            JOptionPane.QUESTION_MESSAGE, null, options, options[0]

        );

        if (mode == null) System.exit(0);

    }



    // Ana GUI arayüzü buradan oluşturulur

    private void initializeUI() {

        // Menü barı ve seçenekleri

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");



        JMenuItem generateKeysItem = new JMenuItem("Generate Keys");

        JMenuItem connectItem = new JMenuItem("Connect to Network");

        JMenuItem disconnectItem = new JMenuItem("Disconnect");

        JMenuItem exitItem = new JMenuItem("Exit");



        generateKeysItem.addActionListener(e -> generateKeys());

        connectItem.addActionListener(e -> connectToNetwork());

        disconnectItem.addActionListener(e -> disconnectFromNetwork());

        exitItem.addActionListener(e -> System.exit(0));



        fileMenu.add(generateKeysItem);

        fileMenu.add(connectItem);

        fileMenu.add(disconnectItem);

        fileMenu.add(exitItem);



        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(

            this, "   Anonymous Chat Project\n Developed by Selin Ece Tandır"

        ));

        helpMenu.add(aboutItem);



        menuBar.add(fileMenu);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);



        // Chat mesajlarının gösterildiği alan

        chatArea = new JTextArea();

        chatArea.setEditable(false);

        JScrollPane chatScroll = new JScrollPane(chatArea);



        // Mesaj giriş alanı ve gönderme butonu

        messageField = new JTextField();

        sendButton = new JButton("Send");

        sendButton.addActionListener(e -> sendMessage());



        JPanel inputPanel = new JPanel(new BorderLayout());

        inputPanel.add(messageField, BorderLayout.CENTER);

        inputPanel.add(sendButton, BorderLayout.EAST);



        // Online kullanıcı listesi

        listModel = new DefaultListModel<>();

        userList = new JList<>(listModel);

        userList.setBorder(new TitledBorder("Online Users"));

        JScrollPane userScroll = new JScrollPane(userList);



        // Arayüz layout'u

        setLayout(new BorderLayout());

        add(chatScroll, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.SOUTH);

        add(userScroll, BorderLayout.EAST);



        setVisible(true);

    }



    // "Generate Keys" menü butonuna tıklandığında çağrılır

    private void generateKeys() {

        try {

            RSAUtils.generateKeyPair();

            JOptionPane.showMessageDialog(this, "RSA key pair successfully generated!");

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());

        }

    }



    // Kullanıcı "Connect to Network" tıkladığında çalışır

    private void connectToNetwork() {

        currentNickname = JOptionPane.showInputDialog(this, "Enter your nickname:");

        if (currentNickname != null && !currentNickname.trim().isEmpty()) {

            chatArea.append("Connected as " + currentNickname + "\n");



            try {

                PublicKey pubKey = RSAUtils.loadPublicKey();

                String pubKeyEncoded = Base64.getEncoder().encodeToString(pubKey.getEncoded());



                // JOIN mesajı JSON formatında oluşturulur

                String jsonStr = MessageBuilder.buildJoinMessage(currentNickname, pubKeyEncoded);



                // JOIN mesajı Python üzerinden UDP olarak yayınlanır

                ProcessBuilder pb = new ProcessBuilder("python3", "python_backend/spoof_sender.py", jsonStr);

                pb.redirectErrorStream(true);

                pb.start();



            } catch (Exception ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(this, "Error during connect: " + ex.getMessage());

            }

        }

    }



    // "Disconnect" tıklanınca çağrılır

    private void disconnectFromNetwork() {

        chatArea.append("Disconnected from network.\n");



        try {

            String jsonStr = MessageBuilder.buildQuitMessage(currentNickname);



            ProcessBuilder pb = new ProcessBuilder("python3", "python_backend/spoof_sender.py", jsonStr);

            pb.redirectErrorStream(true);

            Process p = pb.start();



        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Error while disconnecting: " + ex.getMessage());

            ex.printStackTrace();

        }

    }





    // Kullanıcı mesaj yazıp "Send" tıklayınca çağrılır

    private void sendMessage() {

        String message = messageField.getText().trim();

        if (!message.isEmpty()) {

            chatArea.append(currentNickname + ": " + message + "\n");

            messageField.setText("");



            try {

                PublicKey pubKey = RSAUtils.loadPublicKey(); // Geçici: kendi public key'in

                String encrypted = RSAUtils.encrypt(message, pubKey); // Şifrele



                String jsonStr = MessageBuilder.buildChatMessage(currentNickname, encrypted);



                ProcessBuilder pb = new ProcessBuilder("python3", "python_backend/spoof_sender.py", jsonStr);

                pb.redirectErrorStream(true);

                Process process = pb.start();



                // Python çıktısını terminale yaz

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;

                while ((line = reader.readLine()) != null) {

                    System.out.println("[Python] " + line);

                }



            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error while sending: " + ex.getMessage());

                ex.printStackTrace();

            }

        }

    }



    // Arka planda çalışan thread — Python listener'ı başlatır

    class PythonListener extends Thread {

        private DefaultListModel<String> listModel;



public PythonListener(DefaultListModel<String> listModel) {

    this.listModel = listModel;

}



@Override

public void run() {

    try {

        ProcessBuilder pb = new ProcessBuilder("python3", "python_backend/listener.py");

        pb.redirectErrorStream(true);

        Process process = pb.start();



        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;

        while ((line = reader.readLine()) != null) {

            System.out.println("[listener.py] " + line);



            if (line.startsWith("JOIN:")) {

                String nickname = line.substring(5).trim();

                SwingUtilities.invokeLater(() -> {

                    if (!listModel.contains(nickname)) {

                        listModel.addElement(nickname);

                    }

                });



            }

            else if (line.startsWith("[CHAT]")) {



                // "[CHAT]" + bir boşluk = 7 karakter atla

                String payload = line.substring(7).trim();



                try {

                    if (payload.startsWith("{")) {             // JSON biçimi

                        JSONObject obj  = new JSONObject(payload);

                        String nick     = obj.optString("nickname");

                        String enc      = obj.optString("message");



                        String plain;

                        try { plain = RSAUtils.decrypt(enc, RSAUtils.loadPrivateKey()); }

                        catch (Exception ex) { plain = "(çözülemedi)"; }



                        String out = nick + ": " + plain;

                        SwingUtilities.invokeLater(() -> chatArea.append(out + "\n"));

                        System.out.println(out);



                    } else {                                   // "nick: <b64>" eski biçim

                        int idx = payload.indexOf(':');

                        if (idx == -1) return;                 // format hatalıysa yoksay



                        String nick = payload.substring(0, idx).trim();

                        String enc  = payload.substring(idx + 1).trim();



                        String plain;

                        try { plain = RSAUtils.decrypt(enc, RSAUtils.loadPrivateKey()); }

                        catch (Exception ex) { plain = "(çözülemedi)"; }



                        String out = nick + ": " + plain;

                        SwingUtilities.invokeLater(() -> chatArea.append(out + "\n"));

                        System.out.println(out);

                    }

                } catch (Exception ex) {

                    ex.printStackTrace();

                }

            }

                else if (line.startsWith("[-]")) {

                String nickname = line.substring(4).trim();

                SwingUtilities.invokeLater(() -> {

                    listModel.removeElement(nickname);

                    chatArea.append(nickname + " has disconnected.\n");

                });

            }

        }

    } 

        catch (Exception e) {

         e.printStackTrace();

         }

}



        }





    // Programın başlangıç noktası

    public static void main(String[] args) {

        SwingUtilities.invokeLater(ChatAppGUI::new);

    }

}

