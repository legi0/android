package com.VertexVerveInc.Games;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class Block extends Button
{
	private boolean isCovered; // todavía está cubierto de bloques
	private boolean isMined; // qué el módulo tiene una mina debajo
	private boolean isFlagged; // es un bloque marcado como una mina potencial
	private boolean isQuestionMarked; // está marcado ? bloque
	private boolean isClickable; // bloque puede aceptar eventos de clic
	private int numberOfMinesInSurrounding; // número de minas en los bloques cercanos

	public Block(Context context)
	{
		super(context);
	}

	public Block(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public Block(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	// establecer las propiedades predeterminadas para el bloque
	public void setDefaults()
	{
		isCovered = true;
		isMined = false;
		isFlagged = false;
		isQuestionMarked = false;
		isClickable = true;
		numberOfMinesInSurrounding = 0;

		this.setBackgroundResource(R.drawable.square_blue);
		setBoldFont();
	}

	// Marcar el bloque como desabilidato/abierto
	// Actualizar el número de minas cercanas
	public void setNumberOfSurroundingMines(int number)
	{
		this.setBackgroundResource(R.drawable.square_grey);
		
		updateNumber(number);
	}

	// Configurar icono mina para el bloque
	// Set de bloques como deshabilitar  / abrir si se pasa false
	public void setMineIcon(boolean enabled)
	{
		this.setText("M");

		if (!enabled)
		{
			this.setBackgroundResource(R.drawable.square_grey);
			this.setTextColor(Color.RED);
		}
		else
		{
			this.setTextColor(Color.BLACK);
		}
	}

	// Establecer la mía marcado
	// establecer bloques como deshabilidatos/abrir si se pasa false
	public void setFlagIcon(boolean enabled)
	{
		this.setText("F");

		if (!enabled)
		{
			this.setBackgroundResource(R.drawable.square_grey);
			this.setTextColor(Color.RED);
		}
		else
		{
			this.setTextColor(Color.BLACK);
		}
	}

	// Establecer mina como signo de interrogación
	// Establecer de bloques como deshabilitar / abrir si se pasa false
	public void setQuestionMarkIcon(boolean enabled)
	{
		this.setText("?");
		
		if (!enabled)
		{
			this.setBackgroundResource(R.drawable.square_grey);
			this.setTextColor(Color.RED);
		}
		else
		{
			this.setTextColor(Color.BLACK);
		}
	}

	// Establecer bloques como discapacitados / abrir si se pasa false
	// Else activar / cerrarlo
	public void setBlockAsDisabled(boolean enabled)
	{
		if (!enabled)
		{
			this.setBackgroundResource(R.drawable.square_grey);
		}
		else
		{
			this.setBackgroundResource(R.drawable.square_blue);
		}
	}

	// limpiar todos los icons/text
	public void clearAllIcons()
	{
		this.setText("");
	}

	// establecer la fuente en negrita
	private void setBoldFont()
	{
		this.setTypeface(null, Typeface.BOLD);
	}

	// descubrir este bloque
	public void OpenBlock()
	{
		// no puede descubrir una mina que no está cubierta
		if (!isCovered)
			return;

		setBlockAsDisabled(false);
		isCovered = false;

		// comprobar si tiene la mina
		if (hasMine())
		{
			setMineIcon(false);
		}
		// actualizar el recuento mina cercana
		else
		{
			setNumberOfSurroundingMines(numberOfMinesInSurrounding);
		}
	}

	// configurar el texto como en las inmediaciones recuento mina
	public void updateNumber(int text)
	{
		if (text != 0)
		{
			this.setText(Integer.toString(text));

			// Seleccionar un color diferente para cada número
			// Ya hemos saltado 0 conteo mina
			switch (text)
			{
				case 1:
					this.setTextColor(Color.BLUE);
					break;
				case 2:
					this.setTextColor(Color.rgb(0, 100, 0));
					break;
				case 3:
					this.setTextColor(Color.RED);
					break;
				case 4:
					this.setTextColor(Color.rgb(85, 26, 139));
					break;
				case 5:
					this.setTextColor(Color.rgb(139, 28, 98));
					break;
				case 6:
					this.setTextColor(Color.rgb(238, 173, 14));
					break;
				case 7:
					this.setTextColor(Color.rgb(47, 79, 79));
					break;
				case 8:
					this.setTextColor(Color.rgb(71, 71, 71));
					break;
				case 9: 
					this.setTextColor(Color.rgb(205, 205, 0));
					break;
			}
		}
	}

	// sistema del bloque como una mina debajo
	public void plantMine()
	{
		isMined = true;
	}

	// Mina fue abierta
	// Cambiar el icono de bloqueo y color
	public void triggerMine()
	{
		setMineIcon(true);
		this.setTextColor(Color.RED);
	}

	// se bloque todavía cubierta
	public boolean isCovered()
	{
		return isCovered;
	}

	// no el bloque tiene cualquier mina debajo
	public boolean hasMine()
	{
		return isMined;
	}

	// establecer el número de minas cercanas
	public void setNumberOfMinesInSurrounding(int number)
	{
		numberOfMinesInSurrounding = number;
	}

	// get number of nearby mines
	public int getNumberOfMinesInSorrounding()
	{
		return numberOfMinesInSurrounding;
	}

	// se bloque marcado como marcado
	public boolean isFlagged()
	{
		return isFlagged;
	}

	// marcar como bloque marcado
	public void setFlagged(boolean flagged)
	{
		isFlagged = flagged;
	}

	// se bloque marcado como un signo de interrogación
	public boolean isQuestionMarked()
	{
		return isQuestionMarked;
	}

	// establecer signo de interrogación para el bloque
	public void setQuestionMarked(boolean questionMarked)
	{
		isQuestionMarked = questionMarked;
	}

	// bloque puede recibir evento de clic
	public boolean isClickable()
	{
		return isClickable;
	}

	// desactivar el bloqueo para recibir eventos de clic
	public void setClickable(boolean clickable)
	{
		isClickable = clickable;
	}
}
