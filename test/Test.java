public class Test<E> {
    private E name;

    private static int i = 50;

    public E getName() {
        return name;
    }

    public void setName(E name) {
        this.name = name;
    }
    
    public static Boolean getYesNo() {
        return true;
    }
    
    public static Integer getNumber(int j) {
        return 908+i+j;
    }

    public static void main(String[] args) {
        i = i+1;
        Test t = new Test("abc");
        Test t1 = new Test(getYesNo());
        Test t2 = new Test(getNumber(new Integer(3)));
        System.out.println("t="+t.getName());
        System.out.println("t1="+t1.getName());
        System.out.println("t2="+t2.getName());
    }

    public Test(E arg) {
        name = arg;
    }
}