#!/usr/bin/env bash
# ينشئ مفتاح توقيع ثابت للتطبيق. شغّله مرة واحدة فقط واحتفظ بنسخ احتياطية من الملف وكلمة المرور.
# فقدان المفتاح = لا يمكن تحديث التطبيق فوق النسخة المثبّتة (سيحتاج المستخدم لإلغاء التثبيت).
set -e
keytool -genkeypair -v -keystore hares-release.jks -alias hares \
  -keyalg RSA -keysize 2048 -validity 10000
echo
echo "تم إنشاء hares-release.jks"
echo "للبناء المحلي أنشئ keystore.properties بجانب settings.gradle.kts:"
echo "  storeFile=hares-release.jks"
echo "  storePassword=..."
echo "  keyAlias=hares"
echo "  keyPassword=..."
echo "ولـ GitHub Actions: base64 -w0 hares-release.jks  ثم ضعه في Secret باسم HARES_KEYSTORE_BASE64"
