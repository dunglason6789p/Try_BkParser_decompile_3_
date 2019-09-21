/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.feature;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class DistanceFeature
implements FeatureFunction {
    public static final Class<?>[] paramTypes = new Class[]{AddressFunction.class, AddressFunction.class, String.class};
    private static final Pattern splitPattern = Pattern.compile("\\|");
    private AddressFunction addressFunction1;
    private AddressFunction addressFunction2;
    private final SymbolTableHandler tableHandler;
    private SymbolTable table;
    private final SingleFeatureValue featureValue = new SingleFeatureValue(this);
    private String normalizationString;
    private final Map<Integer, String> normalization;

    public DistanceFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
        this.tableHandler = tableHandler;
        this.normalization = new LinkedHashMap<Integer, String>();
    }

    @Override
    public void initialize(Object[] arguments) throws MaltChainedException {
        if (arguments.length != 3) {
            throw new SyntaxGraphException("Could not initialize DistanceFeature: number of arguments is not correct. ");
        }
        if (!(arguments[0] instanceof AddressFunction)) {
            throw new SyntaxGraphException("Could not initialize DistanceFeature: the first argument is not an address function. ");
        }
        if (!(arguments[1] instanceof AddressFunction)) {
            throw new SyntaxGraphException("Could not initialize DistanceFeature: the second argument is not an address function. ");
        }
        if (!(arguments[2] instanceof String)) {
            throw new SyntaxGraphException("Could not initialize DistanceFeature: the third argument is not a string. ");
        }
        this.setAddressFunction1((AddressFunction)arguments[0]);
        this.setAddressFunction2((AddressFunction)arguments[1]);
        this.normalizationString = (String)arguments[2];
        this.setSymbolTable(this.tableHandler.addSymbolTable("DISTANCE_" + this.normalizationString, 1, 1, "one"));
        String[] items = splitPattern.split(this.normalizationString);
        if (items.length <= 0 || !items[0].equals("0")) {
            throw new SyntaxGraphException("Could not initialize DistanceFeature (" + this + "): the third argument (normalization) must contain a list of integer values separated with | and the first element must be 0.");
        }
        int tmp = -1;
        for (int i = 0; i < items.length; ++i) {
            int v;
            try {
                v = Integer.parseInt(items[i]);
            }
            catch (NumberFormatException e) {
                throw new SyntaxGraphException("Could not initialize DistanceFeature (" + this + "): the third argument (normalization) must contain a sorted list of integer values separated with |", e);
            }
            this.normalization.put(v, ">=" + v);
            this.table.addSymbol(">=" + v);
            if (tmp != -1 && tmp >= v) {
                throw new SyntaxGraphException("Could not initialize DistanceFeature (" + this + "): the third argument (normalization) must contain a sorted list of integer values separated with |");
            }
            tmp = v;
        }
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return paramTypes;
    }

    @Override
    public String getSymbol(int code) throws MaltChainedException {
        return this.table.getSymbolCodeToString(code);
    }

    @Override
    public int getCode(String symbol) throws MaltChainedException {
        return this.table.getSymbolStringToCode(symbol);
    }

    @Override
    public void update() throws MaltChainedException {
        AddressValue arg1 = this.addressFunction1.getAddressValue();
        AddressValue arg2 = this.addressFunction2.getAddressValue();
        if (arg1.getAddress() == null || arg2.getAddress() == null) {
            this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
            this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
            this.featureValue.setValue(1.0);
            this.featureValue.setNullValue(true);
        } else {
            DependencyNode node1 = (DependencyNode)arg1.getAddress();
            DependencyNode node2 = (DependencyNode)arg2.getAddress();
            if (!node1.isRoot() && !node2.isRoot()) {
                int index1 = node1.getIndex();
                int index2 = node2.getIndex();
                int distance = Math.abs(index1 - index2);
                int lower = -1;
                boolean f = false;
                for (Integer upper : this.normalization.keySet()) {
                    if (distance >= lower && distance < upper) {
                        this.featureValue.setIndexCode(this.table.getSymbolStringToCode(this.normalization.get(lower)));
                        this.featureValue.setSymbol(this.normalization.get(lower));
                        this.featureValue.setValue(1.0);
                        f = true;
                        break;
                    }
                    lower = upper;
                }
                if (!f) {
                    this.featureValue.setIndexCode(this.table.getSymbolStringToCode(this.normalization.get(lower)));
                    this.featureValue.setSymbol(this.normalization.get(lower));
                    this.featureValue.setValue(1.0);
                }
                this.featureValue.setNullValue(false);
            } else {
                this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.ROOT_NODE));
                this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.ROOT_NODE));
                this.featureValue.setValue(1.0);
                this.featureValue.setNullValue(true);
            }
        }
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.featureValue;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.table;
    }

    public AddressFunction getAddressFunction1() {
        return this.addressFunction1;
    }

    public void setAddressFunction1(AddressFunction addressFunction1) {
        this.addressFunction1 = addressFunction1;
    }

    public AddressFunction getAddressFunction2() {
        return this.addressFunction2;
    }

    public void setAddressFunction2(AddressFunction addressFunction2) {
        this.addressFunction2 = addressFunction2;
    }

    public SymbolTableHandler getTableHandler() {
        return this.tableHandler;
    }

    public void setSymbolTable(SymbolTable table) {
        this.table = table;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String getMapIdentifier() {
        return this.getSymbolTable().getName();
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
        return obj.toString().equals(this.toString());
    }

    public int hashCode() {
        return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Distance(");
        sb.append(this.addressFunction1.toString());
        sb.append(", ");
        sb.append(this.addressFunction2.toString());
        sb.append(", ");
        sb.append(this.normalizationString);
        sb.append(')');
        return sb.toString();
    }
}
