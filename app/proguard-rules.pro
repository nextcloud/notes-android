# don't obfuscate names and preserve line numbers so crash reports stay readable
-dontobfuscate
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# keep classes needed for single sign on
-keep class com.nextcloud.android.sso.** { *; }
