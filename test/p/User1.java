package p;
public class User1 {
    public User1() {
        new PartAPI().m1();
        PartAPI.m2();
    }
    static {
        PartAPI.m2();
    }
    static class Nested {
        static {
            new PartAPI().m1();
        }
    }
}
