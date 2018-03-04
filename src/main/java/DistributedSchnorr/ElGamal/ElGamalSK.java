package DistributedSchnorr.ElGamal;

import java.math.BigInteger;

public class ElGamalSK {
    private final BigInteger p;
    private final BigInteger x;

    public ElGamalSK(BigInteger p, BigInteger x) {
        this.p = p;
        this.x = x;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getX() {
        return x;
    }
}
