package tests;

import java.util.ArrayList;

import tools.Dprint;

public class ThreadIsAlive extends Thread{
	public static volatile int nb =0;
	public static boolean _stop=false;
	
	public int cpt=0;
	public int id =0;
	
	public ThreadIsAlive(){
		super();
		id = nb;
		nb++;
	}
	
	public void run(){
		while (cpt<1000000){
			cpt++;
		}
		Dprint.println("thread"+id+"has finished");
	}
	public static void main(String[] args) {
		int taille = 200000;
		ArrayList<ThreadIsAlive> a = new ArrayList<ThreadIsAlive>(taille);
		Dprint.println("deb");
		for(int i=0;i<taille;i++){
			ThreadIsAlive t = new ThreadIsAlive();
			a.add(t);
			t.start();
			//System.out.println("nv Thread");
		}
		Dprint.println("milieu");
		for(ThreadIsAlive t:a){
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Dprint.println("fin");
		
		
		
		

//		Thread t1 = new Thread() {
//			public void run() {
//				System.out.println();
//				for (int i = 0; i < 100; i++)
//					System.out.print(i + " ");
//			}
//		};
//
//		class MyThread extends Thread {
//			Thread _t;
//
//			public MyThread(Thread t) {
//				_t = t;
//			}
//
//			public void run() {
//				for (int i = 0; i < 4; i++) {
//					if (_t.isAlive()) {
//						System.out.println("isAlive");
//						i--;
//					} else {
//						System.out.println("restart");
//						_t.run();
//					}
//				}
//			}
//
//		}
//		;
//
//		MyThread t2 = new MyThread(t1);
//		t1.start();
//		t2.start();
	}

}
