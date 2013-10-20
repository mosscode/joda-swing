/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of joda-swing.
 *
 * joda-swing is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * joda-swing is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with joda-swing; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.joda.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXDatePicker;
import org.joda.time.DateTime;
import org.joda.time.TimeOfDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.moss.swing.dialog.YesNoBar;
import com.moss.swing.test.TestFrame;

public class JodaOptionPane {
	private static DateTimeFormatter[] timeFormats = new DateTimeFormatter[]{
		DateTimeFormat.forPattern("hh:mm:ss aa"),
		DateTimeFormat.forPattern("hh:mm aa")
		};
	public static DateTime showInputDialog(Component parent, String message){
		return showInputDialog(parent, message, message);
	}
	public static DateTime showInputDialog(Component parent, String title, String message){
		
		
		final JDialog dialog;
		
		Window parentWindow = SwingUtilities.getWindowAncestor(parent);
		if(parentWindow instanceof Dialog){
			dialog = new JDialog((Dialog) parentWindow, true);
		}else{
			dialog = new JDialog((Frame) parentWindow, true);
		}
		
		dialog.setTitle(title);
		final DateTimeInputPanel controls = new DateTimeInputPanel(new DateTime(), message, dialog);
		
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
		
		switch(controls.outcome){
		case CANCELLED:
			return null;
		case SUBMITTED:
			return controls.selection();
		}
		return null;
	}
	
	private static TimeOfDay parseTime(String text){
		if(text==null || text.trim().length()==0){
			return null;
		}
		else{
			DateTime paddedTime = null;
			for(DateTimeFormatter format: timeFormats){
				try{
					paddedTime = format.parseDateTime(text);
				}catch(IllegalArgumentException e){
					// just means the text is no good.
				}
			}
			
			if(paddedTime==null)
				throw new RuntimeException("Unparsable time: " + text);
			
			return new TimeOfDay(paddedTime.getHourOfDay(), paddedTime.getMinuteOfHour(), paddedTime.getSecondOfMinute());
		}
	}
	
	private enum InputOutcome {
		CANCELLED,
		SUBMITTED
	}
	
	private static class DateTimeInputPanel extends JPanel {
		JPanel controls = new JPanel();
		JXDatePicker datePicker = new JXDatePicker();
		JTextField timeField = new JTextField();
		InputOutcome outcome;
		
		public DateTimeInputPanel(final DateTime defaultValue, String message, final JDialog dialog) {
			
			controls.setLayout(new GridLayout(1, 0));
			controls.add(datePicker);
			controls.add(timeField);
			
			datePicker.setDate(defaultValue.toDate());
			timeField.setText(timeFormats[0].print(defaultValue));
			
			Action okAction = new AbstractAction("OK"){
				public void actionPerformed(ActionEvent e) {
					try{
						selection();
						outcome = InputOutcome.SUBMITTED;
						dialog.setVisible(false);
					}catch(Exception err){
						err.printStackTrace();
						JOptionPane.showMessageDialog(controls, "Invalid Entry: " + err.getMessage());
					}
				}
			};
			
			Action cancelAction = new AbstractAction("Cancel"){
				public void actionPerformed(ActionEvent e) {
					outcome = InputOutcome.CANCELLED;
					dialog.setVisible(false);
				}
			};
			
			YesNoBar yesNoBar = new YesNoBar(okAction, cancelAction);
			
			setLayout(new BorderLayout());
			add(new JLabel(message), BorderLayout.NORTH);
			add(controls);
			add(yesNoBar, BorderLayout.SOUTH);
			
			dialog.add(this);
		}
		
		public DateTime selection(){
			TimeOfDay time = parseTime(timeField.getText());
			System.out.println("Time of day: " + time);
			DateTime selection = time.toDateTime(new DateTime(datePicker.getDate().getTime()));
			return selection;
		}
	}
	
	public static void main(String[] args) {
		TestFrame f = new TestFrame();
		DateTime dt = showInputDialog(f, "Enter Date Please");
		System.out.println("Selection: " + dt);
	}
}
