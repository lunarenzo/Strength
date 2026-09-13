# =============================================================================
# ProGuard Obfuscation & Shrinking Configuration for Paper/Spigot Plugin
# =============================================================================

# Global Flags
-dontshrink
-dontoptimize
-dontwarn **
-ignorewarnings
-repackageclasses 'lunatech.strength.internal'
-allowaccessmodification
-keepparameternames
-renamesourcefileattribute 'SourceFile'
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# -----------------------------------------------------------------------------
# 1. Paper / Spigot Core Entry Points (MUST NEVER BE RENAMED)
# -----------------------------------------------------------------------------
-keep public class lunatech.strength.Strength { *; }
-keep public class lunatech.strength.PaperPluginLoader { *; }
-keep public class * extends org.bukkit.plugin.java.JavaPlugin { *; }
-keep public class * implements org.bukkit.plugin.Plugin { *; }
-keep public class * implements io.papermc.paper.plugin.bootstrap.PluginBootstrap { *; }
-keep public class * implements io.papermc.paper.plugin.loader.PluginLoader { *; }

# -----------------------------------------------------------------------------
# 2. Bukkit Event Listeners (@EventHandler reflection preservation)
# -----------------------------------------------------------------------------
-keep public class * implements org.bukkit.event.Listener { *; }
-keepclassmembers class * implements org.bukkit.event.Listener {
    @org.bukkit.event.EventHandler <methods>;
}

# -----------------------------------------------------------------------------
# 3. Sponge Configurate (@ConfigSerializable field preservation)
# -----------------------------------------------------------------------------
-keep @org.spongepowered.configurate.objectmapping.ConfigSerializable class * { *; }
-keepclassmembers class * {
    @org.spongepowered.configurate.objectmapping.ConfigSerializable <fields>;
    @org.spongepowered.configurate.objectmapping.meta.Comment <fields>;
}

# -----------------------------------------------------------------------------
# 4. CommandAPI & Command Registration
# -----------------------------------------------------------------------------
-keep public class * extends dev.jorel.commandapi.CommandAPICommand { *; }
-keep public class lunatech.strength.command.** { *; }

# -----------------------------------------------------------------------------
# 5. External Integration Hooks (PlaceholderAPI, Vault, WorldGuard, PvPManager)
# -----------------------------------------------------------------------------
-keep public class * extends me.clip.placeholderapi.expansion.PlaceholderExpansion { *; }
-keep public class lunatech.strength.hook.** { *; }
-keep public class lunatech.strength.integration.** { *; }

# -----------------------------------------------------------------------------
# 6. DRM & License Manager Core Methods (Keep public signatures intact)
# -----------------------------------------------------------------------------
-keepclassmembers class lunatech.strength.license.LicenseManager {
    public double getScale(...);
    public boolean isAuthenticated();
    public void verifyAsync();
}
