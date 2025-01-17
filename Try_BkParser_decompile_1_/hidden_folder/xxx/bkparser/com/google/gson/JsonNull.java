package com.google.gson;

public final class JsonNull extends JsonElement {
   public static final JsonNull INSTANCE = new JsonNull();

   /** @deprecated */
   @Deprecated
   public JsonNull() {
   }

   public JsonNull deepCopy() {
      return INSTANCE;
   }

   public int hashCode() {
      return JsonNull.class.hashCode();
   }

   public boolean equals(Object other) {
      return this == other || other instanceof JsonNull;
   }
}
