package com.gxk.jvm;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {

    if (args.length == 0) {
      System.out.println("usage: java [options] class [args]\n");
      return;
    }

    Args cmd = Args.parseArgs(args);

    if (cmd.version) {
      System.out.println("java version \"1.8.0\"");
      return;
    }

    VirtualMachine vm = new VirtualMachine();
    vm.run(cmd);
  }
}
