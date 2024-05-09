import java.util.ArrayList;
import java.util.List;

/**
 * @author wangkeyao
 *
 * 内存溢出，无限占用系统内存
 */
public class Main {
    public static void main(String[] args) {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[10000]);
        }
    }
}
