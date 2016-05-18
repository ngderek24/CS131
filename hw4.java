/* Name: Derek Nguyen

   UID: 304275956

   Others With Whom I Discussed Things: Sung Hyun Yoon 904303999

   Other Resources I Consulted:
   
*/

// import lists and other data structures from the Java standard library
import java.util.*;

// PROBLEM 1

// a type for arithmetic expressions
interface Exp {
    double eval(); 	                       // Problem 1a
    List<Instr> compile(); 	               // Problem 1c
}

class Num implements Exp {
    protected double val;

    //constructor
    public Num(double value) {
        this.val = value;
    }

    public boolean equals(Object o) { return (o instanceof Num) && ((Num)o).val == this.val; }

    public String toString() { return "" + val; }

    public double eval() {
        return val;
    }

    public List<Instr> compile() {
        // return a list with one push instr
        List<Instr> l = new LinkedList<Instr>();
        l.add(new Push(val));
        return l;
    }
}

class BinOp implements Exp {
    protected Exp left, right;
    protected Op op;

    //constructor
    public BinOp(Exp left, Op op, Exp right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public boolean equals(Object o) {
    	if(!(o instanceof BinOp))
    		return false;
    	BinOp b = (BinOp) o;
    	return this.left.equals(b.left) && this.op.equals(b.op) &&
		    	this.right.equals(b.right);
    }

    public String toString() {
		return "BinOp(" + left + ", " + op + ", " + right + ")";
    }

    public double eval() {
        double leftResult = left.eval();
        double rightResult = right.eval();
        return op.calculate(leftResult, rightResult);
    }

    public List<Instr> compile() {
        // postorder traversal of exp when viewed as a tree and return as a list
        List<Instr> leftCompiled = left.compile();
        List<Instr> rightCompiled = right.compile();
        List<Instr> binopCompiled = new LinkedList<Instr>(leftCompiled);
        binopCompiled.addAll(rightCompiled);
        binopCompiled.add(new Calculate(op));

        return binopCompiled;
    }
}

// a representation of four arithmetic operators
enum Op {
    PLUS { public double calculate(double a1, double a2) { return a1 + a2; } },
    MINUS { public double calculate(double a1, double a2) { return a1 - a2; } },
    TIMES { public double calculate(double a1, double a2) { return a1 * a2; } },
    DIVIDE { public double calculate(double a1, double a2) { return a1 / a2; } };

    abstract double calculate(double a1, double a2);
}

// a type for arithmetic instructions
interface Instr {
    void eval(Stack<Double> s);
}

class Push implements Instr {
    protected double val;

    public Push(double value) {
        this.val = value;
    }

	public boolean equals(Object o) { return (o instanceof Push) && ((Push)o).val == this.val; }

    public String toString() {
		return "Push " + val;
    }

    public void eval(Stack<Double> s) {
        s.push(Double.valueOf(val));
    }
}

class Calculate implements Instr {
    protected Op op;

    public Calculate(Op op) {
        this.op = op;
    }

    public boolean equals(Object o) { return (o instanceof Calculate) && 
    						  ((Calculate)o).op.equals(this.op); }

    public String toString() {
		return "Calculate " + op;
    }

    public void eval(Stack<Double> s) {
        // evaluate operator on top two values of stack and push the result onto the stack
        Double val1 = s.pop();
        Double val2 = s.pop();
        double result = op.calculate(val2.doubleValue(), val1.doubleValue());
        s.push(Double.valueOf(result));
    }    
}

class Instrs {
    protected List<Instr> instrs;

    public Instrs(List<Instr> instrs) { this.instrs = instrs; }

