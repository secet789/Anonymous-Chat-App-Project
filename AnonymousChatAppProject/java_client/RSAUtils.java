package java_client;

import java.security.*;

import java.security.spec.*;

import java.io.*;

import javax.crypto.Cipher;

import java.util.Base64;



public class RSAUtils {



    private static final int KEY_SIZE = 2048;

    private static final String PUBLIC_KEY_FILE = "keys/public_key.pem";

    private static final String PRIVATE_KEY_FILE = "keys/private_key.pem";



    // Anahtar çifti üret ve dosyaya yaz

    public static void generateKeyPair() throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

        keyGen.initialize(KEY_SIZE);

        KeyPair pair = keyGen.generateKeyPair();



        // klasörü oluştur

        File keyFolder = new File("keys");

        if (!keyFolder.exists()) {

            keyFolder.mkdirs();

        }



        // Public Key → PEM formatı

        try (FileOutputStream out = new FileOutputStream(PUBLIC_KEY_FILE)) {

            out.write(pair.getPublic().getEncoded());

        }



        // Private Key

        try (FileOutputStream out = new FileOutputStream(PRIVATE_KEY_FILE)) {

            out.write(pair.getPrivate().getEncoded());

        }



        System.out.println("RSA key pair successfully generated.");

    }



    // Public Key oku

    public static PublicKey loadPublicKey() throws Exception {

        byte[] bytes = readFile(PUBLIC_KEY_FILE);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);

    }



    // Private Key oku

    public static PrivateKey loadPrivateKey() throws Exception {

        byte[] bytes = readFile(PRIVATE_KEY_FILE);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);

        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(spec);

    }



    // Şifrele (public key ile)

    public static String encrypt(String message, PublicKey publicKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(message.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);

    }



    // Çöz (private key ile)

    public static String decrypt(String encrypted, PrivateKey privateKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));

        return new String(decrypted);

    }



    // Yardımcı: Dosya oku

    private static byte[] readFile(String filename) throws IOException {

        File file = new File(filename);

        byte[] data = new byte[(int) file.length()];

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {

            dis.readFully(data);

        }

        return data;

    }

}

