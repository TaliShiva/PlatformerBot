public class Guard {
    public static boolean isNull(Object o, String objName) {
        if (o == null) {
            System.out.println(objName);
            return true;
        }else {
            return false;
        }
    }
}
