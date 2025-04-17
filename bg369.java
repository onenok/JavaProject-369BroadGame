import java.awt.HeadlessException;
import javax.swing.JOptionPane;

@SuppressWarnings("unused")
public class bg369 {
    public static void main(String[] args) {
        try {
            // 使用圖形對話框詢問是否使用GUI
            String[] options = {"控制台版本", "GUI版本"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "請選擇遊戲界面類型：",
                    "369 棋盤遊戲",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );
            
            // 如果用戶關閉對話框，默認GUI
            if (choice == -1) {
                choice = 1; // 默認GUI
            }
            final boolean useGUI = (choice == 1);
            Game game = new Game(useGUI, true);
        } catch (HeadlessException e) {
            // 如果GUI無法啟動，回退到控制台模式
            System.out.println("GUI 初始化失敗，將使用控制台模式。");
            Game game = new Game(false, true);
        }
    }
    
}
