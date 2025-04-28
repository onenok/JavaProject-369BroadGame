package bg369;
import java.awt.HeadlessException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.swing.JOptionPane;

import game.Game;
import myutil.Localization;

@SuppressWarnings("CallToPrintStackTrace")
public class bg369 {
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        try {
            // Set System.out to use UTF-8 encoding
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Test output UTF-8 characters
        System.out.println("testing if UTF-8 is using");
        System.out.println("你好，世界！");
        System.out.println("こんにちは、世界！");
        System.out.println("안녕하세요, 세계!");
        System.out.println("The above word should be not Garbled characters.");

        try {
            // Use a dialog box to ask for language selection
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

            // Initialize language based on selection
            Locale locale;
            if (choice == 0) {
                locale = new Locale("en", "US"); // en_US
            } else {
                locale = new Locale("zh", "TW"); // zh_TW
            }
            Localization.init(locale);

            
            // Use a dialog box to ask whether to use GUI
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
            
            // If user closes the dialog, default to GUI
            if (choiceGUI == -1) {
                choiceGUI = 1; // default to GUI
            }
            final boolean useGUI = (choiceGUI == 1);
            Game game = new Game(useGUI, true);
        } catch (HeadlessException e) {
            // If GUI fails to start, fall back to console mode
            System.out.println(Localization.getString("bg369.GUI_Failed"));
            Game game = new Game(false, true);
        }
    }
    
}
