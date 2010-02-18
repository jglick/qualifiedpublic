package p;
import qualifiedpublic.Public;
public class PartAPI {
    public void m1() {}
    public static @Public("p.User1") void m2() {}
    public @Public("p.User2") void m3() {}
}
