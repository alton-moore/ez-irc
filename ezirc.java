

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


/*
<CENTER><P>                    This is the HTML code that invokes this applet.

<APPLET 
NAME="IRC Applet"
CODE="ezirc.class" WIDTH="190" HEIGHT="81" ALIGN="BOTTOM">
<PARAM NAME="server"  VALUE="www.alton-moore.net">
<PARAM NAME="port"    VALUE="6667">
<PARAM NAME="nick"    VALUE="">
<PARAM NAME="channel" VALUE="#al&company">
</APPLET>

</P>
</CENTER>
*/


public class ezirc  extends Applet  implements ActionListener
{
TextField serverName;
TextField serverPort;
TextField nickName;
TextField channelName;
Button Connect;  // For starting the connection.

  public void init ()
  {
  setLayout(new GridLayout(5,2));

  add(new Label("serverName"));
  add(serverName=new TextField(getParameter("server"),50));
  add(new Label("serverPort"));
  add(serverPort=new TextField(getParameter("port"),5));
  add(new Label("nickName"));
  add(nickName=new TextField(getParameter("nick"),9));
  add(new Label("channelName"));
  add(channelName=new TextField(getParameter("channel"),30));

  add(Connect=new Button("Connect"));
  Connect.addActionListener(this);
  }

  /* React on the Connect button. */
  public void actionPerformed (ActionEvent e)
  {
  if (e.getSource()==Connect)
    connect();
  }

  /* This does the connection and opens the irc window. */
  public void connect ()
  {
  try
    {
    new ezircWindow(serverName.getText(),Integer.parseInt(serverPort.getText()),nickName.getText(),channelName.getText())
      {
      public void end ()
        { }
      };
    }
  catch (Exception e) // connection problem
    {
    System.err.println(e);
    System.err.println("Error opening irc window.");
    }
  }

}


