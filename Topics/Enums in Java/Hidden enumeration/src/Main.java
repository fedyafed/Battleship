import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
    // write your program here
        long count = Arrays.stream(Secret.values())
                .map(Secret::name)
                .filter(s -> s.startsWith("STAR"))
                .count();
        System.out.println(count);
    }
}

/*// sample enum for inspiration
   enum Secret {
    STAR, CRASH, START, // ...
}*/
