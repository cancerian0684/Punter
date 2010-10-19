package com.sapient.kb.utils;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.EkitCoreSpell;

public class Ekit extends JFrame
  implements WindowListener
{
  private EkitCore ekitCore;
  private File currentFile = (File)null;

  public Ekit(String paramString1, String paramString2, String paramString3, URL paramURL, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, String paramString4, String paramString5, boolean paramBoolean5, boolean paramBoolean6, boolean paramBoolean7, boolean paramBoolean8, boolean paramBoolean9)
  {
    if (paramBoolean7)
    {
      this.ekitCore = new EkitCoreSpell(false, paramString1, paramString2, paramString3, null, paramURL, paramBoolean1, paramBoolean2, paramBoolean3, paramBoolean4, paramString4, paramString5, paramBoolean5, paramBoolean6, true, paramBoolean8, (paramBoolean8) ? "NW|NS|OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|FN|SP|UC|UM|SP|SR|*|BL|IT|UD|SP|SK|SU|SB|SP|AL|AC|AR|AJ|SP|UL|OL|SP|LK|*|ST|SP|FO" : "NW|NS|OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|BL|IT|UD|SP|FN|SP|UC|SP|LK|SP|SR|SP|ST", paramBoolean9);
    }
    else
    {
      this.ekitCore = new EkitCore(false, paramString1, paramString2, paramString3, null, paramURL, paramBoolean1, paramBoolean2, paramBoolean3, paramBoolean4, paramString4, paramString5, paramBoolean5, paramBoolean6, false, paramBoolean8, (paramBoolean8) ? "NW|NS|OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|FN|SP|UC|UM|SP|SR|*|BL|IT|UD|SP|SK|SU|SB|SP|AL|AC|AR|AJ|SP|UL|OL|SP|LK|*|ST|SP|FO" : "NW|NS|OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|BL|IT|UD|SP|FN|SP|UC|SP|LK|SP|SR|SP|ST", paramBoolean9);
    }

    this.ekitCore.setFrame(this);

    if (paramBoolean1)
    {
      if (paramBoolean8)
      {
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints localGridBagConstraints = new GridBagConstraints();
        localGridBagConstraints.fill = 2;
        localGridBagConstraints.anchor = 11;
        localGridBagConstraints.gridheight = 1;
        localGridBagConstraints.gridwidth = 1;
        localGridBagConstraints.weightx = 1.0D;
        localGridBagConstraints.weighty = 0.0D;
        localGridBagConstraints.gridx = 1;

        localGridBagConstraints.gridy = 1;
        getContentPane().add(this.ekitCore.getToolBarMain(paramBoolean1), localGridBagConstraints);

        localGridBagConstraints.gridy = 2;
        getContentPane().add(this.ekitCore.getToolBarFormat(paramBoolean1), localGridBagConstraints);

        localGridBagConstraints.gridy = 3;
        getContentPane().add(this.ekitCore.getToolBarStyles(paramBoolean1), localGridBagConstraints);

        localGridBagConstraints.anchor = 15;
        localGridBagConstraints.fill = 1;
        localGridBagConstraints.weighty = 1.0D;
        localGridBagConstraints.gridy = 4;
        getContentPane().add(this.ekitCore, localGridBagConstraints);
      }
      else
      {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(this.ekitCore, "Center");
        getContentPane().add(this.ekitCore.getToolBar(paramBoolean1), "North");
      }
    }
    else
    {
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(this.ekitCore, "Center");
    }

    setJMenuBar(this.ekitCore.getMenuBar());

    addWindowListener(this);

    updateTitle();
    pack();
    setVisible(true);
  }

  public Ekit()
  {
    this(null, null, null, null, true, false, true, true, null, null, false, false, false, true, false);
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    dispose();
    System.exit(0);
  }
  public void windowOpened(WindowEvent paramWindowEvent) {
  }
  public void windowClosed(WindowEvent paramWindowEvent) {
  }
  public void windowActivated(WindowEvent paramWindowEvent) {
  }
  public void windowDeactivated(WindowEvent paramWindowEvent) {
  }
  public void windowIconified(WindowEvent paramWindowEvent) {
  }
  public void windowDeiconified(WindowEvent paramWindowEvent) {  }

  private void updateTitle() { setTitle(new StringBuilder().append(this.ekitCore.getAppName()).append((this.currentFile == null) ? "" : new StringBuilder().append(" - ").append(this.currentFile.getName()).toString()).toString()); }


  public static void usage()
  {
    System.out.println("usage: com.hexidec.ekit.Ekit [-t|t+|T] [-s|S] [-m|M] [-x|X] [-b|B] [-v|V] [-p|P] [-fFILE] [-cCSS] [-rRAW] [-uURL] [-lLANG] [-d|D] [-h|H|?]");
    System.out.println("       Each option contained in [] brackets is optional,");
    System.out.println("       and can be one of the values separated be the | pipe.");
    System.out.println("       Each option must be proceeded by a - hyphen.");
    System.out.println("       The options are:");
    System.out.println("         -t|t+|T : -t = single toolbar, -t+ = multiple toolbars, -T = no toolbar");
    System.out.println("         -s|S    : -s = show source window on startup, -S = hide source window");
    System.out.println("         -m|M    : -m = show icons on menus, -M = no menu icons");
    System.out.println("         -x|X    : -x = exclusive document/source windows, -X = use split window");
    System.out.println("         -b|B    : -b = use Base64 document encoding, -B = use regular encoding");
    System.out.println("         -v|V    : -v = include spell checker, -V = omit spell checker");
    System.out.println("         -p|P    : -p = ENTER key inserts paragraph, -P = inserts break");
    System.out.println("         -fFILE  : load HTML document on startup (replace FILE with file name)");
    System.out.println("         -cCSS   : load CSS stylesheet on startup (replace CSS with file name)");
    System.out.println("         -rRAW   : load raw document on startup (replace RAW with file name)");
    System.out.println("         -uURL   : load document at URL on startup (replace URL with file URL)");
    System.out.println("         -lLANG  : specify the starting language (defaults to your locale)");
    System.out.println("                    replace LANG with xx_XX format (e.g., US English is en_US)");
    System.out.println("         -d|D    : -d = DEBUG mode on, -D = DEBUG mode off (developers only)");
    System.out.println("         -h|H|?  : print out this help information");
    System.out.println("         ");
    System.out.println("The defaults settings are equivalent to: -t+ -S -m -x -B -V -p -D");
    System.out.println("         ");
    System.out.println("For further information, read the README file.");
  }

  public static void main(String[] paramArrayOfString)
  {
    String str1 = null;
    String str2 = null;
    String str3 = null;
    URL localURL = null;
    boolean bool1 = true;
    boolean bool2 = true;
    boolean bool3 = false;
    boolean bool4 = true;
    boolean bool5 = true;
    String str4 = null;
    String str5 = null;
    boolean bool6 = false;
    boolean bool7 = false;
    boolean bool8 = false;
    boolean bool9 = false;
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      if ((paramArrayOfString[i].equals("-h")) || (paramArrayOfString[i].equals("-H")) || (paramArrayOfString[i].equals("-?")))
      {
        usage();
      } else if (paramArrayOfString[i].equals("-t")) { bool1 = true; bool2 = false;
      } else if (paramArrayOfString[i].equals("-t+")) { bool1 = true; bool2 = true;
      } else if (paramArrayOfString[i].equals("-T")) { bool1 = false; bool2 = false;
      } else if (paramArrayOfString[i].equals("-s")) { bool3 = true;
      } else if (paramArrayOfString[i].equals("-S")) { bool3 = false;
      } else if (paramArrayOfString[i].equals("-m")) { bool4 = true;
      } else if (paramArrayOfString[i].equals("-M")) { bool4 = false;
      } else if (paramArrayOfString[i].equals("-x")) { bool5 = true;
      } else if (paramArrayOfString[i].equals("-X")) { bool5 = false;
      } else if (paramArrayOfString[i].equals("-b")) { bool6 = true;
      } else if (paramArrayOfString[i].equals("-B")) { bool6 = false;
      } else if (paramArrayOfString[i].startsWith("-f")) { str1 = paramArrayOfString[i].substring(2, paramArrayOfString[i].length());
      } else if (paramArrayOfString[i].startsWith("-c")) { str2 = paramArrayOfString[i].substring(2, paramArrayOfString[i].length());
      } else if (paramArrayOfString[i].startsWith("-r")) { str3 = paramArrayOfString[i].substring(2, paramArrayOfString[i].length());
      } else if (paramArrayOfString[i].equals("-v")) { bool8 = true;
      } else if (paramArrayOfString[i].equals("-V")) { bool8 = false;
      } else if (paramArrayOfString[i].equals("-p")) { bool9 = false;
      } else if (paramArrayOfString[i].equals("-P")) { bool9 = true;
      } else if (paramArrayOfString[i].startsWith("-u"))
      {
        try
        {
          localURL = new URL(paramArrayOfString[i].substring(2, paramArrayOfString[i].length()));
        }
        catch (MalformedURLException localMalformedURLException)
        {
          localMalformedURLException.printStackTrace(System.err);
        }
      }
      else if (paramArrayOfString[i].startsWith("-l"))
      {
        if ((paramArrayOfString[i].indexOf('_') != 4) || (paramArrayOfString[i].length() < 7))
          continue;
        str4 = paramArrayOfString[i].substring(2, paramArrayOfString[i].indexOf('_'));
        str5 = paramArrayOfString[i].substring(paramArrayOfString[i].indexOf('_') + 1, paramArrayOfString[i].length());
      }
      else if (paramArrayOfString[i].equals("-d")) { bool7 = true; } else {
        if (!paramArrayOfString[i].equals("-D")) continue; bool7 = true;
      }
    }
    Ekit localEkit = new Ekit(str1, str2, str3, localURL, bool1, bool3, bool4, bool5, str4, str5, bool6, bool7, bool8, bool2, bool9);
    
    try {
		TimeUnit.SECONDS.sleep(10);
		System.out.println("Body");
		System.err.println(localEkit.ekitCore.getDocumentBody());
		System.out.println("text");
		System.err.println(localEkit.ekitCore.getDocumentText());
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
  }
}