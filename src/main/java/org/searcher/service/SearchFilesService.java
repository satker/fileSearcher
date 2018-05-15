package org.searcher.service;
/*
     Модуль поиска (мозг программы)
*/

import org.searcher.fxml_manager.MainWindowController;
import org.searcher.model.SearchFilesModel;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class SearchFilesService {

    public static volatile boolean searchIsAlive = false;
    private String directory; // Искомая директория
    private String findText; // Искомый текст
    private String findType; // Искомое расширение
    private String findNameFile;
    private int id = 1; // ID результата
    private String chooseSearchType;
    private Map<String, int[]> searchedFiles = new HashMap<>();
    private String[] textTypes = ("1ST ABW ACL AFP AMI ANS ASC AWW CCF CSV CWK DBK DITA DOC" +
            " DOCM DOCX DOT DOTX EGT EPUB EZW FDX FTM FTX GDOC HTML HWP HWPML LOG LWP" +
            " MBP MD ME MCW Mobi NB NBP NEIS ODM ODOC ODT OSHEET OTT OMM PAGES PAP PDAX" +
            " PDF QUOX Radix-64RTF RPT SDW SE STW Sxw TeX INFO Troff TXT UOF UOML VIA" +
            " WPD WPS WPT WRD WRF WRI XHTML XML XPS").split(" ");

    public Map<String, int[]> getSearchedFiles() {
        return searchedFiles;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setFindText(String findText) {
        this.findText = findText;
    }

    public void setFindNameFile(String findNameFile) {
        this.findNameFile = findNameFile;
    }

    public void setFindType(String findType) {
        this.findType = findType;
    }

    public void setChooseSearchType(String chooseSearchType) {
        this.chooseSearchType = chooseSearchType;
    }

    public void startSearching() {
        List<String> result = currentDirectories(directory);
        List<String> readyForAddingToResult = new ArrayList<>();
        searchIsAlive = true;
        while (!result.isEmpty()) {
            for (String currentDirectory : result) {
                if (currentDirectory != null) {
                    readyForAddingToResult.addAll(currentDirectories(currentDirectory));
                }
            }
            result.clear();
            result.addAll(readyForAddingToResult);
            readyForAddingToResult.clear();
        }
        searchIsAlive = false;
    }

    // Возвращает список директорий в папке
    private List<String> currentDirectories(String path) {
        List<String> result = new ArrayList<>();
        // Список файлов текущей директории
        String[] currentFiles = new File(path).list();
        if (currentFiles != null) {
            Arrays.stream(currentFiles)
                    .map(fileOrDirectoryName ->
                            getFileFromFullNameFileOrDirectory(path, fileOrDirectoryName))
                    .forEach(currentFile ->
                            isaRequiredFileOrDirectory(result, currentFile));
        }
        return result;
    }

    private void isaRequiredFileOrDirectory(List<String> result, File currentFile) {
        if (currentFile.isFile()) {
            String fileForAdd = getCorrectFile(currentFile);
            if (fileForAdd != null) {
                MainWindowController.resultFiles.add(new SearchFilesModel(id, fileForAdd));
                id++;
            }
        } else {
            result.add(currentFile.getAbsolutePath());
        }
    }

    private File getFileFromFullNameFileOrDirectory(String fileOrDirectoryPath,
                                                    String fileOrDirectoryName) {
        String fullNameFileOrDirectory = fileOrDirectoryPath + "\\" + fileOrDirectoryName;
        return new File(fullNameFileOrDirectory);
    }

    private String getCorrectFile(File file) {
        String resultFullName = null;
        String fullNameFile = file.getAbsolutePath();
        if (isaGoodFileAtAll(file)) {
            resultFullName = fullNameFile;
        }
        return resultFullName;
    }

    private boolean isaGoodFileAtAll(File file) {
        return file.canRead() && (findType.equals("") || isFileTypeGood(file, findType)) &&
                (findText.equals("") || isFileContainCurrentText(file)) && (findNameFile.equals("")
                || isFileNameGood(file.getName()));
    }

    private boolean isFileNameGood(String nameFile) {
        String nameWithoutType = nameFile.split("\\.(?=[^\\.]+$)")[0];
        return nameWithoutType.equals(findNameFile);
    }

    // Проверка расширения
    private boolean isFileTypeGood(File file, String fileType) {
        return Pattern.compile(".+\\." + fileType + "$")
                .matcher(file.getName())
                .matches();
    }

    // Проверка наличия искомого текста в файле
    private boolean isFileContainCurrentText(File file) {
        if (Arrays.stream(textTypes).anyMatch(type -> isFileTypeGood(file, type))) {
            boolean isFuzzy = chooseSearchType.equals("Fuzzy search");
            try {
                int[] findIndexesWithSearchedSrtings = LuceneSearcherService.isaTextInFile(file, findText,
                        isFuzzy);
                searchedFiles.put(file.getAbsolutePath(), findIndexesWithSearchedSrtings);
                return findIndexesWithSearchedSrtings.length != 0;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}