package com.eldar.eldar.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptUtil {

    private static final String ALGORITHM = "AES";
    private static String base64Key;

    public static void generarClaveAES() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        setBase64Key(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    public static SecretKey obtenerClaveAES() {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static String encriptar(String textoPlano) throws Exception {
        SecretKey secretKey = obtenerClaveAES();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] textoEncriptado = cipher.doFinal(textoPlano.getBytes());
        return Base64.getEncoder().encodeToString(textoEncriptado);
    }

    public static String desencriptar(String textoCifrado) throws Exception {
        SecretKey secretKey = obtenerClaveAES();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] textoCifradoBytes = Base64.getDecoder().decode(textoCifrado);
        byte[] textoDesencriptado = cipher.doFinal(textoCifradoBytes);
        return new String(textoDesencriptado);
    }

    public static void setBase64Key(String base64Key) {
        EncryptUtil.base64Key = base64Key;
    }
}
