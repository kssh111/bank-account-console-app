public class Loading {
    public static void show(String text){
        String anim = "|/-\\";
        System.out.print(text+ " ");

        for (int i = 0; i < 10; i++) {
            System.out.print("\r" + text + " " + anim.charAt(i % anim.length()));
            try { Thread.sleep(120); } catch (Exception ignored) {}
        }
        System.out.println();


    }
}