    public double execute() {
        // traverse the instruction list and evaluate each instruction
        Stack<Double> stack = new Stack<Double>();
        for (Instr i : instrs) {
            i.eval(stack);
        }

        // result is the only value on the stack
        double result = stack.peek().doubleValue();
        return result;
    }  // Problem 1b
}


class CalcTest {
    public static void main(String[] args) {
	    // tests for Problem 1a
		Exp exp = new BinOp(new BinOp(new Num(1.0), Op.PLUS, new Num(2.0)), Op.TIMES, new Num(3.0));
		assert(exp.eval() == 9.0);

		// a test for Problem 1b
		List<Instr> is = new LinkedList<Instr>();
		is.add(new Push(1.0));
		is.add(new Push(2.0));
		is.add(new Calculate(Op.PLUS));
		is.add(new Push(3.0));
		is.add(new Calculate(Op.TIMES));
		Instrs instrs = new Instrs(is);
		assert(instrs.execute() == 9.0);

		// a test for Problem 1c
		assert(exp.compile().equals(is));

        // extra tests
        exp = new BinOp(new Num(2.0), Op.DIVIDE, new BinOp(new BinOp(new Num(3.0), Op.MINUS, new Num(1.0)), Op.TIMES, new Num(1.0)));
        assert(exp.eval() == 1.0);
        List<Instr> il = new LinkedList<Instr>();
        il.add(new Push(2.0));
        il.add(new Push(3.0));
        il.add(new Push(1.0));
        il.add(new Calculate(Op.MINUS));
        il.add(new Push(1.0));
        il.add(new Calculate(Op.TIMES));
        il.add(new Calculate(Op.DIVIDE));
        assert(exp.compile().equals(il));
        Instrs i = new Instrs(il);
        assert(i.execute() == 1.0);

        //
        exp = new BinOp(new BinOp(new Num(3.0), Op.TIMES, new Num(2.0)), Op.MINUS, new BinOp(new Num(4.0), Op.DIVIDE, new Num(1.0)));
        assert(exp.eval() == 2.0);
        List<Instr> a = new LinkedList<Instr>();
        a.add(new Push(3.0));
        a.add(new Push(2.0));
        a.add(new Calculate(Op.TIMES));
        a.add(new Push(4.0));
        a.add(new Push(1.0));
        a.add(new Calculate(Op.DIVIDE));
        a.add(new Calculate(Op.MINUS));
        assert(exp.compile().equals(a));
        Instrs i2 = new Instrs(a);
        assert(i2.execute() == 2.0);

        //
        exp = new BinOp(new BinOp(new Num(6.0), Op.DIVIDE, new Num(4.0)), Op.MINUS, new Num(3.0));
        assert(exp.eval() == -1.5);
        List <Instr> b = new LinkedList<Instr>();
        b.add(new Push(6.0));
        b.add(new Push(4.0));
        b.add(new Calculate(Op.DIVIDE));
        b.add(new Push(3.0));
        b.add(new Calculate(Op.MINUS));
        assert(exp.compile().equals(b));
        Instrs i3 = new Instrs(b);
        assert(i3.execute() == -1.5);

        // Jason's extra tests 
        exp = new BinOp(new BinOp(new Num(3.0), Op.PLUS, new Num(2.0)), Op.TIMES, new BinOp(new Num(1.0), Op.MINUS, new Num(3.0)));
        assert(exp.eval() == -10.0);
        List <Instr> jl2 = new LinkedList<Instr>();
        jl2.add(new Push(3.0));
        jl2.add(new Push(2.0));
        jl2.add(new Calculate(Op.PLUS));
        jl2.add(new Push(1.0));
        jl2.add(new Push(3.0));
        jl2.add(new Calculate(Op.MINUS));
        jl2.add(new Calculate(Op.TIMES));
        assert(exp.compile().equals(jl2));
        Instrs ji2 = new Instrs(jl2);
        assert(ji2.execute() == -10.0);
        // 
        exp = new BinOp(new BinOp(new Num(3.0), Op.TIMES, new Num(0.0)), Op.MINUS, new BinOp(new Num(1.0), Op.MINUS, new Num(3.0)));
        assert(exp.eval() == 2.0);
        List <Instr> jl3 = new LinkedList<Instr>();
        jl3.add(new Push(3.0));
        jl3.add(new Push(0.0));
        jl3.add(new Calculate(Op.TIMES));
        jl3.add(new Push(1.0));
        jl3.add(new Push(3.0));
        jl3.add(new Calculate(Op.MINUS));
        jl3.add(new Calculate(Op.MINUS));
        assert(exp.compile().equals(jl3));
        Instrs ji3 = new Instrs(jl3);
        assert(ji3.execute() == 2.0);
    }
}


// PROBLEM 2

// the type for a set of strings
interface StringSet {
    int size();
    boolean contains(String s);
    void add(String s);

