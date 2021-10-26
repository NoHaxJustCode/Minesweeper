import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
public class Minesweeper extends JFrame implements ActionListener,MouseListener
{
	JToggleButton[][] board;
	JPanel boardPanel;
	JMenuBar menuBar;
	JMenu menu;
	JMenuItem[] difficulty;
	JButton reset;
	JMenu icons;
	boolean firstClick;
	int numMines;
	ImageIcon[] numbers;
	Font mineFont;
	ImageIcon mineIcon;
	ImageIcon flag;
	ImageIcon win, lose, defaulty;
	GraphicsEnvironment ge;
	String[] difficultyN={"Easy", "Normal", "Hard"};
	Timer timer;
	int timePassed;
	JTextField timeField;

	public Minesweeper()
	{
		firstClick=true;
		try
		{
			ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
			mineFont=Font.createFont(Font.TRUETYPE_FONT, new File("mine-sweeper.ttf"));
			ge.registerFont(mineFont);
		}catch(IOException|FontFormatException e){}
		//System.out.println(mineFont);
		numbers=new ImageIcon[8];
		for(int x=0; x<8; x++)
		{
			numbers[x]=new ImageIcon((x+1)+".png");
			numbers[x]=new ImageIcon(numbers[x].getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		}
		UIManager.put("ToggleButton.select", Color.LIGHT_GRAY);
		mineIcon=new ImageIcon("mine.png");
		mineIcon=new ImageIcon(mineIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		win=new ImageIcon("win.png");
		win=new ImageIcon(win.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		lose=new ImageIcon("lose.png");
		lose=new ImageIcon(lose.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		defaulty=new ImageIcon("smile.png");
		defaulty=new ImageIcon(defaulty.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		flag=new ImageIcon("flag.png");
		flag=new ImageIcon(flag.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		createBoard(9,9);
		numMines=10;
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void createBoard(int row, int col)
	{
		if(boardPanel!=null)
		{
			this.remove(boardPanel);
			this.remove(menuBar);
		}
		boardPanel=new JPanel();
		board=new JToggleButton[row][col];
		boardPanel.setLayout(new GridLayout(row, col));
		menuBar=new JMenuBar();
		menuBar.setLayout(new GridLayout(1,2));
		menu=new JMenu("Difficulty");
		difficulty=new JMenuItem[difficultyN.length];
		reset=new JButton("Reset");
		reset.addActionListener(this);
		timeField=new JTextField();
		//timeField.setFont(mineFont);
		icons=new JMenu();
		icons.setIcon(defaulty);
		for(int x=0; x<difficultyN.length;x++)
		{
			difficulty[x]=new JMenuItem(difficultyN[x]);
			difficulty[x].addActionListener(this);
			menu.add(difficulty[x]);
		}
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				board[r][c]=new JToggleButton();
				board[r][c].putClientProperty("row", r);
				board[r][c].putClientProperty("col", c);
				board[r][c].putClientProperty("state", 0);
				board[r][c].setBorder(BorderFactory.createBevelBorder(0));
				board[r][c].setFont(mineFont.deriveFont(15f));
				board[r][c].setFocusPainted(false);
				board[r][c].addMouseListener(this);
				boardPanel.add(board[r][c]);
			}
		}
		this.setSize(40*col, 40*row);
		menuBar.add(menu);
		menuBar.add(reset);
		menuBar.add(timeField);
		menuBar.add(icons);
		this.add(menuBar, BorderLayout.NORTH);
		this.add(boardPanel, BorderLayout.CENTER);
		this.revalidate();
	}

	public void actionPerformed(ActionEvent e)
	{
		for(int x=0; x<difficultyN.length;x++)
		{
			if(e.getSource()==difficulty[x])
			{
				if(x==0)
				{
					numMines=9;
					firstClick=true;
					createBoard(9,9);
				}
				if(x==1)
				{
					numMines=40;
					firstClick=true;
					createBoard(16,16);
				}
				if(x==2)
				{
					numMines=99;
					firstClick=true;
					createBoard(16,40);
				}
			}
		}
		if(e.getSource()==reset)
		{
			icons.setIcon(defaulty);
			timePassed=0;
			timeField.setText(""+timePassed);
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		int row=(int)((JToggleButton)e.getComponent()).getClientProperty("row");
		int col=(int)((JToggleButton)e.getComponent()).getClientProperty("col");
		if(e.getButton()==MouseEvent.BUTTON1&& board[row][col].isEnabled())
		{
			if(firstClick)
			{
				setMinesAndCounts(row, col);
				firstClick=false;
				timer = new Timer();
				timer.schedule(new UpdateTimer(),(long)0,(long)1000);
			}
			int state=(int)board[row][col].getClientProperty("state");
			if(state==-1)
			{
				board[row][col].setIcon(mineIcon);
				board[row][col].setBackground(Color.RED);
				board[row][col].setContentAreaFilled(false);
				board[row][col].setOpaque(true);
				for(int r=0; r<board.length; r++)
				{
					for(int c=0; c<board[0].length; c++)
					{
						int bomb = (int)board[r][c].getClientProperty("state");
						if(bomb==-1)
						{
							board[r][c].setSelected(true);
							board[r][c].setIcon(mineIcon);
							board[r][c].setDisabledIcon(mineIcon);
						}
						board[r][c].setEnabled(false);
					}
				}
				//JOptionPane.showMessageDialog(null, "You Lost");
				icons.setIcon(lose);
				timer.cancel();
			}
			else
			{
				expand(row,col);
				checkWin();
			}

		}
		if(e.getButton()==MouseEvent.BUTTON3)
		{
			if(!firstClick)
			{
				if(!board[row][col].isSelected())
				{
					if(board[row][col].getIcon()==null)
					{
						board[row][col].setIcon(flag);
						board[row][col].setDisabledIcon(flag);
						board[row][col].setEnabled(false);
					}
					else
					{
						board[row][col].setIcon(null);
						board[row][col].setDisabledIcon(null);
						board[row][col].setEnabled(true);
					}
				}
			}
		}
	}
	public void setMinesAndCounts(int curRow, int curCol)
	{
		int count=numMines;
		int dimR=board.length;
		int dimC=board[0].length;
		while(count>0)
		{
			int randR=(int)(Math.random()*dimR);
			int randC=(int)(Math.random()*dimC);
			int state=(int)((JToggleButton)board[randR][randC]).getClientProperty("state");
			if(state==0&&(Math.abs(randR-curRow)>1 || Math.abs(randC-curCol)>1))
			{
				board[randR][randC].putClientProperty("state", -1);
				count--;
			}
		}
		for(int r=0; r<dimR; r++)
		{
			for(int c=0; c<dimC; c++)
			{
				count=0;
				int currToggle=Integer.parseInt(""+board[r][c].getClientProperty("state"));
				if(currToggle!=-1)
				{
					for(int i=r-1; i<=r+1; i++)
					{
						for(int j=c-1; j<=c+1; j++)
						{
							try{
								int toggleState=Integer.parseInt(""+board[i][j].getClientProperty("state"));
								if(toggleState==-1&& !(i==r && j==c))
								{
									count++;
								}
							}catch(ArrayIndexOutOfBoundsException e)
							{}
						}
					}
					board[r][c].putClientProperty("state", count);
				}
			}
		}
	}
	public void write(int row, int col, int state)
	{
		Color color;
		switch(state)
		{
			case 1: color=Color.BLUE;
				break;
			case 2: color=Color.GREEN;
				break;
			case 3: color=Color.RED;
				break;
			case 4: color=new Color(128, 0, 128);
				break;
			case 5: color=new Color(0, 128, 0);
				break;
			case 6: color=Color.MAGENTA;
				break;
			case 7: color=Color.ORANGE;
				break;
			default: color=Color.CYAN;
				break;
		}
		if(state>0)
		{
			board[row][col].setIcon(numbers[state-1]);
			board[row][col].setDisabledIcon(numbers[state-1]);
		}
	}
	public void expand(int row, int col)
	{
		if(!board[row][col].isSelected())
			board[row][col].setSelected(true);
		int state=(int)board[row][col].getClientProperty("state");
		if(state!=0)
		{
			write(row, col, state);
		}
		else
		{
			for(int r3x3=row-1; r3x3<=row+1; r3x3++)
			{
				for(int c3x3=col-1; c3x3<=col+1; c3x3++)
				{
					try
					{
						if(!board[r3x3][c3x3].isSelected())
						{
							expand(r3x3, c3x3);
						}
					}catch(ArrayIndexOutOfBoundsException e)
					{}
				}
			}
		}
	}
	public void checkWin()
	{
		int dimR=board.length;
		int dimC=board[0].length;
		int totalSpaces=dimR*dimC;
		int count=0;
		for(int r=0; r<dimR; r++)
		{
			for(int c=0; c<dimC; c++)
			{
				int state=(int)board[r][c].getClientProperty("state");
				if(board[r][c].isSelected())
					count++;
			}
		}
		if(totalSpaces-count==numMines)
		{
			//JOptionPane.showMessageDialog(null, "You Win");
			icons.setIcon(win);
			timer.cancel();
		}
	}

	public void mouseClicked(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public class UpdateTimer extends TimerTask
	{
		public void run()
		{
			timePassed++;
			timeField.setText(""+timePassed);
		}
	}
	public static void main(String[] args)
	{
		Minesweeper game=new Minesweeper();
	}
}