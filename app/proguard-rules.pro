# Mantém interfaces Retrofit para chamadas HTTP
-keep interface retrofit2.http.* { *; }

# Mantém as classes essenciais do Retrofit
-keep class retrofit2.Call { *; }
-keep class retrofit2.Retrofit { *; }

# Mantém o conversor Gson do Retrofit
-keep class retrofit2.converter.gson.** { *; }

# Mantém os campos anotados com @SerializedName (importante para Gson)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Mantém seus modelos de domínio usados pelo Retrofit/Gson
-keep class com.dev.soarescrf.soares.domain.model.** { *; }

# Evita warnings irrelevantes das libs AndroidX e Retrofit
-dontwarn androidx.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**