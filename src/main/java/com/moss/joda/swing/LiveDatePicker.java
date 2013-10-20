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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class LiveDatePicker extends JPanel {
	public static void main(String[] args) {
		LiveDatePicker p = new LiveDatePicker();
		p.add(new DateSelectionListener(){
			public void dateSelected(YearMonthDay date) {
				System.out.println("Selected " + date);
			}
		});
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(p);
		panel.add(new JLabel("Hello There"));
		panel.add(new JTextField("Testing 123"));
		JFrame frame = new JFrame("Test Frame");
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.setVisible(true);
	}
	
//	private JXDatePicker p;
	private PopupCalendar popup = new PopupCalendar();
	private JButton pButton;
	private JTextField t = new JTextField();
	private YearMonthDay value;
	private List<DateTimeFormatter> formats = new LinkedList<DateTimeFormatter>();
	private List<DateSelectionListener> listeners = new LinkedList<DateSelectionListener>();
	
	private boolean uiUpdating = false;
	
	public LiveDatePicker(){
		formats.add(DateTimeFormat.shortDate());
		formats.add(ISODateTimeFormat.date());
		formats.add(ISODateTimeFormat.basicDate());
		formats.add(DateTimeFormat.mediumDate());
		formats.add(DateTimeFormat.fullDate());
		formats.add(DateTimeFormat.longDate());
		
//		p = new JXDatePicker();
//		pButton = p.getComponent(1);
//		p.getEditor().setVisible(false);
		
		pButton = new JButton("^");
		pButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Point p = pButton.getLocation();
				popup.show(pButton, 0, 0);

			}
		});
		popup.add(new DateSelectionListener(){
			public void dateSelected(YearMonthDay date) {
				setDate(date);
				fireSelection();
			}
		});
		
		t.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e) {
				handle();
			}
			public void insertUpdate(DocumentEvent e) {
				handle();
			}
			public void removeUpdate(DocumentEvent e) {
				handle();
			}
			void handle(){
				if(!uiUpdating){
					String text = t.getText();
					YearMonthDay date = null;
					for (int x=0;date==null && x<formats.size();x++){
						DateTimeFormatter f = formats.get(x);
						try {
							date = f.parseDateTime(text).toYearMonthDay();
						} catch (IllegalArgumentException e) {
						}
					}
					value = date;
					if(date!=null){
						popup.setDate(date);
					}
					fireSelection();
				}
			}
			
		});
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx=1;
		c.ipadx=10;
		add(t, c);
		c.weightx=0;
		c.ipadx=0;
//		c.insets.left = 5;
		add(pButton, c);
		
		setDate(new YearMonthDay());
	}
	
	private void fireSelection(){
		try {
			for (DateSelectionListener l : listeners) {
				l.dateSelected(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private int largest(int x, int y){
		if(x<y) return y;
		return x;
	}
	public void setDate(YearMonthDay date){
		uiUpdating = true;
		this.value = date;
		
		if (value == null) {
			t.setText("");
		}
		else {
			t.setText(formats.get(0).print(date));
		}
		
		popup.setDate(date);
		uiUpdating = false;
//		p.setDate(date.toDateMidnight().toDate());
	}
	public YearMonthDay getDate(){
		return value;
	}
	public void addFormat(DateTimeFormatter format){
		formats.add(format);
	}
	
	public List<DateTimeFormatter> getFormats() {
		return Collections.unmodifiableList(formats);
	}
	

	public void add(DateSelectionListener l){
		listeners.add(l);
	}
	
}
