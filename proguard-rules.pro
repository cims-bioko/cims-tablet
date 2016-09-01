
-dontobfuscate

# androidplot accesses resources dynamically
-keep class org.openhds.mobile.R$* { *; }

# app's scriptable classes
-keep class org.openhds.mobile.navconfig.NavigatorConfig { *; }
-keep class * extends org.openhds.mobile.navconfig.forms.builders.FormPayloadBuilder { *; }
-keep class * extends org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer { *; }
-keep class * extends org.openhds.mobile.navconfig.forms.filters.FormFilter { *; }
-keep class * extends org.openhds.mobile.fragment.navigate.detail.DetailFragment { *; }

# jdom (xml)
-dontwarn org.jdom2.xpath.jaxen.**
-dontwarn org.jdom2.input.StAXEventBuilder
-dontwarn org.jdom2.input.StAXStreamBuilder
-dontwarn org.jdom2.output.StAXEventOutputter
-dontwarn org.jdom2.output.StAXStreamOutputter
-dontwarn org.jdom2.output.support.**

# mozilla rhino (scripting engine)
-dontwarn org.mozilla.javascript.tools.debugger.**
-dontwarn org.mozilla.javascript.tools.shell.**
-keep class org.mozilla.javascript.jdk15.VMBridge_jdk15 { *; }
-keep class org.mozilla.javascript.ImporterTopLevel { *; }
-keep class org.mozilla.javascript.NativeJavaTopPackage { *; }

# android plot (graphs)
-keep class com.androidplot.** { *; }

# lucene (full-text search)
-dontwarn org.apache.lucene.util.RamUsageEstimator
-keep class * extends org.apache.lucene.codecs.Codec
-keep class * extends org.apache.lucene.codecs.PostingsFormat
-keep class * extends org.apache.lucene.codecs.DocValuesFormat
-keep class * implements org.apache.lucene.util.Attribute
