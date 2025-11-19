public class Main {
    public static void main(String[] args) {
        try {
            // Console қолданбасын іске қосу
            ConsoleApp app = new ConsoleApp();
            app.run();

        } catch (Exception e) {
            System.err.println(Colors.RED +
                    "Critical error occurred: " + e.getMessage() +
                    Colors.RESET);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
