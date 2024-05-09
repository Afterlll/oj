/**
 * @author wangkeyao
 * 测试使用 Arg 参数模式
 */
public class Main {
    public static void main(String[] args) {
        int a = Integer. parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println("结果 = " + (a + b));
    }
}