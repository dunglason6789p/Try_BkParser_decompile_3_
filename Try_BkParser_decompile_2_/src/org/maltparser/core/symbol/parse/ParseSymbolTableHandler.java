/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.parse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.parse.ParseSymbolTable;

public class ParseSymbolTableHandler
implements SymbolTableHandler {
    private final SymbolTableHandler parentSymbolTableHandler;
    private final HashMap<String, ParseSymbolTable> symbolTables;

    public ParseSymbolTableHandler(SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
        this.parentSymbolTableHandler = parentSymbolTableHandler;
        this.symbolTables = new HashMap();
        for (String tableName : parentSymbolTableHandler.getSymbolTableNames()) {
            this.addSymbolTable(tableName);
        }
    }

    @Override
    public SymbolTable addSymbolTable(String tableName) throws MaltChainedException {
        ParseSymbolTable symbolTable = this.symbolTables.get(tableName);
        if (symbolTable == null) {
            symbolTable = new ParseSymbolTable(tableName, this.parentSymbolTableHandler);
            this.symbolTables.put(tableName, symbolTable);
        }
        return symbolTable;
    }

    @Override
    public SymbolTable addSymbolTable(String tableName, SymbolTable parentTable) throws MaltChainedException {
        ParseSymbolTable symbolTable = this.symbolTables.get(tableName);
        if (symbolTable == null) {
            symbolTable = new ParseSymbolTable(tableName, parentTable, this.parentSymbolTableHandler);
            this.symbolTables.put(tableName, symbolTable);
        }
        return symbolTable;
    }

    @Override
    public SymbolTable addSymbolTable(String tableName, int columnCategory, int columnType, String nullValueStrategy) throws MaltChainedException {
        ParseSymbolTable symbolTable = this.symbolTables.get(tableName);
        if (symbolTable == null) {
            symbolTable = new ParseSymbolTable(tableName, columnCategory, columnType, nullValueStrategy, this.parentSymbolTableHandler);
            this.symbolTables.put(tableName, symbolTable);
        }
        return symbolTable;
    }

    @Override
    public SymbolTable getSymbolTable(String tableName) {
        return this.symbolTables.get(tableName);
    }

    @Override
    public Set<String> getSymbolTableNames() {
        return this.symbolTables.keySet();
    }

    @Override
    public void cleanUp() {
        for (ParseSymbolTable table : this.symbolTables.values()) {
            table.clearTmpStorage();
        }
    }

    @Override
    public void save(OutputStreamWriter osw) throws MaltChainedException {
        this.parentSymbolTableHandler.save(osw);
    }

    @Override
    public void save(String fileName, String charSet) throws MaltChainedException {
        this.parentSymbolTableHandler.save(fileName, charSet);
    }

    public void loadHeader(BufferedReader bin) throws MaltChainedException {
        String fileLine = "";
        Pattern tabPattern = Pattern.compile("\t");
        try {
            while ((fileLine = bin.readLine()) != null && fileLine.length() != 0 && fileLine.charAt(0) == '\t') {
                String[] items;
                try {
                    items = tabPattern.split(fileLine.substring(1));
                }
                catch (PatternSyntaxException e) {
                    throw new SymbolException("The header line of the symbol table  '" + fileLine.substring(1) + "' could not split into atomic parts. ", e);
                }
                if (items.length == 4) {
                    this.addSymbolTable(items[0], Integer.parseInt(items[1]), Integer.parseInt(items[2]), items[3]);
                    continue;
                }
                if (items.length == 3) {
                    this.addSymbolTable(items[0], Integer.parseInt(items[1]), 1, items[2]);
                    continue;
                }
                throw new SymbolException("The header line of the symbol table  '" + fileLine.substring(1) + "' must contain three or four columns. ");
            }
        }
        catch (NumberFormatException e) {
            throw new SymbolException("The symbol table file (.sym) contains a non-integer value in the header. ", e);
        }
        catch (IOException e) {
            throw new SymbolException("Could not load the symbol table. ", e);
        }
    }

    @Override
    public void load(InputStreamReader isr) throws MaltChainedException {
        try {
            String fileLine;
            BufferedReader bin = new BufferedReader(isr);
            SymbolTable table = null;
            bin.mark(2);
            if (bin.read() == 9) {
                bin.reset();
                this.loadHeader(bin);
            } else {
                bin.reset();
            }
            while ((fileLine = bin.readLine()) != null) {
                if (fileLine.length() <= 0) continue;
                table = this.addSymbolTable(fileLine);
                table.load(bin);
            }
            bin.close();
        }
        catch (IOException e) {
            throw new SymbolException("Could not load the symbol tables. ", e);
        }
    }

    @Override
    public void load(String fileName, String charSet) throws MaltChainedException {
        try {
            this.load(new InputStreamReader((InputStream)new FileInputStream(fileName), charSet));
        }
        catch (FileNotFoundException e) {
            throw new SymbolException("The symbol table file '" + fileName + "' cannot be found. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new SymbolException("The char set '" + charSet + "' is not supported. ", e);
        }
    }

    @Override
    public SymbolTable loadTagset(String fileName, String tableName, String charSet, int columnCategory, int columnType, String nullValueStrategy) throws MaltChainedException {
        try {
            String fileLine;
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(fileName), charSet));
            SymbolTable table = this.addSymbolTable(tableName, columnCategory, columnType, nullValueStrategy);
            while ((fileLine = br.readLine()) != null) {
                table.addSymbol(fileLine.trim());
            }
            return table;
        }
        catch (FileNotFoundException e) {
            throw new SymbolException("The tagset file '" + fileName + "' cannot be found. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new SymbolException("The char set '" + charSet + "' is not supported. ", e);
        }
        catch (IOException e) {
            throw new SymbolException("The tagset file '" + fileName + "' cannot be loaded. ", e);
        }
    }
}