    void print();
}

// an implementation of StringSet using a linked list
class ListStringSet implements StringSet {
    protected SNode head;

    public ListStringSet() {
        this.head = new SEmpty();
    }

    public int size() {
        return head.getSize();
    }

    public boolean contains(String s) {
        return head.contains(s);
    }

    public void add(String s) {
        head = head.add(s);
    }

    public void print() {
        head.print();
    }
}

// a type for the nodes of the linked list
interface SNode {
    int getSize();
    boolean contains(String s);
    SNode add(String s);

    void print();
}

// represents an empty node (which ends a linked list)
class SEmpty implements SNode {
    public int getSize() {
        return 0;
    }

    public boolean contains(String s) {
        return false;
    }

    public SNode add(String s) {
        SNode newNode = new SElement(s, this);
        return newNode;
    }

    public void print() {
        System.out.println("End");
    }
}

// represents a non-empty node
class SElement implements SNode {
    protected String elem;
    protected SNode next;

    public SElement(String val, SNode next) {
        this.elem = val;
        this.next = next;
    }

    public int getSize() {
        return 1 + next.getSize();
    }

    public boolean contains(String s) {
        int compareRes = elem.compareTo(s);
        if (compareRes == 0)            // strings equal
            return true;
        else if (compareRes > 0)        // argument string is lexicographically before node value, so not in set
            return false;
        else                           // argument string is lexicographically after node value, so recurse on next node
            return next.contains(s);
    }

    public SNode add(String s) {
        int compareRes = elem.compareTo(s);
        if (compareRes == 0)        // strings equal
            return this;
        else if (compareRes > 0) {  // new node should be added in this location
            SNode newNode = new SElement(s, this);
            return newNode;
        } else {                    // recurse on next node
            next = next.add(s);
            return this;
        }
    }

    public void print() {
        System.out.println(elem);
        next.print();
    }
}

class StringSetTest {
    public static void main(String[] args) {
        // tests for Problem 2A
        StringSet set = new ListStringSet();
        assert(set.size() == 0);
        set.add("hi");
        assert(set.size() == 1);
        assert(set.contains("bye") == false);
        set.add("hey");
        assert(set.size() == 2);
        assert(set.contains("hey") == true);
        set.add("hi");
        assert(set.size() == 2);
        set.add("sup");
        assert(set.size() == 3);
        assert(set.contains("sup") == true);
        set.add("");
        assert(set.size() == 4);
        set.print();

        // Jason's tests for Sets
        StringSet set1 = new ListStringSet();
        assert(set1.size() == 0);
        set1.add("hello");
        set1.add("bye");
        set1.add("choco");
        set1.add("");
        set1.add("bye");
        assert(set1.size() == 4);
        assert(set1.contains("hello"));
        assert(set1.contains("bye"));
        assert(set1.contains("choco"));
        assert(set1.contains(""));
        assert(!set1.contains("duh"));
        set1.print();
    }
}

interface Set<T> {
    int size();
    boolean contains(T s);
    void add(T s);

    void print();
}

class ListSet<T> implements Set<T> {
    protected Node<T> head;
    protected Comparator<T> comparator;

    public ListSet(Comparator<T> comp) {
        head = new Empty<T>(comp);
        comparator = comp;
    }

    public int size() {
        return head.getSize();
    }

    public boolean contains(T s) {
        return head.contains(s);
    }

    public void add(T s) {
        head = head.add(s);
    }

    public void print() {
        head.print();
    }
}

interface Node<T> {
    int getSize();
    boolean contains(T s);
    Node<T> add(T s);

    void print();
}

class Empty<T> implements Node<T> {
    protected Comparator<T> comparator;

