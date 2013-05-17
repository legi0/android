package com.VertexVerveInc.Games;

import java.util.Random;


import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MinesweeperGame extends Activity
{
	private TextView txtMineCount;
	private TextView txtTimer;
	private ImageButton btnSmile;

	private TableLayout mineField; // diseño de la mesa para añadir minas

	private Block blocks[][]; //bloques de campo de minas
	private int blockDimension = 24; //anchura de cada bloque
	private int blockPadding = 2; // relleno entre bloques

	private int numberOfRowsInMineField = 9;
	private int numberOfColumnsInMineField = 9;
	private int totalNumberOfMines = 10;

	// cronómetro para controlar el tiempo transcurrido
	private Handler timer = new Handler();
	private int secondsPassed = 0;

	private boolean isTimerStarted; // comprobar si el temporizador ya iniciada o no
	private boolean areMinesSet; //comprobar si las minas se siembran en bloques
	private boolean isGameOver;
	private int minesToFind; // número de minas aún por descubrir

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		txtMineCount = (TextView) findViewById(R.id.MineCount);
		txtTimer = (TextView) findViewById(R.id.Timer);
		
		// programar el estilo de fuente para el recuento del temporizador y el mío al estilo de LCD
		Typeface lcdFont = Typeface.createFromAsset(getAssets(),
				"fonts/lcd2mono.ttf");
		txtMineCount.setTypeface(lcdFont);
		txtTimer.setTypeface(lcdFont);
		
		btnSmile = (ImageButton) findViewById(R.id.Smiley);
		btnSmile.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				endExistingGame();
				startNewGame();
			}
		});
		
		mineField = (TableLayout)findViewById(R.id.MineField);
		
		showDialog("Click en emoticon para jugar", 2000, true, false);
	}

	private void startNewGame()
	{
		// minas y restos vegetales do de los cálculos
		createMineField();
		// mostrar todos los bloques de la interfaz de usuario
		showMineField();
		
		minesToFind = totalNumberOfMines;
		isGameOver = false;
		secondsPassed = 0;
	}

	private void showMineField()
	{
		// Recordemos que no mostraremos 0 ª y última filas y columnas
		// que se utilizan para fines de cálculo sólo
		for (int row = 1; row < numberOfRowsInMineField + 1; row++)
		{
			TableRow tableRow = new TableRow(this);  
			tableRow.setLayoutParams(new LayoutParams((blockDimension + 2 * blockPadding) * numberOfColumnsInMineField, blockDimension + 2 * blockPadding));

			for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
			{
				blocks[row][column].setLayoutParams(new LayoutParams(  
						blockDimension + 2 * blockPadding,  
						blockDimension + 2 * blockPadding)); 
				blocks[row][column].setPadding(blockPadding, blockPadding, blockPadding, blockPadding);
				tableRow.addView(blocks[row][column]);
			}
			mineField.addView(tableRow,new TableLayout.LayoutParams(  
					(blockDimension + 2 * blockPadding) * numberOfColumnsInMineField, blockDimension + 2 * blockPadding));  
		}
	}

	private void endExistingGame()
	{
		stopTimer(); // detenerse si el temporizador está en marcha
		txtTimer.setText("000"); // Reiniciar todos los textos
		txtMineCount.setText("000"); // reiniciar cuenta minas
		btnSmile.setBackgroundResource(R.drawable.smile);
		
		// eliminar todas las filas del campo minado TableLayout
		mineField.removeAllViews();
		
		// configurar todas las variables para apoyar el final del partido
		isTimerStarted = false;
		areMinesSet = false;
		isGameOver = false;
		minesToFind = 0;
	}

	private void createMineField()
	{
		// Tomamos una fila fila adicional para cada lado
		// En general dos filas adicionales y dos columnas adicionales
		// Primera y la fila / columna última se utilizan sólo con fines cálculos
		// La fila y columnas marcadas como x sólo son utilizadas para mantener los recuentos de cerca por las minas

		blocks = new Block[numberOfRowsInMineField + 2][numberOfColumnsInMineField + 2];

		for (int row = 0; row < numberOfRowsInMineField + 2; row++)
		{
			for (int column = 0; column < numberOfColumnsInMineField + 2; column++)
			{	
				blocks[row][column] = new Block(this);
				blocks[row][column].setDefaults();

				// Pasar fila actual y el número de columna como int definitiva es que los detectores de eventos
				// De esta manera podemos asegurar que cada detector de eventos se asocia a
				// Particular, instancia de bloque sólo
				final int currentRow = row;
				final int currentColumn = column;

				// Añadir Click Listener
				// Esto se trata como si hiciera click izquierdo del ratón
				blocks[row][column].setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						// comenzar con temporizador en la primera posición
						if (!isTimerStarted)
						{
							startTimer();
							isTimerStarted = true;
						}

						// establecer minas en primer clic
						if (!areMinesSet)
						{
							areMinesSet = true;
							setMines(currentRow, currentColumn);
						}

						// Esto no es el primer clic
						// Comprobar si el bloque actual está marcado
						// Si la bandera no hacen nada
						// Lo que la operación es manejada por LongClick
						// Si el bloque no se encuentra en posición y luego descubrir los bloques cercanos
						// Hasta que lleguemos minas numeradas
						if (!blocks[currentRow][currentColumn].isFlagged())
						{
							// abrir los bloques cercanos hasta que lleguemos bloques numerados
							rippleUncover(currentRow, currentColumn);
							
							// Qué hemos hecho clic una mina
							if (blocks[currentRow][currentColumn].hasMine())
							{
								// game over
								finishGame(currentRow,currentColumn);
							}

							// check si ganamos el juego
							if (checkGameWin())
							{
								// marcar el juego como ganado!
								winGame();
							}
						}
					}
				});

				// Agregamos Long Click listener
				// Como si hicieramos click derecho con el mouse
				blocks[row][column].setOnLongClickListener(new OnLongClickListener()
				{
					public boolean onLongClick(View view)
					{
						// simula click medio
						// Si se trata de una pulsación larga en una mina abierta luego
						// Abrir todos los bloques circundantes
						if (!blocks[currentRow][currentColumn].isCovered() && (blocks[currentRow][currentColumn].getNumberOfMinesInSorrounding() > 0) && !isGameOver)
						{
							int nearbyFlaggedBlocks = 0;
							for (int previousRow = -1; previousRow < 2; previousRow++)
							{
								for (int previousColumn = -1; previousColumn < 2; previousColumn++)
								{
									if (blocks[currentRow + previousRow][currentColumn + previousColumn].isFlagged())
									{
										nearbyFlaggedBlocks++;
									}
								}
							}

							// Si el número de bloque marcado es igual a cerca del número de minas
							// A continuación, abra los bloques cercanos
							if (nearbyFlaggedBlocks == blocks[currentRow][currentColumn].getNumberOfMinesInSorrounding())
							{
								for (int previousRow = -1; previousRow < 2; previousRow++)
								{
									for (int previousColumn = -1; previousColumn < 2; previousColumn++)
									{
										// no abra bloques marcados
										if (!blocks[currentRow + previousRow][currentColumn + previousColumn].isFlagged())
										{
											// bloques abiertos hasta que lleguemos bloque numerada
											rippleUncover(currentRow + previousRow, currentColumn + previousColumn);

											// hemos hecho clic una mina
											if (blocks[currentRow + previousRow][currentColumn + previousColumn].hasMine())
											{
												// game over
												finishGame(currentRow + previousRow, currentColumn + previousColumn);
											}

											//ganamos el juego
											if (checkGameWin())
											{
												// marcamos como ganado
												winGame();
											}
										}
									}
								}
							}

							// Que ya no queremos juzgar este gesto tan regreso
							// No regresar de aquí en realidad desencadenar otra acción
							// Que puede ser marcado como marca bandera o pregunta en blanco
							return true;
						}

						// si el bloque clic está activada, puede hacer clic o marcado
						if (blocks[currentRow][currentColumn].isClickable() && 
								(blocks[currentRow][currentColumn].isEnabled() || blocks[currentRow][currentColumn].isFlagged()))
						{

							// por los clics largos establecen:
							// 1. bloques vacíos al marcado
							// 2. pabellón de signo de interrogación
							// 3. signo de interrogación para dejar en blanco

							// caso 1. establecer bloques en blanco para marcado
							if (!blocks[currentRow][currentColumn].isFlagged() && !blocks[currentRow][currentColumn].isQuestionMarked())
							{
								blocks[currentRow][currentColumn].setBlockAsDisabled(false);
								blocks[currentRow][currentColumn].setFlagIcon(true);
								blocks[currentRow][currentColumn].setFlagged(true);
								minesToFind--; //reduce mine count
								updateMineCountDisplay();
							}
							// el caso 2. establecer con bandera de signo de interrogación
							else if (!blocks[currentRow][currentColumn].isQuestionMarked())
							{
								blocks[currentRow][currentColumn].setBlockAsDisabled(true);
								blocks[currentRow][currentColumn].setQuestionMarkIcon(true);
								blocks[currentRow][currentColumn].setFlagged(false);
								blocks[currentRow][currentColumn].setQuestionMarked(true);
								minesToFind++; // aumentar el número de minas
								updateMineCountDisplay();
							}
							// caso 3. cambiar de cuadro en blanco
							else
							{
								blocks[currentRow][currentColumn].setBlockAsDisabled(true);
								blocks[currentRow][currentColumn].clearAllIcons();
								blocks[currentRow][currentColumn].setQuestionMarked(false);
								// si se marca a continuación, incrementar la cuenta mía
								if (blocks[currentRow][currentColumn].isFlagged())
								{
									minesToFind++; // aumentar el número de minas
									updateMineCountDisplay();
								}
								// eliminar el estado marcado
								blocks[currentRow][currentColumn].setFlagged(false);
							}
							
							updateMineCountDisplay(); // actualizar la mía visualizar
						}

						return true;
					}
				});
			}
		}
	}

	private boolean checkGameWin()
	{
		for (int row = 1; row < numberOfRowsInMineField + 1; row++)
		{
			for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
			{
				if (!blocks[row][column].hasMine() && blocks[row][column].isCovered())
				{
					return false;
				}
			}
		}
		return true;
	}

	private void updateMineCountDisplay()
	{
		if (minesToFind < 0)
		{
			txtMineCount.setText(Integer.toString(minesToFind));
		}
		else if (minesToFind < 10)
		{
			txtMineCount.setText("00" + Integer.toString(minesToFind));
		}
		else if (minesToFind < 100)
		{
			txtMineCount.setText("0" + Integer.toString(minesToFind));
		}
		else
		{
			txtMineCount.setText(Integer.toString(minesToFind));
		}
	}

	private void winGame()
	{
		stopTimer();
		isTimerStarted = false;
		isGameOver = true;
		minesToFind = 0; //colocar el contador de minas a 0

		//colocar icono de duda
		btnSmile.setBackgroundResource(R.drawable.cool);

		updateMineCountDisplay(); // update mine count

		// Desabilitar todos los botones
		// configurar marcados todos los bloques sin bandera
		for (int row = 1; row < numberOfRowsInMineField + 1; row++)
		{
			for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
			{
				blocks[row][column].setClickable(false);
				if (blocks[row][column].hasMine())
				{
					blocks[row][column].setBlockAsDisabled(false);
					blocks[row][column].setFlagIcon(true);
				}
			}
		}

		// Mostrar Mensaje
		showDialog("Ganaste en " + Integer.toString(secondsPassed) + " segundos!", 1000, false, true);
	}

	private void finishGame(int currentRow, int currentColumn)
	{
		isGameOver = true; // Marcar juego como terminado
		stopTimer(); // Parar Cronometro
		isTimerStarted = false;
		btnSmile.setBackgroundResource(R.drawable.sad);

		// Mostrar todas las minas
		// Desabilitar los bloques
		for (int row = 1; row < numberOfRowsInMineField + 1; row++)
		{
			for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
			{
				// Desabilitar los bloques
				blocks[row][column].setBlockAsDisabled(false);
				
				// block tiene una mina y no esta con bandera
				if (blocks[row][column].hasMine() && !blocks[row][column].isFlagged())
				{
					// colocar icono de mina
					blocks[row][column].setMineIcon(false);
				}

				// Block esta con bandera pero no tiene mina
				if (!blocks[row][column].hasMine() && blocks[row][column].isFlagged())
				{
					// colocar icono de bandera 
					blocks[row][column].setFlagIcon(false);
				}

				// block esta con bandera
				if (blocks[row][column].isFlagged())
				{
					// desabilitar boton
					blocks[row][column].setClickable(false);
				}
			}
		}

		// desencadenar mina
		blocks[currentRow][currentColumn].triggerMine();

		// mostrar mensaje
		showDialog("Trataste en " + Integer.toString(secondsPassed) + " segundos!", 1000, false, false);
	}


	private void setMines(int currentRow, int currentColumn)
	{
		// establecer minas excluyendo la ubicación donde el usuario hace clic
		Random rand = new Random();
		int mineRow, mineColumn;

		for (int row = 0; row < totalNumberOfMines; row++)
		{
			mineRow = rand.nextInt(numberOfColumnsInMineField);
			mineColumn = rand.nextInt(numberOfRowsInMineField);
			if ((mineRow + 1 != currentColumn) || (mineColumn + 1 != currentRow))
			{
				if (blocks[mineColumn + 1][mineRow + 1].hasMine())
				{
					row--; // mina ya está allí, no se repiten para un mismo bloque
				}
				// colocar mina en esta hubicacion 
				blocks[mineColumn + 1][mineRow + 1].plantMine();
			}
			// excluye el usuario hace clic en ubicación
			else
			{
				row--;
			}
		}

		int nearByMineCount;

		// contar el número de minas en los alrededores de los bloques
		for (int row = 0; row < numberOfRowsInMineField + 2; row++)
		{
			for (int column = 0; column < numberOfColumnsInMineField + 2; column++)
			{
				//para cada bloque encuentra cerca recuento mina
				nearByMineCount = 0;
				if ((row != 0) && (row != (numberOfRowsInMineField + 1)) && (column != 0) && (column != (numberOfColumnsInMineField + 1)))
				{
					// check in all nearby blocks
					for (int previousRow = -1; previousRow < 2; previousRow++)
					{
						for (int previousColumn = -1; previousColumn < 2; previousColumn++)
						{
							if (blocks[row + previousRow][column + previousColumn].hasMine())
							{
								// comprobar en todos los bloques cercanos
								nearByMineCount++;
							}
						}
					}

					blocks[row][column].setNumberOfMinesInSurrounding(nearByMineCount);
				}
				// para las filas laterales (0 ª y última fila / columna)
				// ajusta cuenta como 9 y marcarlo como leído
				else
				{
					blocks[row][column].setNumberOfMinesInSurrounding(9);
					blocks[row][column].OpenBlock();
				}
			}
		}
	}

	private void rippleUncover(int rowClicked, int columnClicked)
	{
		// no abra filas marcadas o extraído
		if (blocks[rowClicked][columnClicked].hasMine() || blocks[rowClicked][columnClicked].isFlagged())
		{
			return;
		}

		// abrir bloque clickeado
		blocks[rowClicked][columnClicked].OpenBlock();

		// si hace clic bloque tiene minas cercanas y luego no abrir más
		if (blocks[rowClicked][columnClicked].getNumberOfMinesInSorrounding() != 0 )
		{
			return;
		}

		// abrirá el próximo 3 filas y 3 columnas de forma recursiva
		for (int row = 0; row < 3; row++)
		{
			for (int column = 0; column < 3; column++)
			{
				// Comprobar todas las condiciones que haya marcado
				// Si cumplen los bloques posteriores se abren
				if (blocks[rowClicked + row - 1][columnClicked + column - 1].isCovered()
						&& (rowClicked + row - 1 > 0) && (columnClicked + column - 1 > 0)
						&& (rowClicked + row - 1 < numberOfRowsInMineField + 1) && (columnClicked + column - 1 < numberOfColumnsInMineField + 1))
				{
					rippleUncover(rowClicked + row - 1, columnClicked + column - 1 );
				} 
			}
		}
		return;
	}

	public void startTimer()
	{
		if (secondsPassed == 0) 
		{
			timer.removeCallbacks(updateTimeElasped);
			// decirle temporizador para ejecutar llamar después de 1 segundo
			timer.postDelayed(updateTimeElasped, 1000);
		}
	}

	public void stopTimer()
	{
		// desactivar rellamadas
		timer.removeCallbacks(updateTimeElasped);
	}

	// temporizador devolver la llamada cuando está marcada temporizador
	private Runnable updateTimeElasped = new Runnable()
	{
		public void run()
		{
			long currentMilliseconds = System.currentTimeMillis();
			++secondsPassed;

			if (secondsPassed < 10)
			{
				txtTimer.setText("00" + Integer.toString(secondsPassed));
			}
			else if (secondsPassed < 100)
			{
				txtTimer.setText("0" + Integer.toString(secondsPassed));
			}
			else
			{
				txtTimer.setText(Integer.toString(secondsPassed));
			}
 
			// agregar notificacion
			timer.postAtTime(this, currentMilliseconds);
			// Notificar a llamar después de 1 segundo
			// Básicamente a permanecer en el bucle temporizador
			timer.postDelayed(updateTimeElasped, 1000);
		}
	};
	
	private void showDialog(String message, int milliseconds, boolean useSmileImage, boolean useCoolImage)
	{
		// Mostrar mensaje
		Toast dialog = Toast.makeText(
				getApplicationContext(),
				message,
				Toast.LENGTH_LONG);

		dialog.setGravity(Gravity.CENTER, 0, 0);
		LinearLayout dialogView = (LinearLayout) dialog.getView();
		ImageView coolImage = new ImageView(getApplicationContext());
		if (useSmileImage)
		{
			coolImage.setImageResource(R.drawable.smile);
		}
		else if (useCoolImage)
		{
			coolImage.setImageResource(R.drawable.cool);
		}
		else
		{
			coolImage.setImageResource(R.drawable.sad);
		}
		dialogView.addView(coolImage, 0);
		dialog.setDuration(milliseconds);
		dialog.show();
	}
}