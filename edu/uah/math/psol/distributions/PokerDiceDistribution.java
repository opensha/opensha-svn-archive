package edu.uah.math.psol.distributions;

/**
 *  Description of the Class
 *
 *  
 *  
 */
public class PokerDiceDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    final static int c = 7776;

    /**
     *  Constructor for the PokerDiceDistribution object
     */
    public PokerDiceDistribution() {
        setParameters( 0, 6, 1, DISCRETE );
    }

    /**
     *  Gets the density attribute of the PokerDiceDistribution object
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        double d = 0;
        int i = ( int ) x;
        switch ( i ) {
            case 0:
                d = 720.0 / c;
                break;
            case 1:
                d = 3600.0 / c;
                break;
            case 2:
                d = 1800.0 / c;
                break;
            case 3:
                d = 1200.0 / c;
                break;
            case 4:
                d = 300.0 / c;
                break;
            case 5:
                d = 150.0 / c;
                break;
            case 6:
                d = 6.0 / c;
                break;
        }
        return d;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String name() {
        return "Poker Dice Distribution";
    }
}

