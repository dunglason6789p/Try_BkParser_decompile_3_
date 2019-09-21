package org.maltparser.core.syntaxgraph.feature;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.AddressFunction;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.value.AddressValue;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class NumOfFeature implements FeatureFunction {
   public static final Class<?>[] paramTypes = new Class[]{AddressFunction.class, String.class, String.class};
   private static final Pattern splitPattern = Pattern.compile("\\|");
   private AddressFunction addressFunction;
   private final SymbolTableHandler tableHandler;
   private SymbolTable table;
   private final SingleFeatureValue featureValue;
   private NumOfFeature.NumOfRelation numOfRelation;
   private String numOfRelationName;
   private String normalizationString;
   private final Map<Integer, String> normalization;

   public NumOfFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
      this.tableHandler = tableHandler;
      this.featureValue = new SingleFeatureValue(this);
      this.normalization = new LinkedHashMap();
   }

   public void initialize(Object[] arguments) throws MaltChainedException {
      if (arguments.length != 3) {
         throw new SyntaxGraphException("Could not initialize NumOfFeature: number of arguments are not correct. ");
      } else if (!(arguments[0] instanceof AddressFunction)) {
         throw new SyntaxGraphException("Could not initialize NumOfFeature: the first argument is not an address function. ");
      } else if (!(arguments[1] instanceof String)) {
         throw new SyntaxGraphException("Could not initialize NumOfFeature: the second argument (relation) is not a string. ");
      } else if (!(arguments[2] instanceof String)) {
         throw new SyntaxGraphException("Could not initialize NumOfFeature: the third argument (normalization) is not a string. ");
      } else {
         this.setAddressFunction((AddressFunction)arguments[0]);
         this.setNumOfRelation((String)arguments[1]);
         this.normalizationString = (String)arguments[2];
         this.setSymbolTable(this.tableHandler.addSymbolTable("NUMOF" + this.normalizationString, 1, 1, "one"));
         String[] items = splitPattern.split(this.normalizationString);
         if (items.length > 0 && items[0].equals("0")) {
            int tmp = -1;

            for(int i = 0; i < items.length; ++i) {
               int v;
               try {
                  v = Integer.parseInt(items[i]);
               } catch (NumberFormatException var7) {
                  throw new SyntaxGraphException("Could not initialize NumOfFeature (" + this + "): the third argument (normalization) must contain a sorted list of integer values separated with |", var7);
               }

               this.normalization.put(v, ">=" + v);
               this.table.addSymbol(">=" + v);
               if (tmp != -1 && tmp >= v) {
                  throw new SyntaxGraphException("Could not initialize NumOfFeature (" + this + "): the third argument (normalization) must contain a sorted list of integer values separated with |");
               }

               tmp = v;
            }

         } else {
            throw new SyntaxGraphException("Could not initialize NumOfFeature (" + this + "): the third argument (normalization) must contain a list of integer values separated with | and the first element must be 0.");
         }
      }
   }

   public Class<?>[] getParameterTypes() {
      return paramTypes;
   }

   public String getSymbol(int code) throws MaltChainedException {
      return this.table.getSymbolCodeToString(code);
   }

   public int getCode(String symbol) throws MaltChainedException {
      return this.table.getSymbolStringToCode(symbol);
   }

   public void update() throws MaltChainedException {
      AddressValue arg1 = this.addressFunction.getAddressValue();
      if (arg1.getAddress() == null) {
         this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
         this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
         this.featureValue.setNullValue(true);
      } else {
         DependencyNode node = (DependencyNode)arg1.getAddress();
         int numof = 0;
         if (this.numOfRelation == NumOfFeature.NumOfRelation.DEPS) {
            numof = node.getLeftDependentCount() + node.getRightDependentCount();
         } else if (this.numOfRelation == NumOfFeature.NumOfRelation.LDEPS) {
            numof = node.getLeftDependentCount();
         } else if (this.numOfRelation == NumOfFeature.NumOfRelation.RDEPS) {
            numof = node.getRightDependentCount();
         }

         int lower = -1;
         boolean f = false;

         Integer upper;
         for(Iterator i$ = this.normalization.keySet().iterator(); i$.hasNext(); lower = upper) {
            upper = (Integer)i$.next();
            if (numof >= lower && numof < upper) {
               this.featureValue.setIndexCode(this.table.getSymbolStringToCode((String)this.normalization.get(lower)));
               this.featureValue.setSymbol((String)this.normalization.get(lower));
               f = true;
               break;
            }
         }

         if (!f) {
            this.featureValue.setIndexCode(this.table.getSymbolStringToCode((String)this.normalization.get(lower)));
            this.featureValue.setSymbol((String)this.normalization.get(lower));
         }

         this.featureValue.setNullValue(false);
      }

      this.featureValue.setValue(1.0D);
   }

   public void setNumOfRelation(String numOfRelationName) {
      this.numOfRelationName = numOfRelationName;
      this.numOfRelation = NumOfFeature.NumOfRelation.valueOf(numOfRelationName.toUpperCase());
   }

   public NumOfFeature.NumOfRelation getNumOfRelation() {
      return this.numOfRelation;
   }

   public FeatureValue getFeatureValue() {
      return this.featureValue;
   }

   public SymbolTable getSymbolTable() {
      return this.table;
   }

   public AddressFunction getAddressFunction() {
      return this.addressFunction;
   }

   public void setAddressFunction(AddressFunction addressFunction) {
      this.addressFunction = addressFunction;
   }

   public void setSymbolTable(SymbolTable table) {
      this.table = table;
   }

   public int getType() {
      return 1;
   }

   public String getMapIdentifier() {
      return this.getSymbolTable().getName();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : obj.toString().equals(this.toString());
      }
   }

   public int hashCode() {
      return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("NumOf(");
      sb.append(this.addressFunction.toString());
      sb.append(", ");
      sb.append(this.numOfRelationName);
      sb.append(", ");
      sb.append(this.normalizationString);
      sb.append(')');
      return sb.toString();
   }

   public static enum NumOfRelation {
      LDEPS,
      RDEPS,
      DEPS;

      private NumOfRelation() {
      }
   }
}