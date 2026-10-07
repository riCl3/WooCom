# Firestore POJOs are instantiated via reflection by the Firestore SDK.
-keep class com.example.woocom.model.** { *; }
-keepclassmembers class com.example.woocom.model.** {
    public <init>();
}

# Razorpay checkout SDK.
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**

# Kotlin metadata required by some reflection-based libraries.
-keepattributes Signature,SourceFile,LineNumberTable
-keep class kotlin.Metadata { *; }

# Crash reports are only useful with line numbers, and the original file name adds
# nothing that gives away internal structure.
-renamesourcefileattribute SourceFile
