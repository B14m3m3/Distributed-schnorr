package DistributedSchnorr;


import java.math.BigInteger;
import java.security.MessageDigest;

public class Schnorr {
    public static BigInteger sign(BigInteger e, BigInteger V,
                                  BigInteger x, BigInteger m,
                                  BigInteger q){
        BigInteger hash = hashConcat(m,V);
        return e.add(hash.multiply(x)).mod(q);
    }

    public static boolean verify(BigInteger V, BigInteger m,
                                 BigInteger G, BigInteger Y,
                                 BigInteger p, BigInteger sigma){
        BigInteger left = G.modPow(sigma,p);
        BigInteger hash = hashConcat(m,V);
        BigInteger right = V.multiply(Y.modPow(hash,p)).mod(p);
        return left.equals(right);
    }

    public static BigInteger hashConcat(BigInteger m, BigInteger V){
        BigInteger concat = new BigInteger(m.toString() + V.toString());
        return hash(concat);
    }

    private static BigInteger hash(BigInteger big) {
        byte[] hash = new byte[0];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(big.toByteArray());
        } catch (Exception e) {
            System.out.println("hash error: " + e.getMessage());
        }

        return new BigInteger(hash);
    }
}
