package org.apache.commons.lang3.mutable;

import java.io.Serializable;

public class MutableBoolean implements Mutable<Boolean>, Serializable, Comparable<MutableBoolean> {
   private static final long serialVersionUID = -4830728138360036487L;
   private boolean value;

   public MutableBoolean() {
   }

   public MutableBoolean(boolean value) {
      this.value = value;
   }

   public MutableBoolean(Boolean value) {
      this.value = value;
   }

   public Boolean getValue() {
      return this.value;
   }

   public void setValue(boolean value) {
      this.value = value;
   }

   public void setValue(Boolean value) {
      this.value = value;
   }

   public boolean isTrue() {
      return this.value;
   }

   public boolean isFalse() {
      return !this.value;
   }

   public boolean booleanValue() {
      return this.value;
   }

   public Boolean toBoolean() {
      return this.booleanValue();
   }

   public boolean equals(Object obj) {
      if (obj instanceof MutableBoolean) {
         return this.value == ((MutableBoolean)obj).booleanValue();
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.value ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
   }

   public int compareTo(MutableBoolean other) {
      boolean anotherVal = other.value;
      return this.value == anotherVal ? 0 : (this.value ? 1 : -1);
   }

   public String toString() {
      return String.valueOf(this.value);
   }
}
