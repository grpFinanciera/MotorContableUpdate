package org.opentaps.warehouse.inventory;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.ofbiz.base.util.Debug;

/**
 * Esta clase provee la funcionalidad de convertir un numero representado en
 * digitos a una representacion en letras. Mejorado para leer centavos.
 * 
 *
 */
public abstract class UtilNumberToLetter {

    private static final String[] UNIDADES = { "", "UN ", "DOS ", "TRES ",
            "CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE ", "DIEZ ",
            "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS",
            "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE" };

    private static final String[] DECENAS = { "VENTI", "TREINTA ", "CUARENTA ",
            "CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA ",
            "CIEN " };

    private static final String[] CENTENAS = { "CIENTO ", "DOSCIENTOS ",
            "TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ", "SEISCIENTOS ",
            "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS " };

    /**
     * Convierte un numero en representacion numerica a uno en representacion de
     * texto. El numero es valido si esta entre 0 y 999'999.999
     * 
     * @param number
     *            Numero a convertir
     * @return Numero convertido a texto
     * @throws NumberFormatException
     *             Si el numero esta fuera del rango
     */
    public static String convertNumberToLetter(double doubleNumber)
            throws NumberFormatException {
    	
    	Debug.log("doubleNumberLlega: " + doubleNumber);

        StringBuilder converted = new StringBuilder();

        String patternThreeDecimalPoints = "#.000";

        DecimalFormat format = new DecimalFormat(patternThreeDecimalPoints);
        format.setRoundingMode(RoundingMode.DOWN);
        
        Debug.log("format: " + format);

        // formateamos el numero, para ajustarlo a el formato de tres puntos
        // decimales
        String formatedDouble = format.format(doubleNumber);
        Debug.log("formatedDouble: " + formatedDouble);
        doubleNumber = Double.parseDouble(formatedDouble);
        
        Debug.log("doubleNumber: " + doubleNumber);

        // Validamos que sea un numero legal
        if (doubleNumber > 999999999)
            throw new NumberFormatException(
                    "El numero es mayor de 999'999.999, "
                            + "no es posible convertirlo");

        if (doubleNumber < 0)
            throw new NumberFormatException("El numero debe ser positivo");

        String splitNumber[] = String.valueOf(doubleNumber).replace('.', '#')
                .split("#");
        Debug.log("splitNumber1: " + splitNumber);

        // Descompone el trio de millones
        int millon = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0],
                8))
                + String.valueOf(getDigitAt(splitNumber[0], 7))
                + String.valueOf(getDigitAt(splitNumber[0], 6)));
        if (millon == 1)
            converted.append("UN MILLON ");
        else if (millon > 1)
            converted.append(convertNumber(String.valueOf(millon))
                    + "MILLONES ");

        // Descompone el trio de miles
        int miles = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0],
                5))
                + String.valueOf(getDigitAt(splitNumber[0], 4))
                + String.valueOf(getDigitAt(splitNumber[0], 3)));
        if (miles == 1)
            converted.append("MIL ");
        else if (miles > 1)
            converted.append(convertNumber(String.valueOf(miles)) + "MIL ");

        // Descompone el ultimo trio de unidades
        int cientos = Integer.parseInt(String.valueOf(getDigitAt(
                splitNumber[0], 2))
                + String.valueOf(getDigitAt(splitNumber[0], 1))
                + String.valueOf(getDigitAt(splitNumber[0], 0)));
        if (cientos == 1)
            converted.append("UN");

        if (millon + miles + cientos == 0)
            converted.append("CERO");
        if (cientos > 1)
            converted.append(convertNumber(String.valueOf(cientos)));

        converted.append(" PESOS");

        // Descompone los centavos
        Debug.log("splitNumber: " + splitNumber[1]);        
        
        int centavos = Integer.parseInt(String.valueOf(getDigitAt(
                splitNumber[1], 2))
                + String.valueOf(getDigitAt(splitNumber[1], 1))
                + String.valueOf(getDigitAt(splitNumber[1], 0)));
        Debug.log("centavos: " + centavos);
        if(splitNumber[1].length()>=2)
        {	
        	converted.append(" " + splitNumber[1] + "/100");
        }
        else if(splitNumber[1].length()>=1)
        {	
        		converted.append(" " + splitNumber[1] + "0/100");
        }
//        if (centavos == 1)
//        {	Debug.log("If");
//            converted.append(" 01/100");
//        }
//        else if (centavos > 1)
//        	{Debug.log("If2");
//            converted.append(" " + String.valueOf(centavos) + "/100");
//        }
        else
        {	converted.append(" 00/100");
        }

        String convertedStr = converted.toString().toLowerCase();
        convertedStr = convertedStr.substring(0, 1).toUpperCase() + convertedStr.substring(1);
        return convertedStr;
    }

    /**
     * Convierte los trios de numeros que componen las unidades, las decenas y
     * las centenas del numero.
     * 
     * @param number
     *            Numero a convetir en digitos
     * @return Numero convertido en letras
     */
    private static String convertNumber(String number) {

        if (number.length() > 3)
            throw new NumberFormatException(
                    "La longitud maxima debe ser 3 digitos");

        // Caso especial con el 100
        if (number.equals("100")) {
            return "CIEN";
        }

        StringBuilder output = new StringBuilder();
        if (getDigitAt(number, 2) != 0)
            output.append(CENTENAS[getDigitAt(number, 2) - 1]);

        int k = Integer.parseInt(String.valueOf(getDigitAt(number, 1))
                + String.valueOf(getDigitAt(number, 0)));

        if (k <= 20)
            output.append(UNIDADES[k]);
        else if (k > 30 && getDigitAt(number, 0) != 0)
            output.append(DECENAS[getDigitAt(number, 1) - 2] + "Y "
                    + UNIDADES[getDigitAt(number, 0)]);
        else
            output.append(DECENAS[getDigitAt(number, 1) - 2]
                    + UNIDADES[getDigitAt(number, 0)]);

        return output.toString();
    }

    /**
     * Retorna el digito numerico en la posicion indicada de derecha a izquierda
     * 
     * @param origin
     *            Cadena en la cual se busca el digito
     * @param position
     *            Posicion de derecha a izquierda a retornar
     * @return Digito ubicado en la posicion indicada
     */
    private static int getDigitAt(String origin, int position) {
        if (origin.length() > position && position >= 0)
            return origin.charAt(origin.length() - position - 1) - 48;
        return 0;
    }

}