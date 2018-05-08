/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pritt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author theo1038
 */
public class Pritt {

    private JTextArea output;

    public Pritt(JTextArea output) {
        this.output = output;
    }

    public Pritt() {
        this.output = null;
    }

    private void print(String s) {
        if (output != null) {
            output.append(s);
        } else {
            System.err.print(s);
        }
    }

    private void println(String s) {
        print(s + "\n");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            // GUI
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new MainFrame().setVisible(true);
                }
            });
        } else {
            if (args.length == 3) {
                switch (args[0]) {
                    case "-t":
                        {
                            Pritt p = new Pritt();
                            p.createTranscription(new File(args[1]), new File(args[2]));
                            break;
                        }
                    case "-r":
                        {
                            Pritt p = new Pritt();
                            p.createRoelli(new File(args[1]), new File(args[2]));
                            break;
                        }
                    case "-m":
                        {
                            Pritt p = new Pritt();
                            p.createMatrix(new File(args[1]), new File(args[2]));
                            break;
                        }
                    case "-b":
                        {
                            Pritt p = new Pritt();
                            p.createBarbara(new File(args[1]), new File(args[2]));
                            break;
                        }
                }
            } else {
                System.err.println("Usage: java -jar Pritt.jar [ -t | -m | -r ] inFile outFile");
            }
        }
    }

    public void createTranscription(File infile, File outfile) {
        TreeSet<Witness> ws = new TreeSet();

        try {
            ws = readCollation(infile);
        } catch (IOException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        }

        Witness pg = ws.first();

        for (Witness w : ws) {
            w.createTranscription(pg);
        }
        writeTranscription(ws, outfile);
    }

    public void writeMatrix(String[][] matrix, File outFile) {
        try {
            FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (int i = 0; i < matrix.length; i++) {
                bw.write(matrix[i][0]);
                for (int j = 1; j < matrix[i].length; j++) {
                    bw.write("," + matrix[i][j]);
                }
                bw.newLine();
            }

        } catch (IOException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeTranscription(TreeSet<Witness> ws, File outFile) {
        FileOutputStream fileOut = null;
        try {
            Workbook t = new XSSFWorkbook();
            Sheet sheet = t.createSheet("Tradition");

            CellStyle headerStyle = t.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            Font headerFont = t.createFont();
            headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            CellStyle referenceStyle = t.createCellStyle();
            referenceStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            referenceStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            Font referenceFont = t.createFont();
            referenceFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            referenceFont.setColor(IndexedColors.BLACK.getIndex());
            referenceStyle.setFont(referenceFont);

            Row row = sheet.createRow(0);
            int i = 0;
            for (Witness w : ws) {
                Cell cell = row.createCell(i, Cell.CELL_TYPE_STRING);
                cell.setCellValue(w.getName());
                cell.setCellStyle(headerStyle);
                i++;
            }

            print("Writing transcription... ");

            for (i = 0; i < ws.first().getTranscriptionText().length; i++) {
                if (i % (ws.first().getTranscriptionText().length / 10) == 0) {
                    print(((i / (ws.first().getTranscriptionText().length / 10)) * 10) + "% ");
                }

                row = sheet.createRow(i + 1);
                int j = 0;
                for (Witness w : ws) {
                    Cell cell = row.createCell(j, Cell.CELL_TYPE_STRING);
                    cell.setCellValue(w.getTranscription(i));
                    if (j == 0) {
                        cell.setCellStyle(referenceStyle);
                    }
                    j++;
                }
            }
            fileOut = new FileOutputStream(outFile);
            t.write(fileOut);
            fileOut.close();

            println("Done!");

        } catch (Exception ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOut.close();
            } catch (IOException ex) {
                Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public TreeSet<Witness> readCollation(File inFile) throws IOException {
        TreeSet<Witness> witnesses = new TreeSet();
        TreeMap<Integer, QA> qas = new TreeMap();

        int curR = 0, curC = 0;

        try {

            println("Reading collation... ");

            //Workbook c = WorkbookFactory.create(new FileInputStream(inFile));
            Workbook c = WorkbookFactory.create(inFile);

            Sheet s = c.getSheetAt(0);

            int i = 0;

            QA prevQA = new QA(0, 0);

            for (i = 2; i < s.getLastRowNum(); i++) {
                Row r = s.getRow(i);

                if (r != null) {
                    Cell curCell = r.getCell(0);

                    if (curCell != null && curCell.getCellType() == Cell.CELL_TYPE_STRING) {

                        if (curCell.getStringCellValue().equals("question")) {
                            Cell qaidCell = r.getCell(1);
                            int qaid = (int) Math.round(qaidCell.getNumericCellValue());
                            prevQA.setERow(i - 3);
                            prevQA = new QA(qaid, i - 2);
                            qas.put(qaid, prevQA);
                        }
                        if (curCell.getStringCellValue().equals("answer")) {
                            prevQA.setARow(i - 2);
                        }
                    }
                }
            }
            prevQA.setERow(s.getLastRowNum()-2);

            s = c.getSheetAt(0);
            Row a = s.getRow(0);

            i = 0;

            for (Cell cell : a) {
                Witness w = null;
                if (cell != null && !cell.getStringCellValue().equals("")) {
                    if (a.getCell(i + 2) != null && a.getCell(i + 2).getStringCellValue().equals("")) // 3 cols
                    {
                        w = new Witness(cell.getStringCellValue(), i + 1);
                    } else // 2 cols
                    {
                        w = new Witness(cell.getStringCellValue(), i);
                    }

                    if (cell.getColumnIndex() < 3 && cell.getStringCellValue().contains("PG")) {
                        w = new Witness("PG", 2, true, qas);
                    }
                    witnesses.add(w);
                    System.out.println(w);
                }

                i++;
            }

            for (i = 2; i <= s.getLastRowNum(); i++) {
                curR = i;

                if (i % (s.getLastRowNum() / 10) == 0) {
                    print(((i / (s.getLastRowNum() / 10)) * 10) + "% ");
                }

                Row r = s.getRow(i);

                if (r != null) {
                    for (Witness w : witnesses) {
                        curC = w.getColumnIndex();
                        if (r.getCell(w.getColumnIndex()) == null) {
                            // Some empty cells are null, treat as regular empty
                            w.addCollation("");
                        } else {
                            Cell curCell = r.getCell(w.getColumnIndex());

                            if (curCell.getCellType() == Cell.CELL_TYPE_STRING) {
                                w.addCollation(curCell.getStringCellValue());
                            } else if (curCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                w.addCollation("");
                            } else {
                                w.addCollation("ERROR READING");
                            }

                        }
                    }
                } else {
                    // Empty rows with residual formatting
                }

            }

            println("Done!");

        } catch (InvalidFormatException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        }

        return witnesses;
    }

    void createMatrix(File source, File destination) {
        TreeSet<Witness> ws = new TreeSet();

        try {
            ws = readCollation(source);
        } catch (IOException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        }

        Witness ref = ws.first();

        for (Witness w : ws) {
            w.createTranscription(ref);
        }

        String[][] m = new String[ws.size()][ref.getTranscriptionText().length + 1];

        int wC = 0;
        for (Witness w : ws) {
            m[wC][0] = '"' + w.getName() + '"';
            wC++;
        }

        for (int i = 1; i <= ref.getTranscriptionText().length; i++) {
            Iterator<Witness> it = ws.iterator();
            it.next(); // first one is reference
            m[0][i] = "A";
            int j = 1;
            char nextSymbol = 'B';
            HashMap<String, String> variants = new HashMap();

            while (it.hasNext()) {
                String s = it.next().getNormalizedTranscription(i - 1);

                if (s.equals("?") || s.equals("_")) {
                    m[j][i] = s;
                } else if (ref.getNormalizedTranscription(i - 1).equals(s)) {
                    m[j][i] = "A";
                } else {
                    if (variants.containsKey(s)) {
                        m[j][i] = variants.get(s);
                    } else {
                        m[j][i] = Character.toString(nextSymbol);
                        variants.put(s, Character.toString(nextSymbol));
                        nextSymbol++;
                    }
                }
                j++;
            }
        }

        writeMatrix(m, destination);
    }

    public void createRoelli(File infile, File outfile) {
        TreeSet<Witness> ws = new TreeSet();

        try {
            ws = readCollation(infile);
        } catch (IOException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        }

        Witness pg = ws.first();

        for (Witness w : ws) {
            w.createTranscription(pg);
        }

        writeRoelli(ws, outfile);
    }
    


    private StringBuilder createRoelliQA(TreeSet<Witness> ws, QA qa)
    {
        StringBuilder buf = new StringBuilder();
        for (Witness w : ws) {
            buf.append(w.getRoelliReference()).append("|");
            for(String s : w.getTranscriptionQA(qa))
            {
                buf.append(w.getNormalizedString(s));
                if(!w.getNormalizedString(s).equals(""))
                    buf.append(" ");
            }
            buf.append("\n");
        }
        return buf;
    }
    
    private void writeRoelli(TreeSet<Witness> ws, File path) {
        for(QA qa : ws.first().getQas().values())
        {
            System.out.println(qa);
            String fileName = "Question_" + qa.getNumber() + ".txt";
            IO.writeTextFile(createRoelliQA(ws,qa),path,fileName);
        }
    }

    public void createBarbara(File infile, File outfile) {

        for (File singleFile : infile.listFiles()) {
            if (singleFile.isFile() && singleFile.getName().endsWith(".xlsx")) {
                readSingleBarbara(singleFile, outfile);
            }
        }
    }

    public void readSingleBarbara(File infile, File outfile) {
        TreeSet<Witness> ws = new TreeSet();

        try {
            ws = readCollation(infile);
        } catch (IOException ex) {
            Logger.getLogger(Pritt.class.getName()).log(Level.SEVERE, null, ex);
        }

        Witness pg = ws.first();

        for (Witness w : ws) {
            w.createTranscription(pg);
        }

        writeBarbara(ws, outfile);
    }
    
    private void writeBarbara(TreeSet<Witness> ws, File path) {
            
        for (Witness w : ws) {
            for(QA qa : ws.first().getQas().values())
            {
                String name = java.net.URLEncoder.encode(w.getName())+"_QA"+qa.getNumber()+".xml";
                
                File outFile = new File(path.getAbsolutePath() + File.separator +  name);
                IO.writeSingleQAXML(w, qa, outFile);
            }
        }
    }
}
