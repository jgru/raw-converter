package com.jan_gruber.rawprocessor.view.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jan_gruber.rawprocessor.controller.IOController;

public class ConsolePanel extends JPanel implements Consumer {
    JTextArea mConsole;

    public ConsolePanel() {
	this.setLayout(new BorderLayout());
	mConsole = new JTextArea();
	mConsole.setBackground(this.getBackground());
	mConsole.setForeground(new Color(0,0,0));
	mConsole.setLineWrap(false);
	
	this.add(new JScrollPane(mConsole, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
	PrintStream ps = System.out;
         System.setOut(new PrintStream(new ConsoleOutputStream(ps, this)));
	//System.setOut(new PrintStream(mStream));
    }

    @Override
    public void appendText(final String text) {
        if (EventQueue.isDispatchThread()) {
            mConsole.append(text);
            mConsole.setCaretPosition(mConsole.getText().length());
        } else {

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    appendText(text);
                }
            });

        }
    }        
}
