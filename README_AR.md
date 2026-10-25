# BANIS Smart TV Launcher — IRIS Android 4.4.2

## ما الذي يقدمه هذا الإصدار؟
- واجهة رئيسية HOME قابلة للاختيار من إعدادات Android.
- تصميم أسود وذهبي، ساعة وتاريخ.
- اكتشاف التطبيقات التي تعرض أيقونة Launcher على الجهاز.
- البحث المحلي عن التطبيقات بالاسم أو اسم الحزمة.
- اختصار إلى إعدادات Android.
- زر الطقس يفتح صفحة wttr.in في المتصفح (ليس طقسًا حيًا مدمجًا داخل الواجهة).
- يدعم النقر والريموت/الأسهم وOK بحسب دعم الجهاز.

## ملاحظة مهمة
هذا **مشروع مصدر** وليس APK جاهزًا؛ بيئة الإنشاء التي أُنشئ فيها الملف لا تحتوي Android SDK أو Gradle ولا يمكنها الوصول إلى الإنترنت لتنزيلهما. لا تثبّت أي ملف APK مجهول. ابْنِ المشروع محليًا على CachyOS وفق الخطوات أدناه.

## البناء على CachyOS
يفضّل استخدام Android Studio مع JDK 17 وAndroid SDK. المشروع يستخدم Android Gradle Plugin 8.5.2 وGradle 8.7، وcompileSdk 35، مع minSdk 19 حتى يعمل على Android 4.4.2.

1. ثبّت Java وAndroid Studio من مستودعات/المصدر الرسمي الذي تثق به، ثم افتح Android Studio.
2. افتح مجلد `BANIS_Launcher_IRIS` كمشروع Gradle وانتظر مزامنة المشروع.
3. من SDK Manager ثبّت Android SDK Platform 35 وAndroid SDK Build-Tools.
4. اختر Build > Build APK(s).
5. الناتج عادةً: `app/build/outputs/apk/debug/app-debug.apk`.

يمكنك تثبيت أدوات ADB على CachyOS:
```bash
sudo pacman -S android-tools
adb devices
```
إذا ظهر التلفاز على USB ومصرّحًا به:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
إذا كان ADB عبر الشبكة مفعّلًا مسبقًا على التلفاز:
```bash
adb connect TV_IP:5555
adb -s TV_IP:5555 install -r app/build/outputs/apk/debug/app-debug.apk
```
استبدل `TV_IP` بعنوان التلفاز الحقيقي. لا تفترض أن المنفذ 5555 مفتوح؛ في Android 4.4 غالبًا يجب تفعيل TCP ADB مسبقًا عبر USB أو طريقة يدعمها الجهاز.

## اختيار المشغّل
بعد التثبيت، اضغط زر Home في الريموت. إذا ظهرت شاشة اختيار المشغّل، اختر BANIS Smart TV ثم اختر Always/دائمًا إذا تأكدت أنه يعمل. إذا لم تظهر شاشة الاختيار، افتح إعدادات Android > Apps > Home/Launcher (قد تختلف أسماء القوائم) وامسح الإعداد الافتراضي للواجهة الحالية ثم اضغط Home مجددًا.

## الرجوع للواجهة الأصلية
لا تحذف المشغّل الأصلي. من إعدادات التطبيقات امسح الإعداد الافتراضي لـ BANIS أو استخدم ADB:
```bash
adb shell pm clear-preferred-activities com.banis.launcher
```
ثم اضغط Home. إذا لم يعمل ذلك على نسخة الشركة، افتح إعدادات التطبيقات وأعد اختيار الواجهة الأصلية.

## حدود هذا الإصدار
- الطقس يفتح صفحة ويب خارج التطبيق؛ لا توجد API طقس مدمجة.
- لا يمكن ضمان عمل ADB عبر Wi-Fi أو فتح الإعدادات على كل نسخة IRIS؛ تختلف نسخ النظام المعدّلة.
- يجب اختبار APK على الجهاز قبل تعيينه افتراضيًا.
