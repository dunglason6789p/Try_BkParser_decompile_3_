/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent.graph.dataformat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.concurrent.graph.ConcurrentGraphException;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.helper.URLFinder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataFormat {
    private final String name;
    private final ColumnDescription[] columns;
    private final HashMap<String, ColumnDescription> columnMap;

    public DataFormat(DataFormat dataFormat) {
        this.name = dataFormat.name;
        this.columns = new ColumnDescription[dataFormat.columns.length];
        this.columnMap = new HashMap();
        for (int i = 0; i < dataFormat.columns.length; ++i) {
            this.columns[i] = new ColumnDescription(dataFormat.columns[i]);
            this.columnMap.put(this.columns[i].getName(), this.columns[i]);
        }
    }

    public DataFormat(String name, ColumnDescription[] columns) {
        this.name = name;
        this.columns = new ColumnDescription[columns.length];
        this.columnMap = new HashMap();
        for (int i = 0; i < columns.length; ++i) {
            this.columns[i] = new ColumnDescription(columns[i]);
            this.columnMap.put(this.columns[i].getName(), this.columns[i]);
        }
    }

    public DataFormat(String name, ArrayList<ColumnDescription> columns) {
        this.name = name;
        this.columns = new ColumnDescription[columns.size()];
        this.columnMap = new HashMap();
        for (int i = 0; i < columns.size(); ++i) {
            this.columns[i] = new ColumnDescription(columns.get(i));
            this.columnMap.put(this.columns[i].getName(), this.columns[i]);
        }
    }

    public String getName() {
        return this.name;
    }

    public ColumnDescription getColumnDescription(int position) {
        return this.columns[position];
    }

    public ColumnDescription getColumnDescription(String columnName) {
        ColumnDescription columnDescription = this.columnMap.get(columnName);
        if (columnDescription != null) {
            return columnDescription;
        }
        for (int i = 0; i < this.columns.length; ++i) {
            if (!this.columns[i].getName().equals(columnName.toUpperCase())) continue;
            this.columnMap.put(columnName, this.columns[i]);
            return this.columns[i];
        }
        return null;
    }

    public SortedSet<ColumnDescription> getSelectedColumnDescriptions(Set<Integer> positionSet) {
        SortedSet<ColumnDescription> selectedColumns = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = 0; i < this.columns.length; ++i) {
            if (!positionSet.contains(this.columns[i].getPosition())) continue;
            selectedColumns.add(this.columns[i]);
        }
        return selectedColumns;
    }

    public Set<String> getLabelNames() {
        Set<String> labelNames = Collections.synchronizedSet(new HashSet());
        for (int i = 0; i < this.columns.length; ++i) {
            labelNames.add(this.columns[i].getName());
        }
        return labelNames;
    }

    public int numberOfColumns() {
        return this.columns.length;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.columns);
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        DataFormat other = (DataFormat)obj;
        if (!Arrays.equals(this.columns, other.columns)) {
            return false;
        }
        return !(this.name == null ? other.name != null : !this.name.equals(other.name));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append('\n');
        for (int i = 1; i < this.columns.length; ++i) {
            sb.append(this.columns[i]);
            sb.append('\n');
        }
        return sb.toString();
    }

    public static DataFormat parseDataFormatXMLfile(String fileName) throws MaltChainedException {
        return DataFormat.parseDataFormatXMLfile(new URLFinder().findURL(fileName));
    }

    public static DataFormat parseDataFormatXMLfile(URL url) throws ConcurrentGraphException {
        String dataFormatName;
        if (url == null) {
            throw new ConcurrentGraphException("The data format specification file cannot be found. ");
        }
        ArrayList<ColumnDescription> columns = new ArrayList<ColumnDescription>();
        try {
            int i;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element root = db.parse(url.openStream()).getDocumentElement();
            if (!root.getNodeName().equals("dataformat")) {
                throw new ConcurrentGraphException("Data format specification file must contain one 'dataformat' element. ");
            }
            dataFormatName = root.getAttribute("name");
            NodeList cols = root.getElementsByTagName("column");
            Element col = null;
            for (i = 0; i < cols.getLength(); ++i) {
                col = (Element)cols.item(i);
                ColumnDescription column = new ColumnDescription(i, col.getAttribute("name"), ColumnDescription.getCategory(col.getAttribute("category")), ColumnDescription.getType(col.getAttribute("type")), col.getAttribute("default"), false);
                columns.add(column);
            }
            columns.add(new ColumnDescription(i++, "PPPATH", 3, 1, "_", true));
            columns.add(new ColumnDescription(i++, "PPLIFTED", 3, 1, "_", true));
            columns.add(new ColumnDescription(i++, "PPCOVERED", 3, 1, "_", true));
        }
        catch (IOException e) {
            throw new ConcurrentGraphException("Cannot find the file " + url.toString() + ". ", e);
        }
        catch (ParserConfigurationException e) {
            throw new ConcurrentGraphException("Problem parsing the file " + url.toString() + ". ", e);
        }
        catch (SAXException e) {
            throw new ConcurrentGraphException("Problem parsing the file " + url.toString() + ". ", e);
        }
        return new DataFormat(dataFormatName, columns);
    }
}
