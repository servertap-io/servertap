package io.servertap.services;

import com.google.gson.JsonObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.*;
import io.servertap.enums.RequestType;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import static io.jsonwebtoken.SignatureAlgorithm.RS256;

public class TokenValidator {
    private JsonObject keys;
    private String audience;
    public TokenValidator(String audience) {
        this.getKeys();
        this.audience = audience;
    }



    public void getKeys() {
        RequestBuilder rb = new RequestBuilder("https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com");
        try {
            this.keys = rb.getPublicKeysJson();
            System.out.println(this.keys);
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Error getting tokens");
        }

    }

    public PublicKey generateJwtKeyDecryption(String jwtPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.decodeBase64(jwtPublicKey);
        X509EncodedKeySpec x509EncodedKeySpec=new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("here");
            System.out.println(this.keys);
            SignedJWT signedJWT = SignedJWT.parse(authToken);
            JWSHeader header = signedJWT.getHeader();
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            String audience = claimsSet.getAudience().get(0);
            System.out.println("here 2");
            String kid = header.getKeyID();
            System.out.println(kid);
            System.out.println(audience);
            String publicKey = this.keys.get(kid).getAsString(); // getting the public key from firebase
            System.out.println(publicKey);
            System.out.println(audience);
            if (!audience.equals(this.audience)) {
                System.out.println(this.audience + " Invalid Audience " + audience);
                throw new IllegalArgumentException();
            }
            publicKey = formatPublicKey(publicKey);
            System.out.println(publicKey);

            Jwts.parser().setSigningKey(generateJwtKeyDecryption(publicKey)).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature: {}"+ e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: {}"+ e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: {}"+ e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: {}"+ e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: {}"+ e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("no such algorithm exception");
        } catch (InvalidKeySpecException e) {
            System.out.println("invalid key exception");
        } catch (ParseException | CertificateException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private String formatPublicKey(String x509Certificate) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(x509Certificate.getBytes()));

// Get the public key from the certificate
        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
// Convert the public key to PEM format
        StringWriter sw = new StringWriter();
        sw.write("-----BEGIN PUBLIC KEY-----\n");
        sw.write(java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        sw.write("\n-----END PUBLIC KEY-----");
        return sw.toString();
    }
}
