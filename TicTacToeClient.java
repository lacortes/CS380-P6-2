import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


// Luis Cortes
// CS 380
// Poject 6


public class TicTacToeClient {
	public static byte[][] gameBoard;

	public static void main(String[] args) {
		BoardMessage.Status playingGame; // Hold status of game

		try (Socket socket = new Socket("codebank.xyz", 38006)) {
			Scanner kb = new Scanner(System.in);
			System.out.println("Connected");

			// Setup streams for serialization
			ObjectOutputStream outStream = new ObjectOutputStream(
				new PrintStream(socket.getOutputStream(), true));
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream()); 
		
			// Identify self to server
			ConnectMessage connectMsg = new ConnectMessage("Luis Cortes");
			outStream.writeObject(connectMsg);

			// Send first Command message. Begin new game
			CommandMessage commandMsg = new CommandMessage(CommandMessage.Command.NEW_GAME);
			outStream.writeObject(commandMsg);

			// Read in board message from server
			Message boardMsg = (Message)inStream.readObject();
			gameBoard = ((BoardMessage)boardMsg).getBoard();
			showBoard();

			playingGame = ((BoardMessage)boardMsg).getStatus();

			byte row;
			byte col;
			System.out.println("Your symbol: X");

			// Make move while game is in progress
			while(playingGame == BoardMessage.Status.IN_PROGRESS) {
				System.out.print("Enter new row Position: ");
				row = kb.nextByte();
				
				System.out.print("Enter new col Position: ");
				col = kb.nextByte();

				if (row >= 3 || col >= 3) {
					System.out.println("ERROR: Enter values from 0-2 ");
					continue;
				} 

				// Make move
				MoveMessage moveMsg = new MoveMessage(row, col);
				outStream.writeObject(moveMsg);

				boardMsg = (Message)inStream.readObject();

				// Check for error
				MessageType type = boardMsg.getType();

				if (type == MessageType.ERROR) { // Error found
					ErrorMessage error = (ErrorMessage)boardMsg;
					System.out.println(error.getError());
					System.out.println("TRY AGAIN!!");
				} else { // Play as normal
					gameBoard = ((BoardMessage)boardMsg).getBoard();
					playingGame = ((BoardMessage)boardMsg).getStatus();

					showBoard();	
				}
			}

			// Game ended, show results
			System.out.println(playingGame.toString());

		} catch (Exception e) {e.printStackTrace();}
	}

	/** 
	 *	Display the board 
	 */
	public static void showBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				byte square = gameBoard[i][j];

				switch(square) {
					case 0: // Empty Square
						System.out.printf("%2s", '*');
						break;
					case 1: // Player one -> X
						System.out.printf("%2s", 'X');
						break;
					case 2: // Player two -> O
						System.out.printf("%2s", 'O');
						break;
				}
			}
			System.out.println();
		}
	}
}