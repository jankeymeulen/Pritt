/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pritt;

/**
 *
 * @author theo1038
 */
public class QA implements Comparable {
    
    private final int number;
    private final int qrow;
    private int arow;
    private int erow;
    
    public QA (int number, int row)
    {
        this.qrow = row;
        this.arow = -1;
        this.number = number;
    }
    
    public QA (int number, int qrow, int arow, int erow)
    {
        this.qrow = qrow;
        this.arow = arow;
        this.erow = erow;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public int getRow() {
        return qrow;
    }
    
    public int getQRow() {
        return qrow;
    }
    
    public int getARow() {
        return arow;
    }
    
    public int getERow() {
        return erow;
    }

    public void setARow(int a) {
        arow = a;
    }
    
    public void setERow(int e) {
        erow = e;
    }
    
    @Override
    public int compareTo(Object o) {
        return number - ((QA)o).number;
    }
    
    @Override
    public String toString()
    {
        return "QA "+number+" question on row "+qrow+", answer on row "+arow+", end on row "+erow;
    }
    
}
