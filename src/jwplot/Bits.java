
package jwplot;

import java.math.BigInteger;
import java.util.*;

/**
 * Provides random bits, statically.
 * Kills VM exit code 2 on misuse.
 * */

public class Bits
{
    public static Random rng = null;
    static boolean locked = false;
    static boolean ready  = false;

    /**
       If not locked, seed the generator with the clock.
    */
    public static void init()
    {
        init(System.currentTimeMillis());
    }

    /**
       If not locked, seed the generator with the clock mod 1000.
       Useful for debugging.
       @param print If true, print the seed.
    */
    public static long init(boolean print)
    {
        long seed = System.currentTimeMillis() % 1000;
        init(seed);

        if (print)
            System.out.println("Bits.seed: " + seed);

        return seed;
    }

    /**
       If not locked, seed the generator
       @param seed The seed.
    */
    public static void init(long seed)
    {
        assert (!locked) : "Bits locked!";
        // System.out.println("Seeding RNG...");
        rng = new Random(seed);
        ready = true;
    }

    /**
       Lock the generator to prevent seeding
    */
    public static void lock()
    {
        locked = true;
    }

    /**
       Unlock the generator
    */
    public static void unlock()
    {
        locked = false;
    }

    public static double nextDouble()
    {
        if (! ready)
        {
            System.err.println("Bits not ready!");
            System.exit(2);
        }
        return rng.nextDouble();
    }

    public static double nextDouble(double d)
    {
        return rng.nextDouble() * d;
    }

    public static int nextInt()
    {
        if (! ready)
        {
            System.err.println("Bits not ready!");
            System.exit(2);
        }
        return rng.nextInt();
    }

    /**
       Return a integer in [0..t).
    */
    public static int nextInt(int t)
    {
        double d = nextDouble();
        int i = (new Double(d * t)).intValue();
        return i;
    }

    public static boolean nextBoolean()
    {
        if (! ready)
        {
            System.err.println("Bits not ready!");
            System.exit(2);
        }
        return rng.nextBoolean();
    }

    public static long nextLong()
    {
        if (! ready)
        {
            System.err.println("Bits not ready!");
            System.exit(2);
        }
        return rng.nextLong();
    }

    public static long nextLong(long t)
    {
        double d = nextDouble();
        long i = (new Double(d * t)).longValue();
        return i;
    }

    public static BigInteger nextBigInteger(BigInteger i)
    {
        if (i.equals(BigInteger.ZERO))
            return BigInteger.ZERO;

        int b = i.bitLength()+1;
        BigInteger result;
        do
        {
            result = BigInteger.valueOf(b);
        } while (result.compareTo(i) >= 0);

        return result;
    }

    public static void nextBytes(byte[] bytes)
    {
        rng.nextBytes(bytes);
    }
}
