package utils;

public class StringTests {

    public static void main(String[] args) {
        String one = " \"Akeu Coucou!\"  ";
        String two = StringUtils.trimDoubleQuotes(one);
        System.out.printf("[%s] becomes [%s]\n", one, two);

        one = " 'Coucou Larigou!'  ";
        two = StringUtils.trimSimpleQuotes(one);
        System.out.printf("[%s] becomes [%s]\n", one, two);
    }
}
