/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pritt;

import java.util.List;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author theo1038
 */
public class Witness implements Comparable {

    private String name;
    private int textCol;
    private ArrayList<String> collation;
    private ArrayList<String> transcription;
    private TreeMap<Integer, QA> qas;
    private boolean isReference;

    private String excelColumnFromNumber(int column) {
        String columnString = "";
        int columnNumber = column + 1;
        while (columnNumber > 0) {
            int currentLetterNumber = (columnNumber - 1) % 26;
            char currentLetter = (char) (currentLetterNumber + 65);
            columnString = currentLetter + columnString;
            columnNumber = (columnNumber - (currentLetterNumber + 1)) / 26;
        }
        return columnString;
    }

    public Witness(String name, int textCol) {
        this.name = name;
        this.textCol = textCol;
        this.collation = new ArrayList();
        this.transcription = new ArrayList();
        this.isReference = false;
    }

    public Witness(String name, int textCol, boolean isReference) {
        this(name, textCol);
        this.isReference = isReference;
    }

    public Witness(String name, int textCol, boolean isReference, TreeMap<Integer, QA> qas) {
        this(name, textCol, isReference);
        this.qas = qas;
    }

    @Override
    public String toString() {
        return name + " [" + textCol + "][" + excelColumnFromNumber(textCol) + "]";
    }

    @Override
    public int compareTo(Object o) {
        if (isReference) {
            return Integer.MIN_VALUE;
        }
        return textCol - ((Witness) o).textCol;
    }

    public void addCollation(String s) {
        collation.add(s);
    }

    public int getColumnIndex() {
        return textCol;
    }

    public String getCollation(int i) {
        return collation.get(i);
    }

    public String getTranscription(int i) {
        return transcription.get(i);
    }

    public String[] getCollationQA(QA qa) {
        List<String> subList = collation.subList(qa.getQRow(), qa.getERow() + 1);
        return subList.toArray(new String[subList.size()]);
    }

    public String[] getCollationQ(QA qa) {
        List<String> subList = collation.subList(qa.getQRow(), qa.getARow());
        return subList.toArray(new String[subList.size()]);
    }

    public String[] getCollationA(QA qa) {
        List<String> subList = collation.subList(qa.getARow(), qa.getERow() + 1);
        return subList.toArray(new String[subList.size()]);
    }

    public String[] getTranscriptionQA(QA qa) {
        List<String> subList = transcription.subList(qa.getQRow(), qa.getERow() + 1);
        return subList.toArray(new String[subList.size()]);
    }

    public String[] getTranscriptionQ(QA qa) {
        List<String> subList = transcription.subList(qa.getQRow(), qa.getARow());
        return subList.toArray(new String[subList.size()]);
    }

    public String[] getTranscriptionA(QA qa) {
        List<String> subList = transcription.subList(qa.getARow(), qa.getERow() + 1);
        return subList.toArray(new String[subList.size()]);
    }

    public String getNormalizedTranscription(int i) {
        String s = transcription.get(i);
        return getNormalizedString(s);
    }

    private String StringArrayToSentence(String[] sA) {
        StringBuilder b = new StringBuilder();
        for (String s : sA) {
            if (!s.equals("_") && !s.equals("")) {
                b.append(s).append(" ");
            }
        }
        return b.substring(0, (b.length()-1)>0?b.length()-1:0).replaceAll("\\p{Cntrl}", "");
    }

    public String getTranscriptionTextQA(QA qa) {
        return StringArrayToSentence(getTranscriptionQA(qa));
    }

    public String getTranscriptionTextQ(QA qa) {
        return StringArrayToSentence(getTranscriptionQ(qa));
    }

    public String getTranscriptionTextA(QA qa) {
        return StringArrayToSentence(getTranscriptionA(qa));
    }

    public String getNormalizedString(String s) {
        if (s.equals("?")) {
            return s;
        }
        if (s.equals("_") || s.equals("om"))
        {
            return "";
        }
        
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        StringBuilder w = new StringBuilder();

        for (Character c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                w.append(c);
            }
        }
        return w.toString().toLowerCase();
    }

    public String[] getCollationText() {
        String[] r = collation.toArray(new String[0]);
        return r;
    }

    public String[] getTranscriptionText() {
        if (transcription.isEmpty()) {
            return null;
        } else {
            String[] r = transcription.toArray(new String[0]);
            return r;
        }
    }

    public void createTranscription(Witness pg) {
        if (isReference) {
            transcription.clear();
            transcription.addAll(collation);
        } else {
            for (int i = 0; i < collation.size(); i++) {
                if (getCollation(i).equals("")) {
                    transcription.add(i, pg.getCollation(i));
                } else {
                    transcription.add(i, getCollation(i));
                }
            }
        }
        cleanupTranscription();
    }

    private void cleanupTranscription() {
        for (int i = 0; i < transcription.size(); i++) {
            if (this.isReference) {
                if (transcription.get(i).equals("om") || transcription.get(i).equals("_") || transcription.get(i).equals("?")) {
                    transcription.set(i, "");
                }
            } else {
                if (transcription.get(i).equals("om")) {
                    transcription.set(i, "");
                }
            }
        }
    }

    public boolean isReference() {
        return isReference;
    }

    public String getName() {
        return name;
    }

    public String getRoelliReference() {
        return String.format("%1$-" + 12 + "s", name.substring(name.trim().lastIndexOf(" ") + 1, name.trim().length()));
    }

    public TreeMap<Integer, QA> getQas() {
        return qas;
    }

    public int getSize() {
        return transcription.size();
    }
}
