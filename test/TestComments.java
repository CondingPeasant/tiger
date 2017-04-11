// Comment at first line
class Factorial { 
    /* test delimiter */
	public static void main(String[] a) {
        System.out.println(new Fac().ComputeFac(10));
    }
    /* // in the delimiters */
    // /* after //
}
class Fac { // // after statement
    public int ComputeFac(int num) {
        /*
         * delimiters cover several lines.
         */
        int num_aux;
        /* nested /* delimiters */ test */
        if (num < 1)
            num_aux = 1;
        else
            num_aux = num * (this.ComputeFac(num-1));
        return num_aux;
    }
}
// Comment at last line;
