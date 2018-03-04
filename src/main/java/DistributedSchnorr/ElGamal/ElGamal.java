package DistributedSchnorr.ElGamal;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class ElGamal {

    private static BigInteger TWO = new BigInteger("2");
    private static int bitSize = 100; // made smaller to faster run tests

    public static ElGamalKeyPair KeyGen() {
        BigInteger[] primes = getPrimes(bitSize, new SecureRandom());
        BigInteger p = primes[0];
        BigInteger q = primes[1];

        BigInteger h = randNum(p, new SecureRandom());

        while (!h.modPow(q, p).equals(BigInteger.ONE)) {
            if (h.modPow(q.multiply(ElGamal.TWO), p).equals(BigInteger.ONE))
                h = h.modPow(TWO, p);
            else
                h = randNum(p, new SecureRandom());
        }
        BigInteger x = randNum(q.subtract(BigInteger.ONE), new SecureRandom());
        BigInteger g = h.modPow(x, p);

        ElGamalPK pk = new ElGamalPK(p, h, g, q);
        ElGamalSK sk = new ElGamalSK(p, x);
        return new ElGamalKeyPair(pk, sk);
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

    public static ElGamalCipher Encrypt(BigInteger p, BigInteger g, BigInteger h, BigInteger message) {
        BigInteger q = p.subtract(BigInteger.ONE).divide(ElGamal.TWO);
        BigInteger r = randNum(q, new SecureRandom());
        BigInteger k1 = g.modPow(r, p);
        BigInteger enc = h.modPow(r, p);
        BigInteger k2 = message.xor(hash(enc));
        return new ElGamalCipher(k1, k2);

    }

    public static BigInteger Decrypt(BigInteger p, BigInteger x, BigInteger k1, BigInteger k2) {
        BigInteger s = k1.modPow(x, p);
        return k2.xor(hash(s));
    }

    private static BigInteger[] getPrimes(int nb_bits, SecureRandom rand) {
        int certainty = 40;

        BigInteger q = new BigInteger(nb_bits, certainty, rand);
        BigInteger p = q.multiply(TWO).add(BigInteger.ONE);

        while (!p.isProbablePrime(certainty)) {
            q = new BigInteger(nb_bits, certainty, rand);
            p = q.multiply(TWO).add(BigInteger.ONE);
        }

        return new BigInteger[]{p, q};
    }

    public static BigInteger randNum(BigInteger N, SecureRandom rand) {
        return new BigInteger(N.bitLength() + 100, rand).mod(N);
    }
}