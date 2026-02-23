package java_client;



import org.json.JSONObject;



public class MessageBuilder {



    public static String buildJoinMessage(String nickname, String publicKey) {

        JSONObject json = new JSONObject();

        json.put("type", "JOIN");

        json.put("nickname", nickname);

        json.put("public_key", publicKey);

        return json.toString();

    }



    public static String buildChatMessage(String nickname, String encryptedMessage) {

        JSONObject json = new JSONObject();

        json.put("type", "CHAT");

        json.put("nickname", nickname);

        json.put("message", encryptedMessage);

        return json.toString();

    }



    public static String buildQuitMessage(String nickname) {

        JSONObject obj = new JSONObject();

        obj.put("type", "QUIT");

        obj.put("nickname", nickname);

        return obj.toString();

    }



}

