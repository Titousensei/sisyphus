package com.thefind.util;

/**
 * Fastest high-quality (non-cryptographic) 32-bits hash function
 *
 * @author Eric Gaudet
 */
public class MurmurHash3
{
  private final int seed_;

  private int value_;

  public MurmurHash3(int seed)
  {
    seed_ = seed;
    reset();
  }

  public final void reset()
  {
    value_ = seed_;
  }

  public void update(byte[] b)
  { value_ = MurmurHash3.calc(b, 0, b.length, value_); }

  public void update(byte v)
  {
    byte[] b = new byte[1];
    b[0] = v;
    value_ = MurmurHash3.calc(b, 0, b.length, value_);
  }

  public void update(short v)
  {
    byte[] b = new byte[2];
    b[0] = (byte) (v&0xFF);
    b[1] = (byte) ((v>>8)&0xFF);
    value_ = MurmurHash3.calc(b, 0, b.length, value_);
  }

  public void update(int v)
  {
    byte[] b = new byte[4];
    b[0] = (byte) (v&0xFF);
    b[1] = (byte) ((v>>8)&0xFF);
    b[2] = (byte) ((v>>16)&0xFF);
    b[3] = (byte) ((v>>24)&0xFF);
    value_ = MurmurHash3.calc(b, 0, b.length, value_);
  }

  public void update(float v)
  { update(Float.floatToRawIntBits(v)); }

  public void update(long v)
  {
    byte[] b = new byte[8];
    b[0] = (byte) (v&0xFF);
    b[1] = (byte) ((v>>8)&0xFF);
    b[2] = (byte) ((v>>16)&0xFF);
    b[3] = (byte) ((v>>24)&0xFF);
    b[4] = (byte) ((v>>32)&0xFF);
    b[5] = (byte) ((v>>40)&0xFF);
    b[6] = (byte) ((v>>48)&0xFF);
    b[7] = (byte) ((v>>56)&0xFF);
    value_ = MurmurHash3.calc(b, 0, b.length, value_);
  }

  public void update(double v)
  { update(Double.doubleToRawLongBits(v)); }

  public int getInt()
  { return value_; }

  public static int calc(byte[] data, int offset, int len, int seed)
  {
    int h1 = seed;
    int roundedEnd = offset + (len & 0xfffffffc); // round down to 4 byte block

    for (int i=offset; i<roundedEnd; i+=4) {
      // little endian load order
      int k1 = (data[i] & 0xff) | ((data[i+1] & 0xff) << 8) | ((data[i+2] & 0xff) << 16) | (data[i+3] << 24);
      k1 *= 0xcc9e2d51;
      k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
      k1 *= 0x1b873593;

      h1 ^= k1;
      h1 = (h1 << 13) | (h1 >>> 19); // ROTL32(h1,13);
      h1 = h1*5+0xe6546b64;
    }

    // tail
    int k1 = 0;

    switch(len & 0x03) {
      case 3:
        k1 = (data[roundedEnd + 2] & 0xff) << 16;
        // fallthrough
      case 2:
        k1 |= (data[roundedEnd + 1] & 0xff) << 8;
        // fallthrough
      case 1:
        k1 |= (data[roundedEnd] & 0xff);
        k1 *= 0xcc9e2d51;
        k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
        k1 *= 0x1b873593;
        h1 ^= k1;
    }

    // finalization
    h1 ^= len;

    // fmix(h1);
    h1 ^= h1 >>> 16;
    h1 *= 0x85ebca6b;
    h1 ^= h1 >>> 13;
    h1 *= 0xc2b2ae35;
    h1 ^= h1 >>> 16;

    return h1;
  }
}

