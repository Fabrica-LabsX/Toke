# Reglas de R8 para TOKE. Las librerías (Supabase, Ktor, Firebase, kotlinx.serialization) traen las suyas.
# Modelos serializables: se conservan los nombres de campos que viajan a Supabase.
-keepclassmembers class com.flabs.cobrate.core.** { @kotlinx.serialization.SerialName <fields>; }
-keep,includedescriptorclasses class com.flabs.cobrate.**$$serializer { *; }
-keepclassmembers class com.flabs.cobrate.** { *** Companion; }
-keepclasseswithmembers class com.flabs.cobrate.** { kotlinx.serialization.KSerializer serializer(...); }
-dontwarn org.slf4j.**
