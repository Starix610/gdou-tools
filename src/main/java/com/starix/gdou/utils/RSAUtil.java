package com.starix.gdou.utils;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * @author Starix
 * @date 2020-07-05 22:55
 */
public class RSAUtil {

    public static String encrypt(String modules, String exponent, String str) throws Exception {
        byte[] bModules = Base64.getDecoder().decode(modules);
        byte[] bExponent = Base64.getDecoder().decode(exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(bModules), new BigInteger(bExponent));
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] encryptData = cipher.doFinal(str.getBytes());
        // rsa加密结果再进行base64加密
        return Base64.getEncoder().encodeToString(encryptData);
    }

}
