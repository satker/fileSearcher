package org.searcher.open_file;

import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.searcher.service.SearchFilesService;

public class GetTextFromFile {

  public static List<String> getTextFromFile(String filePath) throws IOException {
    List<String> result = new ArrayList<>();

    try {
      if (SearchFilesService.isFileTypeGood(filePath, "doc")) {
        getLinesCurrentFileDoc(filePath, result);
      }
      if (SearchFilesService.isFileTypeGood(filePath, "docx")) {
        getLinesCurrentFileDocX(filePath, result);
      } else {
        getLinesCurrentFileTxt(filePath, result);
      }
    } catch (IllegalArgumentException e) {
      getLinesCurrentFileRtf(filePath, result);
    }
    return result;
  }

  private static void getLinesCurrentFileRtf(String filePath, List<String> result) {
    //// Надо преобразовать формат ртф
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
    WordExtractor extractor = new WordExtractor(document);
    String[] fileData = extractor.getParagraphText();
    for (int i = 0; i < fileData.length; i++) {
      if (fileData[i] != null) {
        result.add(fileData[i]);
      }
    }
  }

  private static void getLinesCurrentFileTxt(String str, List<String> result) throws IOException {
    result.addAll(readAllLines(Paths.get(str), Charset.forName("ISO-8859-1")));
  }
}
