package com.bijlipay.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class AttributeEncryptor {
    private static final String AES = "AES";
    private static final String SECRET = "c6aHe%z_D}MxbCf#";
    private final Key key = new SecretKeySpec("c6aHe%z_D}MxbCf#".getBytes(), "AES");
    private final Cipher cipher = Cipher.getInstance("AES");

    public AttributeEncryptor() throws Exception {
    }

    public String encrypt(String attribute) {
        try {
            this.cipher.init(1, this.key);
            return Base64.getEncoder().encodeToString(this.cipher.doFinal(attribute.getBytes()));
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }

    public String decrypt(String dbData) {
        try {
            this.cipher.init(2, this.key);
            return new String(this.cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}