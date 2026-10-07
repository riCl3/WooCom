# Firestore POJOs are instantiated via reflection by the Firestore SDK.
-keep class com.example.woocom.model.** { *; }
-keepclassmembers class com.example.woocom.model.** {
    public <init>();
}

# Razorpay checkout SDK.
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**

# Kotlin metadata required by some reflection-based libraries.
-keepattributes Signature
-keep class kotlin.Metadata { *; }

# Firebase core rules are bundled with the SDKs; keep the SDK from being shrunk
# incorrectly on reflective lookups.
-dontwarn com.google.firebase.**