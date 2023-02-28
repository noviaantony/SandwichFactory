import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import java.util.*;

// javac SandwichManager 10 4 4 3 3 3 3 5 4
// java SandwichManager 10 4 4 3 3 3 3 5 4

public class SandwichManager {



  static void gowork(int n){ 
    for (int i=0; i<n; i++){
        long m = 300000000;
        while (m>0){
            m--;
        }
    }
   }

  public static void main(String[] args) {


    /******************************************************************************
     * ARGUMENTS
     ******************************************************************************/
    int n_sandwiches = Integer.parseInt(args[0]); // total sandwiches to make
    System.out.println("sandwiches: " + n_sandwiches);
    int bread_capacity = Integer.parseInt(args[1]); // number of slots in bread pool
    System.out.println("bread capacity: " + bread_capacity);
    int egg_capacity = Integer.parseInt(args[2]); // number of slots in egg pool
    System.out.println("egg capacity: " + egg_capacity);
    int n_bread_makers = Integer.parseInt(args[3]); // number of bread maker - B0,B1,B2
    System.out.println("bread makers: " + n_bread_makers);
    int n_egg_makers = Integer.parseInt(args[4]); //  number of egg makers - E0,E1,E2
    System.out.println("egg makers: " + n_egg_makers);
    int n_sandwich_packers = Integer.parseInt(args[5]); // number of sandwich packer
    System.out.println("sandwich packers: " + n_sandwich_packers);
    int bread_rate = Integer.parseInt(args[6]); //  minutes to toast a slice of bread
    System.out.println("bread rate: " + bread_rate);
    int egg_rate = Integer.parseInt(args[7]); //  minutes to make 1 scrambled egg
    System.out.println("egg rate: " + egg_rate);
    int packing_rate = Integer.parseInt(args[8]); // number of minutes to pack a sandwich
    System.out.println("packing rate: " + packing_rate);
    System.out.println();

  

    /******************************************************************************
     * BREAD MACHINE
     ******************************************************************************/
    BreadPool breadPool = new BreadPool(bread_capacity); // buffer
    Runnable breadMaker = new Runnable() {
      @Override
      public void run(){
        for (int i=0; i < bread_capacity ; i++){
          gowork(bread_rate);
          Bread bread= new Bread(i); 
          bread.setThreadName(Thread.currentThread().getName());
          breadPool.put(bread);
        }
      }
    };

    /******************************************************************************
     * EGG MACHINE
     ******************************************************************************/
    EggPool eggPool = new EggPool(egg_capacity); // buffer
    Runnable eggMaker = new Runnable() {
      @Override
      public void run(){
        for (int i=0; i< egg_capacity; i++){
          gowork(egg_rate);
          Egg egg= new Egg(i);
          egg.setThreadName(Thread.currentThread().getName());
          eggPool.put(egg);
        }
      }
    }; 
    /******************************************************************************
     * SANDWICH MACHINES
     ******************************************************************************/
    Runnable sandwichPacker = new Runnable() {
      @Override
      public void run(){
        // System.out.println("ENTERED");
        gowork(packing_rate);

        Bread bread1 = breadPool.get();
        Bread bread2 = breadPool.get();
        Egg egg= eggPool.get();
        
        Sandwich sandwich = new Sandwich(0,egg, bread1, bread2);

        System.out.println( Thread.currentThread().getName() + " packs " + sandwich + " with " + bread1 + " from " +  bread1.getThreadName() + " and " + egg + " from " + egg.getThreadName() + " and " + bread2 + " from " + bread2.getThreadName() );


        
      }
    };

    /******************************************************************************
     * MANAGER
     ******************************************************************************/
    Thread[] threads = new Thread[100];

    for (int i = 0; i < n_bread_makers; i++) {
      threads[i] = new Thread(breadMaker);
      String name = "B" + i;
      threads[i].setName(name);
    }
    for (int i = n_bread_makers; i < (n_bread_makers+n_egg_makers) ; i++) {
      threads[i] = new Thread(eggMaker);
      int num = i - n_bread_makers;
      String name = "E" + num;
      threads[i].setName(name);
    }
    for (int i = 0; i <  (n_bread_makers+n_egg_makers); i++) {
      threads[i].start();
      // System.out.println("thread " + i);
    }

    for (int i = (n_bread_makers+n_egg_makers); i < (n_bread_makers+n_egg_makers)+ n_sandwich_packers; i++){
      threads[i] = new Thread(sandwichPacker);
      int num = i - (n_bread_makers+n_egg_makers);
      String name = "S" + num;
      threads[i].setName(name);
    }

    for (int i = (n_bread_makers+n_egg_makers); i < (n_bread_makers+n_egg_makers)+ n_sandwich_packers; i++){
      threads[i].start();
      // System.out.println("sandwich packer " + i);
    }


  }


}


class Bread {

  int id;
  String threadName;

  public Bread(int id){
    this.id = id;
  }

  @Override
  public String toString(){
    return "bread " + id; 
  }

  public void setThreadName(String name) {
    this.threadName = name;
  }

  public String getThreadName() {
    return this.threadName;
  }

}

class Egg {

  int id;
  String threadName;

  public Egg(int id){
    this.id = id;
  }

  @Override
  public String toString(){
    return "egg " + id; 
  }

  public void setThreadName(String name) {
    this.threadName = name;
  }

  public String getThreadName() {
    return this.threadName;
  }

}


class Sandwich {

  int id;
  Egg egg;
  Bread bread1;
  Bread bread2;
  String threadName;

  public Sandwich(int id, Egg egg, Bread bread1, Bread bread2){
    this.id = id;
    this.egg = egg;
    this.bread1 = bread1;
    this.bread2 = bread2;
  }

  @Override
  public String toString(){
    return "sandwich " + id;
  }

}


class BreadPool {

  static volatile Bread[] buffer;
  static volatile int front = 0, back = 0, item_count = 0;


  BreadPool(int size){ 
    buffer = new Bread[size];
  }

  public synchronized void put(Bread bread){
    while (item_count == buffer.length){
       try { this.wait(); } catch (InterruptedException e) {}
    }
    buffer[back] = bread;
    back = (back + 1) % buffer.length;

    System.out.println(Thread.currentThread().getName() + " puts " + bread);
    item_count++;
    
    this.notifyAll();
  
  }

  public synchronized Bread get(){
    while (item_count == 0){
      try { this.wait(); } catch (InterruptedException e) {}
    }

    Bread bread = buffer[front];
    front = (front + 1) % buffer.length;
    // System.out.println("B" + item_count + " eats " + bread);
    item_count--;
    this.notifyAll();

    return bread;
  }

}


class EggPool {

    static volatile Egg[] buffer;
    static volatile int front = 0, back = 0, item_count = 0;

    EggPool(int size){ 
      buffer = new Egg[size];
    }

    public synchronized void put(Egg egg){
      while (item_count == buffer.length){
          try { this.wait(); } catch (InterruptedException e) {}
      }
      buffer[back] = egg;
      back = (back + 1) % buffer.length;
      System.out.println(Thread.currentThread().getName() + " puts " + egg);
      item_count++;
      this.notifyAll();
    }

    public synchronized Egg get(){
      while (item_count == 0){
        try { this.wait(); } catch (InterruptedException e) {}
      }

      Egg egg = buffer[front];
      front = (front + 1) % buffer.length;
      // System.out.println("E" + item_count + " eats " + egg);
      item_count--;
      this.notifyAll();

      return egg;
    }


}
