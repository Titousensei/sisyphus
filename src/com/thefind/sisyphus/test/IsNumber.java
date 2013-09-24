package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IsNumber
extends TestColumns
{
  protected final boolean test_;

  public IsNumber(boolean test, String... columns)
  {
    super(columns);
    test_ = test;
  }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  {
    for (int i=0 ; i<that.length ; i++) {
      String cs = that[i];
      if (cs == null
      || "".equals(cs)
      || ".".equals(cs)
      || "-".equals(cs)
      || "-.".equals(cs)
      ) {
        return !test_;
      }
      int sz = cs.length();
      boolean has_dot = false;
      for (int j=0; j<sz; j++) {
        switch (cs.charAt(j)) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          break;
        case '-':
          if (j>0) return !test_;
          break;
        case '.':
          if (has_dot) return !test_;
          has_dot = true;
          break;
        default:
          return !test_;
        }
      }
    }
    return test_;
  }

  @Override
  public String toStringWhich()
  { return (test_) ? "~<number>" : "!~<number>"; }

  public static void main(String[] args)
  {
    IsNumber pos = new IsNumber(true);
    IsNumber neg = new IsNumber(false);
    System.out.println("POS " + pos.eval(args));
    System.out.println("NEG " + neg.eval(args));
  }
}

