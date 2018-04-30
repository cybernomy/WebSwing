package ensemble.samples.controls.clipboard;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.StringTokenizer;

public class TableUtils {

	private static NumberFormat numberFormatter = NumberFormat.getNumberInstance();

	
	/**
	 * Install the keyboard handler:
	 *   + CTRL + C = copy to clipboard
	 *   + CTRL + V = paste to clipboard
	 * @param table
	 */
	public static void installCopyPasteHandler(TableView<?> table) {

		// install copy/paste keyboard handler
		table.setOnKeyPressed(new TableKeyEventHandler());

	}

	/**
	 * Copy/Paste keyboard event handler.
	 * The handler uses the keyEvent's source for the clipboard data. The source must be of type TableView.
	 */
	public static class TableKeyEventHandler implements EventHandler<KeyEvent> {

		KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
		KeyCodeCombination pasteKeyCodeCompination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);

		public void handle(final KeyEvent keyEvent) {

			if (copyKeyCodeCompination.match(keyEvent)) {

				if( keyEvent.getSource() instanceof TableView) {
				
					// copy to clipboard
					copySelectionToClipboard( (TableView<?>) keyEvent.getSource());
	
					// event is handled, consume it
					keyEvent.consume();
					
				}

			} 
			else if (pasteKeyCodeCompination.match(keyEvent)) {

				if( keyEvent.getSource() instanceof TableView) {
				
					// copy to clipboard
					pasteFromClipboard( (TableView<?>) keyEvent.getSource());
	
					// event is handled, consume it
					keyEvent.consume();
					
				}

			} 

		}

	}

	/**
	 * Get table selection and copy it to the clipboard.
	 * @param table
	 */
	public static void copySelectionToClipboard(TableView<?> table) {

		StringBuilder clipboardString = new StringBuilder();

		ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

		int prevRow = -1;

		for (TablePosition position : positionList) {

			int row = position.getRow();
			int col = position.getColumn();

			// determine whether we advance in a row (tab) or a column
			// (newline).
			if (prevRow == row) {
				
				clipboardString.append('\t');
				
			} else if (prevRow != -1) {
				
				clipboardString.append('\n');
				
			}

			// create string from cell
			String text = "";
			
			Object observableValue = (Object) table.getColumns().get(col).getCellObservableValue( row);
			
			// null-check: provide empty string for nulls
			if (observableValue == null) {
				text = "";
			}
			else if( observableValue instanceof DoubleProperty) { // TODO: handle boolean etc
				
	    		text = numberFormatter.format( ((DoubleProperty) observableValue).get());

			}
	    	else if( observableValue instanceof IntegerProperty) { 

	    		text = numberFormatter.format( ((IntegerProperty) observableValue).get());
	    		
	    	}			    	
	    	else if( observableValue instanceof StringProperty) { 
	    		
	    		text = ((StringProperty) observableValue).get();
	    		
	    	}
	    	else {
	    		System.out.println("Unsupported observable value: " + observableValue);
	    	}

			// add new item to clipboard
			clipboardString.append(text);

			// remember previous
			prevRow = row;
		}

		// create clipboard content
		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(clipboardString.toString());

		// set clipboard content
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	
		
	}

	public static void pasteFromClipboard( TableView<?> table) {
		
		// abort if there's not cell selected to start with
		if( table.getSelectionModel().getSelectedCells().size() == 0) {
			return;
		}
				
		// get the cell position to start with
		TablePosition pasteCellPosition = table.getSelectionModel().getSelectedCells().get(0);
		
		System.out.println("Pasting into cell " + pasteCellPosition);
		
		String pasteString = Clipboard.getSystemClipboard().getString();
		
		System.out.println(pasteString);

		int rowClipboard = -1;
		
		StringTokenizer rowTokenizer = new StringTokenizer( pasteString, "\n");
		while( rowTokenizer.hasMoreTokens()) {

			rowClipboard++;
			
			String rowString = rowTokenizer.nextToken();
			
		    StringTokenizer columnTokenizer = new StringTokenizer( rowString, "\t");

		    int colClipboard = -1;
		    
		    while( columnTokenizer.hasMoreTokens()) {

		    	colClipboard++;

		    	// get next cell data from clipboard
		    	String clipboardCellContent = columnTokenizer.nextToken();

		    	// calculate the position in the table cell
		    	int rowTable = pasteCellPosition.getRow() + rowClipboard;
		    	int colTable = pasteCellPosition.getColumn() + colClipboard;

		    	// skip if we reached the end of the table
		    	if( rowTable >= table.getItems().size()) {
		    		continue;
		    	}
		    	if( colTable >= table.getColumns().size()) {
		    		continue;
		    	}
		    	
        	    // System.out.println( rowClipboard + "/" + colClipboard + ": " + cell);
		    	
		    	// get cell
		    	TableColumn tableColumn = table.getColumns().get(colTable);
		    	ObservableValue observableValue = tableColumn.getCellObservableValue(rowTable);
		    	
		    	System.out.println( rowTable + "/" + colTable + ": " +observableValue);
		    	
		    	// TODO: handle boolean, etc
		    	if( observableValue instanceof DoubleProperty) { 
		    		
					try {
						
						double value = numberFormatter.parse(clipboardCellContent).doubleValue();
						((DoubleProperty) observableValue).set(value);
						
					} catch (ParseException e) {
						e.printStackTrace();
					}
		    		
		    	}
		    	else if( observableValue instanceof IntegerProperty) { 
		    		
					try {
						
						int value = NumberFormat.getInstance().parse(clipboardCellContent).intValue();
						((IntegerProperty) observableValue).set(value);
						
					} catch (ParseException e) {
						e.printStackTrace();
					}
		    		
		    	}			    	
		    	else if( observableValue instanceof StringProperty) { 
		    		
		    		((StringProperty) observableValue).set(clipboardCellContent);
		    		
		    	} else {
		    		
		    		System.out.println("Unsupported observable value: " + observableValue);
		    		
		    	}
		    	
		    	System.out.println(rowTable + "/" + colTable);
		    }

		}

	}
	
}