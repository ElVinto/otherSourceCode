package tools;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
/**
 * Title:        Small World Graph Generation Program
 * Description:  D. J. Watts and S. H. Strogatz model
 * Copyright:    Copyright (c) 2001
 * Company:      Columbia University
 * @author Angelos Stavrou
 * @version 1.0
	Last change:  AS   20 Dec 101    9:45 am
 */

 class GraphDraw extends JPanel {
  public int numberofpoints;
  public double conprob;
  public int neighbors;
  public void StartG(int nn, double cp, int neighb) {
    numberofpoints=nn;
    conprob = cp;
    neighbors=neighb;
//    System.out.println("Number of nodes --->"+nn);
//    System.out.println("Rewiring Probability --->"+cp);
    repaint(0);
  }
  public void paintComponent(Graphics g){
    super.paintComponent(g);
    int maxWidth = getWidth()-6;
    int maxHeight = getHeight()-6;
    Random randomv = new Random();
    int posx[] = new int [numberofpoints+1];
    int posy[] = new int [numberofpoints+1];
    int degree[] = new int [numberofpoints];
    int adjacencyM[][] = new int [numberofpoints+1][numberofpoints+1];
    g.setColor(Color.red);
    for (int i = 0; i < numberofpoints; i++){
        double p1,p2;
        p1=maxWidth/2.+(maxHeight/2)*Math.cos(2.*(Math.PI/(new Integer(numberofpoints).doubleValue()))*i);
        p2=maxWidth/2.+(maxWidth/2)*Math.sin(2.*(Math.PI/(new Integer(numberofpoints).doubleValue()))*i);
        posx[i]=new Double(p1).intValue();
        posy[i]=new Double(p2).intValue();
        g.fillOval(posx[i],posy[i],5,5);

 //g.drawArc(35, 50, 125, 180, 1, 360);
    }
     g.setColor(Color.blue);
    int numofedgesc = 0;
    for (int i1 = 0; i1 < numberofpoints ;i1++){
      for  (int i2 = i1+1 ; i2 <= i1+neighbors/2 ;i2++){
          adjacencyM[i1][i2%numberofpoints]=1;
          adjacencyM[i2%numberofpoints][i1]=1;
          numofedgesc++;
          degree[i1]++;
          degree[i2%numberofpoints]++;
          g.drawLine(posx[i1]+2,posy[i1]+2,posx[i2%numberofpoints]+2,posy[i2%numberofpoints]+2);

      }
    }
    int candidate;
    for (int i1 = 0; i1 < numberofpoints ;i1++){
      for  (int i2 = 1 ; i2 < neighbors/2+1 ;i2++){
        if (conprob >= randomv.nextDouble()){
           candidate=randomv.nextInt(numberofpoints);
           System.out.println("Node"+i1+"Candidate="+candidate+":"+adjacencyM[i1][candidate]+":"+adjacencyM[candidate][i1]+":");
            if  ((candidate != i1) && (adjacencyM[i1][candidate]!=1)|| (adjacencyM[candidate][i1]!=1)){
              System.out.println("Success :"+"Node"+i1+"Candidate="+candidate+":"+adjacencyM[i1][candidate]+":"+adjacencyM[candidate][i1]+":");
              adjacencyM[i1][candidate]=1;
              adjacencyM[candidate][i1]=1;
              adjacencyM[i1][(i1+i2)%numberofpoints]=0;
              adjacencyM[(i1+i2)%numberofpoints][i1]=0;
              degree[candidate]++;
              degree[(i1+i2)%numberofpoints]--;
              g.setColor(Color.red);
              g.drawLine(posx[i1]+2,posy[i1]+2,posx[(i1+i2)%numberofpoints]+2,posy[(i1+i2)%numberofpoints]+2);
              g.setColor(Color.blue);
              g.drawLine(posx[i1]+2,posy[i1]+2,posx[candidate]+2,posy[candidate]+2);
            }
        }

      }
    }
//    for (int i1 = 0; i1 < numberofpoints ;i1++){
//     System.out.println("Degree:"+i1+":"+degree[i1]);
//    }
    GraphS.numofedges.setText(Integer.toString(numofedgesc));
//    System.out.println("--->"+maxWidth);
  }

}
public class GraphS extends JApplet implements ActionListener {
//  public JTextField conprob,numofnodes;
  int i;
  public JButton jButton1 = new JButton();
  public JButton jButton2 = new JButton();
  public JLabel jLabel1 = new JLabel();
  public JLabel jLabel2 = new JLabel();
  public JLabel jLabel3 = new JLabel();
  public JLabel jLabel4 = new JLabel();
  public JLabel jLabel5 = new JLabel();
  public JLabel jLabel6 = new JLabel();
  public JTextField conprob = new JTextField(3);
  public JTextField numofnodes = new JTextField(4);
  public static JTextField numofedges = new JTextField(6);
  public static JTextField neighbors = new JTextField(2);
  public JTextField dummy = new JTextField(10);
  public GraphDraw graphd = new GraphDraw();
  Dimension d = new Dimension (350,350);
  public JSlider zoom= new JSlider (0,100,0);
  public void init() {
  Container cp = getContentPane();
  cp.setLayout(new FlowLayout(0,20,20));
  cp.setSize(600,500);
  jButton1.setText("Start");
  jButton2.setText("Stop");
  jLabel1.setText("Rewiring Probability:");
  jLabel2.setText("Number of Nodes:   ");
  jLabel3.setText("Number of Edges:   ");
  jLabel4.setText("Number of Neighbors");
  jLabel5.setText("Blue for Active Edges");
  jLabel6.setText("Red for Deleted Edges");
  //jLabel6.setBackground(Color.red);
  numofedges.setEditable(false);
  cp.add(jLabel2);
  cp.add(numofnodes);
  cp.add(jLabel1);
  cp.add(conprob);
  cp.add(jButton1);
  cp.add(jButton2);
  graphd.setPreferredSize(d);
  cp.add(BorderLayout.CENTER,graphd);
  cp.add(BorderLayout.SOUTH,jLabel3);
  cp.add(BorderLayout.SOUTH,numofedges);
  cp.add(BorderLayout.EAST,jLabel5);
  cp.add(BorderLayout.EAST,jLabel6);
  cp.add(BorderLayout.SOUTH,jLabel4);
  cp.add(BorderLayout.SOUTH,neighbors);
  cp.add(BorderLayout.SOUTH,zoom);

  jButton1.addActionListener(this);
  numofnodes.addActionListener(this);
  conprob.addActionListener(this);
  zoom.setBorder(new TitledBorder("Rewiring Probability:"));
  zoom.addChangeListener(new ChangeListener(){
    public void stateChanged(ChangeEvent e){
 //     System.out.println(((JSlider)e.getSource()).getValue());
      conprob.setText(Double.toString(((JSlider)e.getSource()).getValue()/100.));
      graphd.StartG(Integer.parseInt(numofnodes.getText()),Double.parseDouble(conprob.getText()),Integer.parseInt(neighbors.getText()));
    }
  });

  numofnodes.setText("25");
  conprob.setText("0");
  neighbors.setText("4");

}



 public final void update(Graphics g) {
    paint(g);
    //   System.out.println(""+graph);
  }
// A main() for the application :
  public static void  main (String[] args) {
    JApplet applet = new GraphS();
    JFrame frame = new JFrame("The D. J. Watts and S. H. Strogatz model for Small World Graphs");
    frame.getContentPane().add(applet);
    frame.setSize(1000,1000);
    frame.pack();
	  applet.init();
	    applet.start();
	    frame.setVisible(true);
  }
    public void actionPerformed(ActionEvent evt) {
    //  System.out.println(i);
    //  i++;
      graphd.StartG(Integer.parseInt(numofnodes.getText()),Double.parseDouble(conprob.getText()),Integer.parseInt(neighbors.getText()));
    }

}


