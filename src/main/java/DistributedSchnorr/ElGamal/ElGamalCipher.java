package DistributedSchnorr.ElGamal;

import java.math.BigInteger;


public class ElGamalCipher {
    private final BigInteger k1;
    private final BigInteger k2;

    public ElGamalCipher(BigInteger k1, BigInteger k2) {
        this.k1 = k1;
        this.k2 = k2;
    }

    public BigInteger getK1() {
        return k1;
    }

    public BigInteger getK2() {
        return k2;
    }
}
