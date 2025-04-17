package myutil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class PrintUtils {
    private static final Scanner scanner = new Scanner(System.in);

    // 用於設定結束字符
    public static class PEnd {
        public String value;

        public PEnd() {
            this.value = "endl"; // 預設換行
        }

        public PEnd(String value) {
            this.value = value;
        }
    }

    // 用於設定逐字輸出
    public static class PVo {
        public int value; // 逐字輸出時延遲的毫秒數

        public PVo() {
            this.value = 0; // 預設不延遲
        }

        public PVo(int value) {
            this.value = value;
        }
    }
    // 用於input(As)方法，設定是否拆分輸入及指定拆分字符。
    public static class PSplit {
        public String splitChar;

        public PSplit() {
            this.splitChar = " ";
        }

        public PSplit(String splitChar) {
            this.splitChar = splitChar;
        }
    }

    /**
     * 通用的 print 方法，接受任意數量的參數。
     * 其中若參數為 PEnd 或 PVo 則視為選項，其他參數合併作為輸出字串。
     */
    public static void print(Object... args) {
        PEnd pEnd = null;
        PVo pVo = null;
        StringBuilder sb = new StringBuilder();

        for (Object arg : args) {
            if (arg instanceof PEnd pEnd1) {
                pEnd = pEnd1;
            } else if (arg instanceof PVo pVo1) {
                pVo = pVo1;
            } else if (!(arg instanceof PSplit)) {
                sb.append(to_String(arg));
            }
        }

        // 使用預設選項（若使用者未提供）
        if (pEnd == null) {
            pEnd = new PEnd();
        }
        if (pVo == null) {
            pVo = new PVo();
        }

        String text = sb.toString();
        // 根據 PVo 設定，決定是否逐字輸出與延遲時間
        if (pVo.value > 0) {
            for (char c : text.toCharArray()) {
                System.out.print(c);
                System.out.flush();
                try {
                    TimeUnit.MILLISECONDS.sleep(pVo.value);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            System.out.print(text);
        }

        // 根據 PEnd 的設定決定如何結束輸出
        if ("endl".equals(pEnd.value)) {
            System.out.println();
        } else {
            System.out.print(pEnd.value);
        }
    }

    /**
     * 基本輸入方法，會先用 print 方法輸出提示（可包含 PEnd 與 PVo 選項）。  
     * 使用範例（在相同 package 或透過 import 匯入 PEnd 與 PVo）：  
     *   String name = PrintUtils.input("請輸入名字：", new PEnd(""), new PVo(0));
     */
    @SuppressWarnings("unchecked")
    public static <T> T input(Object... args) {
        PSplit pSplit = null;
        boolean hasPEnd = false;
        for (Object arg : args) {
            if (arg instanceof PEnd) {
                hasPEnd = true;
                break;
            } else if (arg instanceof PSplit pSplit1) {
                pSplit = pSplit1;
            }
        }
        Object[] newArgs = args;
        if (!hasPEnd) {
            newArgs = java.util.Arrays.copyOf(args, args.length + 1);
            newArgs[newArgs.length - 1] = new PEnd("");
        }
        print(newArgs);
        
        String inputStr = scanner.nextLine();
        if (pSplit != null) {
            // 當有分割選項時，假設返回 String[]
            return (T) inputStr.split(pSplit.splitChar);
        }
        // 當沒有分割選項時，假設返回 String
        return (T) inputStr;
    }

    /**
     * 讀入一整行內容，並依照空白分隔，轉型後回傳 List&lt;T&gt;。  
     * 目前支援的型別有：Integer, Double, Float, String。
     */
    public static <T> java.util.List<T> inputAs(Class<T> type, Object... args) {
        boolean hasPSplit = false;
        // 先檢查是否有 PSplit 參數
        for (Object arg : args) {
            if (arg instanceof PSplit) {
                hasPSplit = true;
                break;
            }
        }
        
        Object[] newArgs = args;
        // 如果部份參數中沒有 PSplit，則創建一個新陣列，並在末尾新增一個 PSplit
        if (!hasPSplit) {
            newArgs = java.util.Arrays.copyOf(args, args.length + 1);
            newArgs[newArgs.length - 1] = new PSplit();
        }
        
        String[] strInput = input(newArgs);
        java.util.List<T> results = new java.util.ArrayList<>();
        for (String token : strInput) {
            if (type.equals(Integer.class)) {
                results.add(type.cast(Integer.valueOf(token)));
            } else if (type.equals(Double.class)) {
                results.add(type.cast(Double.valueOf(token)));
            } else if (type.equals(Float.class)) {
                results.add(type.cast(Float.valueOf(token)));
            } else if (type.equals(Boolean.class)) {
                results.add(type.cast(Boolean.valueOf(token)));
            } else if (type.equals(String.class)) {
                results.add(type.cast(token));
            } else {
                throw new RuntimeException("Unsupported type: " + type.getName());
            }
        }
        
        if (results.isEmpty()) {
            throw new RuntimeException("Invalid input, please enter a valid value");
        }
        return results;
    }
        /**
     * 格式化輸出，接受格式字串與對應參數，同時可附帶結束輸出與逐字延遲的選項。
     */
    public static void printf(String format, Object... args) {
        PEnd pEnd = null;
        PVo pVo = null;
        Object[] formatArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PEnd pEnd1) {
                pEnd = pEnd1;
            } else if (args[i] instanceof PVo pVo1) {
                pVo = pVo1;
            } else {
                formatArgs[i] = args[i];
            }
        }
        String output = String.format(format, formatArgs);
        print(output, pEnd, pVo);
    }

    public static String to_String(Object obj) {
        if (obj == null) {
            return "null";
        }
        Class<?> clazz = obj.getClass();
        // 處理陣列：無論是基本型別陣列或物件陣列
        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < length; i++) {
                Object element = Array.get(obj, i);
                sb.append(to_String(element));
                if (i < length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        // 處理 Collection
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            Iterator<?> it = coll.iterator();
            while (it.hasNext()) {
                sb.append(to_String(it.next()));
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        // 處理 Map
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> entry = it.next();
                sb.append(to_String(entry.getKey()));
                sb.append("=");
                sb.append(to_String(entry.getValue()));
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            return sb.toString();
        }
        // 以下處理其他物件
        // 如果該物件使用的是 Object 的預設 toString()，則用反射印出所有欄位
        try {
            Method toStringMethod = clazz.getMethod("toString");
            if (toStringMethod.getDeclaringClass().equals(Object.class)) {
                Field[] fields = clazz.getDeclaredFields();
                StringBuilder sb = new StringBuilder();
                sb.append(clazz.getSimpleName()).append("@[");
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    sb.append(fields[i].getName()).append("=");
                    sb.append(to_String(fields[i].get(obj)));
                    if (i < fields.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
                return sb.toString();
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            // 如果反射過程中有例外，則直接使用 obj.toString()
        }
        return obj.toString();
    }
}
