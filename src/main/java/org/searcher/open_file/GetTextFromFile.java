package org.searcher.open_file;

import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.searcher.service.SearchFilesService;

public class GetTextFromFile {

  private GetTextFromFile() {
    throw new IllegalStateException("Utility class");
  }

  public static List<String> getTextFromFile(String filePath) throws IOException {
    List<String> result = new ArrayList<>();
    try {
      if (SearchFilesService.isFileTypeGood(filePath, "doc")) {
        getLinesCurrentFileDoc(filePath, result);
      } else if (SearchFilesService.isFileTypeGood(filePath, "docx")) {
        getLinesCurrentFileDocX(filePath, result);
      } else if (SearchFilesService.isFileTypeGood(filePath, "rtf")) {
        getLinesCurrentFileRtf(filePath, result);
      } else if (SearchFilesService.isFileTypeGood(filePath, "pdf")) {
        getLinesCurrentFilePdf(filePath, result);
      } else {
        getLinesCurrentFileTxt(filePath, result);
      }
    } catch (IllegalArgumentException e) {
      // выбрасывает IllegalArgumentException если rtf замаскирован под doc, поэтому открываем как rtf
      getLinesCurrentFileRtf(filePath, result);
    } catch (Exception e) {
      return result;
    }
    return result;
  }

  private static void getLinesCurrentFilePdf(String filePath, List<String> result)
      throws IOException {
    try (PDDocument document = PDDocument.load(new File(filePath))) {
      document.getClass();
      if (!document.isEncrypted()) {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        PDFTextStripper tStripper = new PDFTextStripper();
        String pdfFileInText = tStripper.getText(document);
        // split by whitespace
        String[] lines = pdfFileInText.split("\\r?\\n");
        result.addAll(Arrays.asList(lines));
      }
    }
  }

  private static void getLinesCurrentFileRtf(String filePath, List<String> result)
      throws IOException {
    FileInputStream stream = new FileInputStream(filePath);
    RTFEditorKit kit = new RTFEditorKit();
    Document doc = kit.createDefaultDocument();
    try {
      kit.read(stream, doc, 0);
      result.add(doc.getText(0, doc.getLength()));
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  private static void getLinesCurrentFileDocX(String filePath, List<String> result)
      throws IOException {
    File file = new File(filePath);
    try (FileInputStream fis = new FileInputStream(file.getAbsolutePath())) {
      XWPFDocument document = new XWPFDocument(fis);
      List<XWPFParagraph> paragraphs = document.getParagraphs();
      for (XWPFParagraph para : paragraphs) {
        result.add(para.getText());
      }
    }
  }

  private static void getLinesCurrentFileDoc(String str, List<String> result) throws IOException {
    File file = new File(str);
    FileInputStream fis = new FileInputStream(file.getAbsolutePath());
    HWPFDocument document = new HWPFDocument(fis);
    try (WordExtractor extractor = new WordExtractor(document)) {
      String[] fileData = extractor.getParagraphText();
      for (String aFileData : fileData) {
        if (aFileData != null) {
          result.add(aFileData);
        }
      }
    }
  }

  private static void getLinesCurrentFileTxt(String str, List<String> result) throws IOException {
    result.addAll(readAllLines(Paths.get(str), Charset.forName("UTF-8")));
  }
}
