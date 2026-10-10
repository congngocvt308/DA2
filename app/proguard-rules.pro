# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Gson (type converters)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.cong.smartalarm.core.database.** { *; }
-keep class com.cong.smartalarm.core.network.** { *; }
-keep class com.cong.smartalarm.feature.topic.model.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Coroutines
-dontwarn kotlinx.coroutines.**

# --- NEW RULES TO FIX APK CRASH ---

# 1. Bảo vệ các lớp Logic và Thuật toán (Brain của Quiz)
-keep class com.cong.smartalarm.domain.** { *; }
-keep class com.cong.smartalarm.core.alarm.** { *; }

# 2. Quan trọng: Giữ lại hàm khởi tạo của ViewModels cho Compose
# Giúp hàm viewModel() tìm thấy constructor sau khi bị mã hóa code
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}
-keep class * implements androidx.lifecycle.ViewModelProvider$Factory

# 3. Bảo vệ các thư viện mạng và các thuộc tính Reflection
-keepattributes InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
