package edu.cusat.gs.rawpacket;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;

public class RawPacketTest {

	public static void main(String args[]) {
		test();
	}

	public static void test() {
		// RawPacketManager manager = new RawPacketManager(JSatTrak app,
		// "TOPTOP", new File("data/packets/testLog.txt"));
		//
		//
		// PacketLogger log = new PacketLogger(new File("data/packets/TOPTOP"));
		// byte[] b = {0, 1, 2, 3, 4, 5};
		// log.logPacket(new Packet(b, new LocalDateTime(), new
		// LocalDateTime()));
		//
		// log.close();

		JXDatePicker picker = new JXDatePicker();
		// monthView.setPreferredCols(2);
		// monthView.setPreferredRows(2);

		JFrame frame = new JFrame();

		JXMonthView monthView = picker.getMonthView();
		frame.getContentPane().add(monthView);
		// frame.pack();
		frame.setVisible(true);

		// Change the selection mode to select full weeks.
		// monthView.getMonthView().setSelectionModel(JXMonthView.WEEK_SELECTION);

		// Add an action listener that will be notified when the user
		// changes selection via the mouse.
		picker.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.out.println(((JXMonthView) ((JXDatePicker) e.getSource())
						.getMonthView()).getSelection());

				((JXMonthView) ((JXDatePicker) e.getSource()).getMonthView())
						.getSelection();
			}
		});
	}
}