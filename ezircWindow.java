

import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class ezircWindow extends Frame  implements Runnable, ActionListener
  {
  String serverName;
  int serverPort;
  String nickName;
  String channelName;
  TextArea Output;
  TextField Input;
  private static String localHostName;
  private static BufferedReader socketIn;
  private static DataOutputStream socketOut;
  boolean terminate=false;
  static long ServerPingInterval=30000;
  long currentTime,startTime,lastMessageFromServerTime,lastPingToServerTime;
    
  public ezircWindow(String server,int port,String nick,String channel)
    {
    serverName = server;
    serverPort = port;
    nickName = nick;
    channelName = channel;

    setSize(550,450);  // Should be sufficient, and fits to 640x480.
    setLayout(new BorderLayout());
    setTitle("EZIRC Java (01/22/2001 a) -- "+serverName+"  "+nickName+"  "+channelName);
    add("Center",Output=new TextArea());
    Output.setEditable(false);  // Of course, output cannot be edited.
    add("South",Input=new TextField());
    Input.addActionListener(this);
    setVisible(true);

    addWindowListener(new WindowAdapter ()  // Add listener for window closing.
      {
      public void windowClosing (WindowEvent e)
        {
        setVisible(false);
        dispose();
        end();
        }
      } );
    addWindowListener(new WindowAdapter ()  // Add listener for window activation.
      {
      public void windowActivated (WindowEvent e)
        {
        Input.requestFocus();
        }
      } );

    try
      {
      Output.append("Opening connection to server.\r\n");
      Socket socket=new Socket(serverName,serverPort);
      Output.append("Creating output socket.\r\n");
      socketOut=new DataOutputStream(socket.getOutputStream());
      Output.append("Creating input socket.\r\n");
      socketIn=new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }
    catch (Exception e)
      {
      Output.append("Error creating output and input sockets.\r\n");
      System.err.println(e);
      System.err.println("Error creating output and input sockets.");
      }

    try
      {
      Output.append("Getting local host name.\r\n");
//      localHostName = socket.getLocalAddress().getHostName();
       localHostName = "localhost";
      Output.append("Local host name is "+localHostName+"\r\n");
      }
    catch (Exception e)
      {
      Output.append("Error getting local host name.\r\n");
      System.err.println(e);
      System.err.println("Error getting local host name.");
      }

    try
      {
      Output.append("Starting input thread now.\r\n");
      startTime = System.currentTimeMillis();
      lastMessageFromServerTime = System.currentTimeMillis();
      Thread t=new Thread(this);  // Start the input thread.
      t.start();
      Thread.sleep(3000);
      }
    catch (Exception e)
      {
      Output.append("Error starting input thread.\r\n");
      System.err.println(e);
      System.err.println("Error starting input thread.");
      }

    try
      {
      Output.append("Sending nick command to server.\r\n");
      socketOut.writeBytes("nick "+nickName+"\r\n");
      socketOut.flush();
      Thread.sleep(1500);

      Output.append("Sending user command to server.\r\n");
      socketOut.writeBytes("user "+nickName+" "+localHostName+" "+serverName+" :Ezirc User\r\n");
      socketOut.flush();
//      Thread.sleep(1000);
      }
    catch (Exception e)
      {
      Output.append("Error sending nick or user command to server.\r\n");
      System.err.println(e);
      System.err.println("Error sending nick or user command to server.");
      }

    Input.requestFocus();
    }


  public void run ()
  {
  try
    {
    String message;
    int spacePosition1;
    int spacePosition2;
    int spacePosition3;
    String messageField1;
    String messageField2;
    Output.append("Input thread running.\r\n");
    while (!terminate)
      {
      if (socketIn.ready())  // Is anything available from the server?
        {
        message = socketIn.readLine();
        lastMessageFromServerTime = System.currentTimeMillis();

        spacePosition1 = message.indexOf(' ');
        spacePosition2 = message.indexOf(' ',spacePosition1+1);
        if (spacePosition2 == -1)
          spacePosition3 = -1;
        else
          spacePosition3 = message.indexOf(' ',spacePosition2+1);
        messageField1 = message.substring(0,spacePosition1);
        if (spacePosition2 == -1)
          messageField2 = message.substring(spacePosition1+1);
        else
          messageField2 = message.substring(spacePosition1+1,spacePosition2);

        if (messageField2.equals("PONG"))
          continue;
        if (messageField1.equals("PING"))
          {
          Output.append("Replying to PING now.\r\n");
          socketOut.writeBytes("PONG "+message.substring(spacePosition1+1)+"\r\n");
          socketOut.flush();
          continue;
          }
        if (messageField2.equals("001"))
          {
          Output.append("Joining channel now.\r\n");
          socketOut.writeBytes("JOIN "+channelName+"\r\n");
          socketOut.flush();
          continue;
          }
        if (messageField2.equals("PRIVMSG"))
          {
          Output.append("<"+message.substring(1,message.indexOf('!'))+"> "+message.substring(spacePosition3+2)+"\r\n");
          continue;
          }
        Output.append(message+"\r\n");
        continue;
        }
      if ((System.currentTimeMillis() - lastMessageFromServerTime) > ((ServerPingInterval * 2) + 10000))
        {
        Output.append("Failure to communicate with server for a while, exiting in 15 seconds!\r\n");
        Thread.sleep(15000);
        break;
        }
      if (((System.currentTimeMillis() - lastMessageFromServerTime) > ServerPingInterval)
       && ((System.currentTimeMillis() - lastPingToServerTime) > ServerPingInterval))
        {
//        Output.append("Pinging IRC server now....\r\n");
        socketOut.writeBytes("PING "+localHostName+"\r\n");
        socketOut.flush();
        lastPingToServerTime = System.currentTimeMillis();
        }
      Thread.sleep(10);
      }
    socketOut.writeBytes("QUIT\r\n");
    socketOut.flush();
    Thread.sleep(1000);
    }
  catch (Exception e)
    {
    System.out.println(e);
    }
  setVisible(false);
  dispose();
  end();
  }


  // The only action is RETURN in the input line.
  public void actionPerformed (ActionEvent e)
    {
    if (e.getSource()==Input)
      {
      try
        {
        String inputText = Input.getText();
        if (inputText.equals("/quit"))
          {
          terminate = true;
          }
        else
        if (inputText.substring(0,1).equals("/"))
          {
          System.out.println("Sending your command.");
          socketOut.writeBytes(inputText.substring(1)+"\r\n");
          socketOut.flush();
          Input.setText("");
          }
        else
          {
          socketOut.writeBytes("PRIVMSG "+channelName+" :"+inputText+"\r\n");
          socketOut.flush();
          Output.append(inputText+"\r\n");
          Input.setText("");
          }
        }
      catch (Exception e2)
        {
        System.err.println(e2);
        System.err.println("Error writing to socket.");
        }
      }
    }

  // This is called when the window closes. Override!
  public void end ()
	{
	}
	
  }