    public Empty(Comparator<T> comp) {
        comparator = comp;
    }

    public int getSize() {
        return 0;
    }

    public boolean contains(T s) {
        return false;
    }

    public Node<T> add(T s) {
        Node<T> newNode = new Element<T>(s, this, comparator);
        return newNode;
    }

    public void print() {
        System.out.println("End");
    }
}

class Element<T> implements Node<T> {
    protected T elem;
    protected Node<T> next;
    protected Comparator<T> comparator;

    public Element(T val, Node<T> next, Comparator<T> comp) {
        this.elem = val;
        this.next = next;
        comparator = comp;
    }

    public int getSize() {
        return 1 + next.getSize();
    }

    public boolean contains(T s) {
        int compareRes = comparator.compare(elem, s);
        if (compareRes == 0)        // equal objects
            return true;
        else if (compareRes > 0)    // argument object is "less than" current elem, so not in set
            return false;
        else                        // recurse on next node
            return next.contains(s);
    }

    public Node<T> add(T s) {
        int compareRes = comparator.compare(elem, s);
        if (compareRes == 0)        // equal objects
            return this;
        else if (compareRes > 0) {  // new node should be added in this location
            Node<T> newNode = new Element<T>(s, this, comparator);
            return newNode;
        } else {                    // recurse on next node
            next = next.add(s);
            return this;
        }
    }

    public void print() {
        System.out.println(elem);
        next.print();
    }
}

class SetTest {
    public static void main(String[] args) {
        // tests for Problem 2B
        // ascending integers
        Set<Integer> set = new ListSet<Integer>((i1, i2) -> i1.compareTo(i2));
        set.add(6);
        assert(set.size() == 1);
        assert(set.contains(4) == false);
        
        set.add(3);
        assert(set.size() == 2);
        assert(set.contains(3) == true);
        
        set.add(6);
        assert(set.size() == 2);

        set.add(8);
        assert(set.size() == 3);
        assert(set.contains(8) == true);

        set.print();

        // reverse alphabetical strings
        Set<String> set2 = new ListSet<String>((s1, s2) -> s2.compareTo(s1));
        set2.add("hi");
        assert(set2.size() == 1);
        assert(set2.contains("bye") == false);
        
        set2.add("hey");
        assert(set2.size() == 2);
        assert(set2.contains("hey") == true);

        set2.add("hi");
        assert(set2.size() == 2);

        set2.add("sup");
        assert(set2.size() == 3);
        assert(set2.contains("sup") == true);

        set2.print();
        
        // decreasing string length
        Set<String> set3 = new ListSet<String>(new Comparator<String>() {
                                                public int compare(String s1, String s2) {
                                                    return s2.length() - s1.length();
                                                }
        } );
        set3.add("a");
        set3.add("ab");
        assert(set3.size() == 2);
        set3.add("a");
        set3.add("abc");
        assert(set3.contains("abc") == true);
        set3.add("bc");
        set3.print();

        // Jason's
        Set<String> set4 = new ListSet<String>((String s1, String s2) -> s1.compareTo(s2));
        assert(set4.size() == 0);
        set4.add("hello");
        set4.add("bye");
        set4.add("choco");
        set4.add("");
        set4.add("bye");
        assert(set4.size() == 4);
        assert(set4.contains("hello"));
        assert(set4.contains("bye"));
        assert(set4.contains("choco"));
        assert(set4.contains(""));
        assert(!set4.contains("duh"));
        set4.print();

        Set<Integer> set5 = new ListSet<Integer>((Integer i1, Integer i2) -> i1 - i2);
        assert(set5.size() == 0);
        set5.add(3);
        set5.add(5);
        set5.add(-1);
        set5.add(0);
        set5.add(6);
        set5.add(5);
        set5.add(6);
        set5.add(-1);
        assert(set5.size() == 5);
        assert(set5.contains(3));
        assert(set5.contains(5));
        assert(set5.contains(0));
        assert(set5.contains(-1));
        assert(set5.contains(6));
        assert(!set5.contains(7)); 
        set5.print();
    }
}