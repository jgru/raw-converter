package com.jan_gruber.rawprocessor.view.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

//inspired by: http://stackoverflow.com/questions/9776465/how-to-visualize-console-java-in-jframe-jpanel
public class ConsoleOutputStream extends OutputStream {
    private Consumer mConsumer;
    private StringBuilder mBuilder;
private PrintStream oldStream;

    public ConsoleOutputStream(PrintStream oldStream, Consumer c) {
	this.mBuilder= new StringBuilder(128);
	this.mConsumer=c;
	this.oldStream= oldStream;

    }

    @Override
    public void write(int b) throws IOException {
	char c= (char)b;
	String s= Character.toString(c);
	mBuilder.append((char) b);
	
	if (s.equals("\n")) {
	    //if there's a new line-> write to textArea
	    mConsumer.appendText(mBuilder.toString());
           mBuilder.delete(0, mBuilder.length());
            mBuilder.append("<<");
        }
	

	oldStream.print(c);

    
    }
}
