package de.micromata.merlin.excel;

import de.micromata.merlin.CoreI18n;
import de.micromata.merlin.persistency.PersistencyRegistry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Wraps and enhances a POI workbook.
 */
public class ExcelWorkbook implements AutoCloseable {
    private static Logger log = LoggerFactory.getLogger(ExcelWorkbook.class);

    private Workbook workbook;
    private List<ExcelSheet> sheetList;
    private Map<String, CellStyle> cellStyleMap = new HashMap<>();
    private Map<String, Font> fontMap = new HashMap<>();
    private InputStream inputStream;

    public static ExcelWorkbook create(Path path) {
        InputStream inputStream = PersistencyRegistry.getDefault().getInputStream(path);
        if (inputStream == null) {
            log.error("Cam't get input stream for path: " + path.toAbsolutePath());
            return null;
        }
        String filename = path.getFileName().toString();
        return new ExcelWorkbook(inputStream, filename);
    }


    public ExcelWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public ExcelWorkbook(String excelFilename) {
        this(new File(excelFilename));
    }

    public ExcelWorkbook(File excelFile) {
        try {
            FileInputStream fis = new FileInputStream(excelFile);
            open(fis, excelFile.getName());
        } catch (IOException ex) {
            log.error("Couldn't open File '" + excelFile.getAbsolutePath() + "': " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param inputStream
     * @param filename    Only for logging purposes if any error occurs.
     */
    public ExcelWorkbook(InputStream inputStream, String filename) {
        open(inputStream, filename);
    }

    private void open(InputStream inputStream, String filename) {
        this.inputStream = inputStream;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException ex) {
            log.error("Couldn't open File '" + filename + "' from InputStream: " + ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public ExcelSheet getSheet(String sheetName) {
        initializeSheetList();
        if (sheetName == null) {
            log.error("Can't get sheet by name without given name. Name parameter is null.");
            return null;
        }
        for (ExcelSheet sheet : sheetList) {
            if (sheetName.equals(sheet.getSheetName())) {
                return sheet;
            }
        }
        return null;
    }

    /**
     * A sheet name might be localized, such as "Configuration" and "Konfiguration". Try to get the first sheet matching one
     * of the given sheetNames.
     * @param i18nKey of the sheet title
     * @return
     */
    public ExcelSheet getSheetByLocalizedNames(String i18nKey) {
        return getSheetByLocalizedNames(CoreI18n.getAllTranslations(i18nKey));
    }

    /**
     * A sheet name might be localized, such as "Configuration" and "Konfiguration". Try to get the first sheet matching one
     * of the given sheetNames.
     * @param sheetNames
     * @return
     */
    public ExcelSheet getSheetByLocalizedNames(Set<String> sheetNames) {
        if (CollectionUtils.isEmpty(sheetNames)) {
            return null;
        }
        ExcelSheet sheet;
        for (String sheetName : sheetNames) {
            sheet = getSheet(sheetName);
            if (sheet != null) {
                return sheet;
            }
        }
        return null;
    }

    public ExcelSheet createOrGetSheet(String sheetName) {
        initializeSheetList();
        if (sheetName == null) {
            log.error("Can't get sheet by name without given name. Name parameter is null.");
            return null;
        }
        ExcelSheet sheet = getSheet(sheetName);
        if (sheet != null) {
            return sheet;
        }
        sheet = new ExcelSheet(this, getPOIWorkbook().createSheet(sheetName));
        sheet.setModified(true);
        sheetList = null;
        return sheet;
    }

    public Workbook getPOIWorkbook() {
        return workbook;
    }

    private void initializeSheetList() {
        if (sheetList != null) {
            return; // Already initialized.
        }
        sheetList = new ArrayList<>();
        for (Sheet poiSheet : workbook) {
            ExcelSheet excelSheet = new ExcelSheet(this, poiSheet);
            sheetList.add(excelSheet);
        }
    }

    /**
     * @return true if any sheet of this workbook returns true: {@link ExcelSheet#isModified()}
     */
    public boolean isModified() {
        initializeSheetList();
        for (ExcelSheet sheet : sheetList) {
            if (sheet.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Please re-use cell styles due to limitations of Excel.
     *
     * @param id
     * @return
     */
    public CellStyle createOrGetCellStyle(String id) {
        CellStyle cellStyle = cellStyleMap.get(id);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            cellStyleMap.put(id, cellStyle);
        }
        return cellStyle;
    }

    /**
     * Please re-use cell styles due to limitations of Excel.
     *
     * @param id
     * @return
     */
    public Font createOrGetFont(String id) {
        Font font = fontMap.get(id);
        if (font == null) {
            font = workbook.createFont();
            fontMap.put(id, font);
        }
        return font;
    }

    public ByteArrayOutputStream getAsByteArrayOutputStream() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            workbook.write(bos);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return bos;
    }


    @Override
    public void close() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            workbook.close();
        } catch (final IOException ioe) {
            // ignore
        }
    }
}
