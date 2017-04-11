package slp;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import control.Control;
import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.Op;
import slp.Slp.ExpList;
import slp.Slp.Stm;
import util.Bug;

public class Main
{
  // ///////////////////////////////////////////
  // maximum number of args

  private int maxArgsExp(Exp.T exp)
  {
    if (exp instanceof Exp.Id
        || exp instanceof Exp.Num
        || exp instanceof Exp.Op) {
      return 1;
    } else if (exp instanceof Exp.Eseq) {
      Exp.Eseq e = (Exp.Eseq) exp;
      int n1 = maxArgsStm(e.stm);
      int n2 = maxArgsExp(e.exp);
      return n1 >= n2 ? n1 : n2;
    } else  {
      new Bug();
    }
    return 0;
  }
  
  private int maxArgsExpList(ExpList.T expList)
  {
    if (expList instanceof ExpList.Pair) {
      ExpList.Pair el = (ExpList.Pair) expList;
      return maxArgsExpList(el.list) + 1;
    } else if (expList instanceof ExpList.Last) {
      ExpList.Last el = (ExpList.Last) expList;
      return maxArgsExp(el.exp);
    } else  {
      new Bug();
    }
    return 0;
  } 

  private int maxArgsStm(Stm.T stm)
  {
    if (stm instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) stm;
      int n1 = maxArgsStm(s.s1);
      int n2 = maxArgsStm(s.s2);
      return n1 >= n2 ? n1 : n2;
    } else if (stm instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) stm; 
      return maxArgsExp(s.exp);
    } else if (stm instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) stm; 
      return maxArgsExpList(s.explist);
    } else {
      new Bug();
    }
    return 0;
  }

  // ////////////////////////////////////////
  // interpreter
  private Map<String,Integer> mIdValue;

  private int interpExp(Exp.T exp)
  {
    if (exp instanceof Exp.Id) {
        Exp.Id e = (Exp.Id) exp;
        return mIdValue.get(e.id);
    } else if (exp instanceof Exp.Num) {
        Exp.Num e = (Exp.Num) exp;
        return e.num;
    } else if (exp instanceof Exp.Op) {
        Exp.Op e = (Exp.Op) exp;
        int ret = 0;
        int leftResult = interpExp(e.left);
        int rightResult = interpExp(e.right);
        switch(e.op) {
          case ADD:
              ret = leftResult + rightResult;
            break;
          case SUB:
              ret = leftResult - rightResult;
            break;
          case TIMES:
              ret = leftResult * rightResult;
            break;
          case DIVIDE:
            if (0 == rightResult)
              throw new ArithmeticException();
            ret = leftResult / rightResult;
            break;
          default:
            new Bug();
            break;
        }
        return ret;
    } else if (exp instanceof Exp.Eseq) {
        Exp.Eseq e = (Exp.Eseq) exp;
        interpStm(e.stm);
        return interpExp(e.exp);
    } else  {
      new Bug();
    }
    return 0;
  }

  private void printExpList(ExpList.T expList)
  {
    if (expList instanceof ExpList.Pair) {
      ExpList.Pair el = (ExpList.Pair) expList;
      System.out.print(interpExp(el.exp) + " ");
      printExpList(el.list);
    } else if (expList instanceof ExpList.Last) {
      ExpList.Last el = (ExpList.Last) expList;
      System.out.println(interpExp(el.exp));
    } else  {
      new Bug();
    }
  }

  private void interpStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) prog;
      interpStm(s.s1);
      interpStm(s.s2);
    } else if (prog instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) prog; 
      mIdValue.put(s.id, interpExp(s.exp));
    } else if (prog instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) prog; 
      printExpList(s.explist);
    } else {
      new Bug();
    }
  }

  // ////////////////////////////////////////
  // compile
  HashSet<String> ids;
  StringBuffer buf;

  private void emit(String s)
  {
    buf.append(s);
  }

  private void compileExp(Exp.T exp)
  {
    if (exp instanceof Id) {
      Exp.Id e = (Exp.Id) exp;
      String id = e.id;

      emit("\tmovl\t" + id + ", %eax\n");
    } else if (exp instanceof Num) {
      Exp.Num e = (Exp.Num) exp;
      int num = e.num;

      emit("\tmovl\t$" + num + ", %eax\n");
    } else if (exp instanceof Op) {
      Exp.Op e = (Exp.Op) exp;
      Exp.T left = e.left;
      Exp.T right = e.right;
      Exp.OP_T op = e.op;

      switch (op) {
      case ADD:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\taddl\t%edx, %eax\n");
        break;
      case SUB:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\tsubl\t%eax, %edx\n");
        emit("\tmovl\t%edx, %eax\n");
        break;
      case TIMES:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\timul\t%edx\n");
        break;
      case DIVIDE:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tcmpl\t$0, %eax\n");
        emit("\tje\t\texception\n");
        emit("\tpopl\t%edx\n");
        emit("\tmovl\t%eax, %ecx\n");
        emit("\tmovl\t%edx, %eax\n");
        emit("\tcltd\n");
        emit("\tdiv\t%ecx\n");
        break;
      default:
        new Bug();
      }
    } else if (exp instanceof Eseq) {
      Eseq e = (Eseq) exp;
      Stm.T stm = e.stm;
      Exp.T ee = e.exp;

      compileStm(stm);
      compileExp(ee);
    } else {
      new Bug();
    }
  }

  private void compileExpList(ExpList.T explist)
  {
    if (explist instanceof ExpList.Pair) {
      ExpList.Pair pair = (ExpList.Pair) explist;
      Exp.T exp = pair.exp;
      ExpList.T list = pair.list;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
      compileExpList(list);
    } else if (explist instanceof ExpList.Last) {
      ExpList.Last last = (ExpList.Last) explist;
      Exp.T exp = last.exp;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else {
      new Bug();
    }
  }

  private void compileStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) prog;
      Stm.T s1 = s.s1;
      Stm.T s2 = s.s2;

      compileStm(s1);
      compileStm(s2);
    } else if (prog instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) prog;
      String id = s.id;
      Exp.T exp = s.exp;

      ids.add(id);
      compileExp(exp);
      emit("\tmovl\t%eax, " + id + "\n");
    } else if (prog instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) prog;
      ExpList.T explist = s.explist;

      compileExpList(explist);
      emit("\tpushl\t$newline\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else {
      new Bug();
    }
  }

  // ////////////////////////////////////////
  public void doit(Stm.T prog)
  {
    // return the maximum number of arguments
    if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
      int numArgs = maxArgsStm(prog);
      System.out.println(numArgs);
    }

    // interpret a given program
    if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
      mIdValue = new HashMap<String, Integer>();
      interpStm(prog);
    }

    // compile a given SLP program to x86
    if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
      ids = new HashSet<String>();
      buf = new StringBuffer();

      compileStm(prog);
      try {
        // FileOutputStream out = new FileOutputStream();
        FileWriter writer = new FileWriter("slp_gen.s");
        writer
            .write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
        writer.write("\t.data\n");
        writer.write("slp_format:\n");
        writer.write("\t.string \"%d \"\n");
        writer.write("newline:\n");
        writer.write("\t.string \"\\n\"\n");
        writer.write("exception_msg:\n");
        writer.write("\t.string \"Exception occurs!\\n\"\n");
        for (String s : this.ids) {
          writer.write(s + ":\n");
          writer.write("\t.int 0\n");
        }
        writer.write("\n\n\t.text\n");
        writer.write("\t.globl main\n");
        writer.write("main:\n");
        writer.write("\tpushl\t%ebp\n");
        writer.write("\tmovl\t%esp, %ebp\n");
        writer.write(buf.toString());
        writer.write("\tjp\texit\n");
        writer.write("exception:\n");
        writer.write("\tpushl\t$exception_msg\n");
        writer.write("\tcall\tprintf\n");
        writer.write("\taddl\t$4, %esp\n");
        writer.write("exit:\n");
        writer.write("\tleave\n\tret\n\n");
        writer.close();
        Process child = Runtime.getRuntime().exec("gcc -m32 slp_gen.s");
        child.waitFor();
        if (!Control.ConSlp.keepasm)
          Runtime.getRuntime().exec("rm -rf slp_gen.s");
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
      // System.out.println(buf.toString());
    }
  }
}
