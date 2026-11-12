# Reglas de R8 para TOKE. Las librerías (Supabase, Ktor, Firebase, kotlinx.serialization) traen las suyas.
# Modelos serializables: se conservan los nombres de campos que viajan a Supabase.
-keepclassmembers class com.flabs.cobrate.core.** { @kotlinx.serialization.SerialName <fields>; }
-keep,includedescriptorclasses class com.flabs.cobrate.**$$serializer { *; }
-keepclassmembers class com.flabs.cobrate.** { *** Companion; }
-keepclasseswithmembers class com.flabs.cobrate.** { kotlinx.serialization.KSerializer serializer(...); }
-dontwarn org.slf4j.**

# Versión pública: los Log.v/d/i desaparecen del código final (el código ya usa LogSeguro, que apaga d fuera de depuración).
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
