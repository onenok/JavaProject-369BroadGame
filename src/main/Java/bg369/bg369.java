package bg369;
import java.awt.HeadlessException;
import java.util.Locale;

import javax.swing.JOptionPane;

import game.Game;
import myutil.Localization;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

@SuppressWarnings("unused")
public class bg369 {
    public static void main(String[] args) {
        try {
            // 將 System.out 設定為使用 UTF-8 編碼
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 測試輸出 UTF-8 字元
        System.out.println("你好，世界！");
        System.out.println("こんにちは、世界！");
        System.out.println("안녕하세요, 세계!");
        try {
            // 使用圖形對話框詢問語言
            String[] languages = {"English", "繁體中文"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "請選擇語言 / Select Language",
                    "Language",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    languages,
                    languages[0]
            );

            // 根據選擇初始化語言
            Locale locale;
            if (choice == 0) {
                locale = new Locale("en", "US"); // 英文
            } else {
                locale = new Locale("zh", "TW"); // 繁體中文
            }
            Localization.init(locale);

            
            // 使用圖形對話框詢問是否使用GUI
            String[] options = {Localization.getString("bg369.options.console"), Localization.getString("bg369.options.gui")};
            int choiceGUI = JOptionPane.showOptionDialog(
                    null,
                    Localization.getString("bg369.options.message"),
                    Localization.getString("title"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );
            
            // 如果用戶關閉對話框，默認GUI
            if (choiceGUI == -1) {
                choiceGUI = 1; // 默認GUI
            }
            final boolean useGUI = (choiceGUI == 1);
            Game game = new Game(useGUI, true);
        } catch (HeadlessException e) {
            // 如果GUI無法啟動，回退到控制台模式
            System.out.println(Localization.getString("bg369.GUI_Failed"));
            Game game = new Game(false, true);
        }
    }
    
}
