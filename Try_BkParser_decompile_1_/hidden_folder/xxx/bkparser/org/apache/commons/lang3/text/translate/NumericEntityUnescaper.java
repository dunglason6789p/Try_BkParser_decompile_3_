package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;

public class NumericEntityUnescaper extends CharSequenceTranslator {
   private final EnumSet<NumericEntityUnescaper.OPTION> options;

   public NumericEntityUnescaper(NumericEntityUnescaper.OPTION... options) {
      if (options.length > 0) {
         this.options = EnumSet.copyOf(Arrays.asList(options));
      } else {
         this.options = EnumSet.copyOf(Arrays.asList(NumericEntityUnescaper.OPTION.semiColonRequired));
      }

   }

   public boolean isSet(NumericEntityUnescaper.OPTION option) {
      return this.options == null ? false : this.options.contains(option);
   }

   public int translate(CharSequence input, int index, Writer out) throws IOException {
      int seqEnd = input.length();
      if (input.charAt(index) == '&' && index < seqEnd - 2 && input.charAt(index + 1) == '#') {
         int start = index + 2;
         boolean isHex = false;
         char firstChar = input.charAt(start);
         if (firstChar == 'x' || firstChar == 'X') {
            ++start;
            isHex = true;
            if (start == seqEnd) {
               return 0;
            }
         }

         int end;
         for(end = start; end < seqEnd && (input.charAt(end) >= '0' && input.charAt(end) <= '9' || input.charAt(end) >= 'a' && input.charAt(end) <= 'f' || input.charAt(end) >= 'A' && input.charAt(end) <= 'F'); ++end) {
         }

         boolean semiNext = end != seqEnd && input.charAt(end) == ';';
         if (!semiNext) {
            if (this.isSet(NumericEntityUnescaper.OPTION.semiColonRequired)) {
               return 0;
            }

            if (this.isSet(NumericEntityUnescaper.OPTION.errorIfNoSemiColon)) {
               throw new IllegalArgumentException("Semi-colon required at end of numeric entity");
            }
         }

         int entityValue;
         try {
            if (isHex) {
               entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
            } else {
               entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
            }
         } catch (NumberFormatException var12) {
            return 0;
         }

         if (entityValue > 65535) {
            char[] chrs = Character.toChars(entityValue);
            out.write(chrs[0]);
            out.write(chrs[1]);
         } else {
            out.write(entityValue);
         }

         return 2 + (end - start) + (isHex ? 1 : 0) + (semiNext ? 1 : 0);
      } else {
         return 0;
      }
   }

   public static enum OPTION {
      semiColonRequired,
      semiColonOptional,
      errorIfNoSemiColon;

      private OPTION() {
      }
   }
}
